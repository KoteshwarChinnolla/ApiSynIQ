package main

import (
	AudioGrpcCmd "ApiGateway/cmd/AudioGrpc"
	WebsocketCmd "ApiGateway/cmd/Websocket"
)

func main() {
	go AudioGrpcCmd.CreateGrpcServer()
	WebsocketCmd.CreateWebServer()
}
