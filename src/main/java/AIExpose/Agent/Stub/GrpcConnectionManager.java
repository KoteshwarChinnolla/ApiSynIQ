package AIExpose.Agent.Stub;

import com.apisyniq.grpc.ControllerGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class GrpcConnectionManager {

    public GrpcConnectionManager(){}

    private ManagedChannel channel;
    private ControllerGrpc.ControllerBlockingStub stub;

    @Value("${GRPC_CLIENT_SERVER_PORT:7322}")
    private int GRPC_PORT;

    @Value("${GRPC_CLIENT_SERVER_ADDRESS:localhost}")
    private String GRPC_SERVER_ADDRESS;

    public boolean isConnected() {
        return channel != null && !channel.isShutdown() && !channel.isTerminated();
    }

    public synchronized void connect() {
        if (isConnected()) return; // already connected

        channel = ManagedChannelBuilder
                .forAddress(GRPC_SERVER_ADDRESS, GRPC_PORT)
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

