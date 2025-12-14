// Copyright 2013 The Gorilla WebSocket Authors. All rights reserved.
// Use of this source code is governed by a BSD-style
// license that can be found in the LICENSE file.

package WebsocketCmd

import (
	ws "ApiGateway/pkg/Websocket"
	"ApiGateway/pkg/stub"
	"flag"
	"log"
	"net/http"
)

var addr = flag.String("addr", ":8081", "http service address")

func CreateWebServer() {
	flag.Parse()
	ws.NewHub()
	hub := ws.GlobalHub
	go hub.Run()
	stub.InitConnection()
	fs := http.FileServer(http.Dir("./static"))
	http.Handle("/", fs)
	http.HandleFunc("/ws", func(w http.ResponseWriter, r *http.Request) {
		ws.ServeWs(hub, w, r)
	})
	err := http.ListenAndServe(*addr, nil)
	if err != nil {
		log.Fatal("ListenAndServe: ", err)
	}
}
