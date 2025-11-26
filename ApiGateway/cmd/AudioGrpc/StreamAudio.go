package AudioGrpcCmd

import (
	websocket "ApiGateway/pkg/Websocket"
	pb "ApiGateway/pkg/proto"
	"io"
	"log"
	"net"

	"google.golang.org/grpc"
)

type server struct {
	pb.UnimplementedTTSServiceServer
}

func (s *server) UploadAudio(stream pb.TTSService_UploadAudioServer) error {
	log.Println("Receiving audio stream from Python...")

	for {
		chunk, err := stream.Recv()
		if err == io.EOF {
			log.Println("Stream ended")
			return nil
		}
		if err != nil {
			return err
		}
		// websocket.GlobalHub.BroadcastAudio(chunk.GetAudioOut())
		websocket.GlobalHub.Broadcast <- chunk.GetAudioOut()
		log.Printf("Received chunk: %d bytes, sample_rate=%d, channels=%d, text=%s\n",
			len(chunk.GetAudioOut().AudioBytes), chunk.GetAudioOut().Channels, chunk.GetAudioOut().SampleRate, chunk.GetAudioOut().Text)
	}
}

func CreateGrpcServer() {
	lis, err := net.Listen("tcp", ":7323")
	if err != nil {
		log.Fatalf("failed to listen: %v", err)
	}

	grpcServer := grpc.NewServer()
	pb.RegisterTTSServiceServer(grpcServer, &server{})

	log.Println("gRPC server listening on :7323")
	if err := grpcServer.Serve(lis); err != nil {
		log.Fatalf("failed to serve: %v", err)
	}
}
