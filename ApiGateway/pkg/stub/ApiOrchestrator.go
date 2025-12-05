package stub

import (
	pb "ApiGateway/pkg/proto"
	"google.golang.org/grpc"
	"log"
)

var (
	Orchestrator *ApiOrchestrator
)

type ApiOrchestrator struct {
	Client pb.TTSServiceClient
	Stream pb.TTSService_UploadAudioClient
}

func InitConnection() {
	conn, err := grpc.NewClient("localhost:7324", grpc.WithInsecure())
	if err != nil {
		log.Panic(err)
	}

	c := pb.NewTTSServiceClient(conn)
	Orchestrator = &ApiOrchestrator{
		Client: c,
		Stream: nil,
	}
}

func (a *ApiOrchestrator) SendAudioPacket(packet *pb.StreamPacket_RawAudio) error {
	return a.Stream.Send(&pb.StreamPacket{Packet: packet})
}

func (a *ApiOrchestrator) SendTextPacket(packet *pb.StreamPacket_Text) error {
	return a.Stream.Send(&pb.StreamPacket{Packet: packet})
}
