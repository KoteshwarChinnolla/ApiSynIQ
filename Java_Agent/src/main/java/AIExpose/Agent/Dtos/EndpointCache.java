package AIExpose.Agent.Dtos;

import java.util.ArrayList;
import java.util.List;

public class EndpointCache {

    private final List<EndpointData> endpoints = new ArrayList<>();

    public void add(EndpointData data) {
        endpoints.add(data);
    }

    public List<EndpointData> all() {
        return endpoints;
    }
}

