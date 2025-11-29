package AIExpose.Agent.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import AIExpose.Agent.Controller.EndpointHandlerListener;
import AIExpose.Agent.Dtos.EndpointCache;

@Configuration
public class EndpointMetadataConfig {

    @Bean
    public EndpointCache endpointCache() {
        return new EndpointCache();
    }

    @Bean
    public EndpointHandlerListener endpointHandlerListener() {
        return new EndpointHandlerListener();
    }
}
