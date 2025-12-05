// Copyright 2013 The Gorilla WebSocket Authors. All rights reserved.
// Use of this source code is governed by a BSD-style
// license that can be found in the LICENSE file.

package websocket

import (
	"ApiGateway/pkg/proto"
	"log"
)

var (
	GlobalHub *Hub
)

type Hub struct {
	clients    map[string]*Client
	Broadcast  chan *proto.AudioChunk
	register   chan *Client
	unregister chan *Client
	update     chan *Client
}

func NewHub() {
	GlobalHub = &Hub{
		Broadcast:  make(chan *proto.AudioChunk),
		register:   make(chan *Client),
		unregister: make(chan *Client),
		update:     make(chan *Client),
		clients:    make(map[string]*Client),
	}
}

func (h *Hub) broadcastAudio(data *proto.AudioChunk) {
	client := h.clients[data.SessionId]
	if client != nil {
		client.send <- data
	}
}

func (h *Hub) removeClient(SessionID string, client *Client) {
	var error proto.Error
	error.Error = "CLOSE_CONNECTION"
	error.Username = client.UserID
	error.SessionId = client.SessionID
	error.StreamId = client.StreamID
	error.Language = client.Language
	err := client.orchestrator.Stream.Send(&proto.StreamPacket{Packet: &proto.StreamPacket_Error{Error: &error}})
	if err != nil {
		log.Printf("error at removeClient %s", err)
	}
	delete(h.clients, SessionID)
	close(client.send)
}

func (h *Hub) updateClient(SessionID string, client *Client) {
	delete(h.clients, SessionID)
	h.clients[SessionID] = client
	log.Printf("Client Updated for SessionId %s", SessionID)
}

func (h *Hub) Run() {
	for {
		select {
		case client := <-h.register:
			h.clients[client.SessionID] = client
		case client := <-h.unregister:
			if _, ok := h.clients[client.SessionID]; ok {
				h.removeClient(client.SessionID, client)
				log.Printf("connection closed %s for session %s", client.UserID, client.SessionID)
			}
		case client := <-h.update:
			if _, ok := h.clients[client.SessionID]; ok {
				h.updateClient(client.SessionID, client)
			}
		case message := <-h.Broadcast:
			h.broadcastAudio(message)
		}
	}
}
