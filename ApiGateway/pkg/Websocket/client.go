// Copyright 2013 The Gorilla WebSocket Authors. All rights reserved.
// Use of this source code is governed by a BSD-style
// license that can be found in the LICENSE file.

package websocket

import (
	"ApiGateway/pkg/proto"
	"ApiGateway/pkg/stub"
	"context"
	"encoding/json"
	"log"
	"net/http"
	"time"

	"github.com/gorilla/websocket"
)

const (
	// Time allowed to write a message to the peer.
	writeWait = 10 * time.Second

	// Time allowed to read the next pong message from the peer.
	pongWait = 60 * time.Second

	// Send pings to peer with this period. Must be less than pongWait.
	pingPeriod = (pongWait * 9) / 10

	// Maximum message size allowed from peer.
	maxMessageSize = 512
)

var (
	newline = []byte{'\n'}
	space   = []byte{' '}
)

var upgrader = websocket.Upgrader{
	ReadBufferSize:  1024,
	WriteBufferSize: 1024,
}

type Client struct {
	hub          *Hub
	conn         *websocket.Conn
	send         chan *proto.AudioChunk
	orchestrator *stub.ApiOrchestrator
	UserID       string
	StreamID     string
	SessionID    string
	Language     string
	AudioOption  string
	Options      map[string]string
}

func (c *Client) readPump() {
	defer func() {
		c.orchestrator.Stream.CloseSend()
		c.conn.Close()
	}()

	for {
		_, message, err := c.conn.ReadMessage()
		if err != nil {
			log.Println("ws read error:", err)
			break
		}
		if string(message) == "CLOSE_CONNECTION" {
			c.orchestrator.Stream.CloseSend()
		}
		var rawAudio proto.RawAudio
		rawAudio.AudioBytes = message
		err = c.orchestrator.SendAudioPacket(&proto.StreamPacket_RawAudio{
			RawAudio: &rawAudio,
		})
		if err != nil {
			log.Println("grpc send error:", err)
		}
	}
}

func (c *Client) writePump() {
	ticker := time.NewTicker(pingPeriod)
	defer func() {
		ticker.Stop()
		c.conn.Close()
		print("connection closed" + c.UserID)
	}()

	for {
		select {
		case audioBytes, ok := <-c.send:
			if !ok {
				// channel closed
				c.conn.WriteMessage(websocket.CloseMessage, []byte{})
				return
			}

			// SEND **binary audio** to browser
			// err := c.conn.WriteMessage(websocket.BinaryMessage, audioBytes)
			err := c.conn.WriteJSON(audioBytes)
			if err != nil {
				log.Println("write error:", err)
				return
			}

		case <-ticker.C:
			_ = c.conn.WriteMessage(websocket.PingMessage, []byte("ping"))
		}
	}
}

func ServeWs(hub *Hub, w http.ResponseWriter, r *http.Request) {
	conn, err := upgrader.Upgrade(w, r, nil)
	if err != nil {
		log.Println(err)
		return
	}
	name := r.URL.Query().Get("name")

	c := stub.Orchestrator
	stream, err := c.Client.UploadAudio(context.Background())
	if err != nil {
		log.Panic("No grpc Stream created")
		return
	}
	c.Stream = stream
	client := &Client{hub: hub, conn: conn, send: make(chan *proto.AudioChunk, 256), orchestrator: c, UserID: name}

	_, msg, _ := conn.ReadMessage()
	var incoming proto.IncomingAudio
	json.Unmarshal(msg, &incoming)
	client.Language = incoming.Language
	client.StreamID = incoming.StreamId
	client.AudioOption = incoming.AudioOption
	client.SessionID = incoming.SessionId
	client.hub.register <- client

	stream.Send(&proto.StreamPacket{Packet: &proto.StreamPacket_AudioIn{
		AudioIn: &incoming,
	}})
	print("connected " + name)

	go client.writePump()
	go client.readPump()
}
