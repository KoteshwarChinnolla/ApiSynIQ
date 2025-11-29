package AIExpose.Agent.Dtos;

import com.apisyniq.grpc.repeatedInput;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EndpointCache {

    private final Map<String, List<Map<String, EndpointData>>> endpoints = new HashMap<>();
    private final List<com.apisyniq.grpc.EndpointData> grpcEndpoints = new ArrayList<>();

    public void add(String className, String methodName, EndpointData data) {
        List<Map<String, EndpointData>> list =
                endpoints.computeIfAbsent(className, k -> new ArrayList<>());

        Map<String, EndpointData> endpointDataMap = new HashMap<>();
        endpointDataMap.put(methodName, data);

        list.add(endpointDataMap);
    }

    public Map<String, List<Map<String, EndpointData>>> all() {
        return endpoints;
    }

    public void getGrpc(EndpointData data) {
        grpcEndpoints.add(data.toGrpcEndpointData());
    }

    public List<com.apisyniq.grpc.EndpointData> grpcAll() {
        return grpcEndpoints;
    }
}

