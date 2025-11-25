// Copyright 2013 The Gorilla WebSocket Authors. All rights reserved.
// Use of this source code is governed by a BSD-style
// license that can be found in the LICENSE file.

package websocket

import (
	"ApiGateway/pkg/proto"
	"log"
)

// Hub maintains the set of active clients and broadcasts messages to the
// clients.
var (
	GlobalHub *Hub
)

type Hub struct {
	clients    map[*Client]string
	broadcast  chan []byte
	register   chan *Client
	unregister chan *Client
}

func NewHub() {
	GlobalHub = &Hub{
		broadcast:  make(chan []byte),
		register:   make(chan *Client),
		unregister: make(chan *Client),
		clients:    make(map[*Client]string),
	}
}

func (h *Hub) BroadcastAudio(data *proto.AudioChunk) {
	for client := range h.clients {
		log.Println("Broadcasting audio to " + client.UserID)
		client.conn.WriteJSON(data)
	}
}

func (h *Hub) Run() {
	for {
		select {
		case client := <-h.register:
			h.clients[client] = client.UserID
		case client := <-h.unregister:
			if _, ok := h.clients[client]; ok {
				delete(h.clients, client)
				close(client.send)
			}
		case message := <-h.broadcast:
			for client := range h.clients {
				select {
				case client.send <- message:
				default:
					close(client.send)
					delete(h.clients, client)
				}
			}
		}
	}
}
