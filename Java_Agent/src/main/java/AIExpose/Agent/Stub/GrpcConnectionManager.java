package AIExpose.Agent.Stub;

import com.apisyniq.grpc.ControllerGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.stereotype.Service;

@Service
public class GrpcConnectionManager {

    private ManagedChannel channel;
    private ControllerGrpc.ControllerBlockingStub stub;

    public boolean isConnected() {
        return channel != null && !channel.isShutdown() && !channel.isTerminated();
    }

    public synchronized void connect() {
        if (isConnected()) return; // already connected

        channel = ManagedChannelBuilder
                .forAddress("localhost", 7322)
                .usePlaintext()
                .build();

        stub = ControllerGrpc.newBlockingStub(channel);
        System.out.println("GRPC Channel created successfully!");
    }

    public ControllerGrpc.ControllerBlockingStub getStub() {
        if (!isConnected()) {
            throw new IllegalStateException("Channel is not connected. Call /connect first.");
        }
        return stub;
    }
}

