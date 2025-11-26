// Copyright 2013 The Gorilla WebSocket Authors. All rights reserved.
// Use of this source code is governed by a BSD-style
// license that can be found in the LICENSE file.

package websocket

import (
	"ApiGateway/pkg/proto"
)

var (
	GlobalHub *Hub
)

type Hub struct {
	clients    map[string][]*Client
	Broadcast  chan *proto.AudioChunk
	register   chan *Client
	unregister chan *Client
}

func NewHub() {
	GlobalHub = &Hub{
		Broadcast:  make(chan *proto.AudioChunk),
		register:   make(chan *Client),
		unregister: make(chan *Client),
		clients:    make(map[string][]*Client),
	}
}

func (h *Hub) broadcastAudio(data *proto.AudioChunk) {
	for _, client := range h.clients[data.Username] {
		client.send <- data
	}
}

func (h *Hub) removeClient(userID string, client *Client) {
	clients := h.clients[userID]

	for i, c := range clients {
		if c == client {
			h.clients[userID] = append(clients[:i], clients[i+1:]...)
			if len(h.clients[userID]) == 0 {
				delete(h.clients, userID)
			}
			return
		}
	}
}

func (h *Hub) Run() {
	for {
		select {
		case client := <-h.register:
			h.clients[client.UserID] = append(h.clients[client.UserID], client)

		case client := <-h.unregister:
			if _, ok := h.clients[client.UserID]; ok {
				h.removeClient(client.UserID, client)
				close(client.send)
			}
		case message := <-h.Broadcast:
			h.broadcastAudio(message)
		}
	}
}
