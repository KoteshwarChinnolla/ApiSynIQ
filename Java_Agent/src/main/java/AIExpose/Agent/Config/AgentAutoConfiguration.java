package AIExpose.Agent.Config;

import AIExpose.Agent.AIExposeEp.EndpointBuilder;
import jakarta.servlet.ServletContext;

import java.util.List;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import AIExpose.Agent.AIExposeEp.EndpointScanner;
import AIExpose.Agent.Controller.Data;
import AIExpose.Agent.Dtos.EndpointCache;
import AIExpose.Agent.Dtos.EndpointData;
import AIExpose.Agent.Stub.GrpcConnectionManager;
import org.springframework.core.env.Environment;

@AutoConfiguration
public class AgentAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public GrpcConnectionManager grpcConnectionManager() {
        return new GrpcConnectionManager();
    }
    
    @Bean
    @ConditionalOnMissingBean
    public EndpointCache endpointCache(List<EndpointData> endpoint){
        return new EndpointCache();
    }

    @Bean
    @ConditionalOnMissingBean
    public EndpointData endpointData(){
        return new EndpointData();
    }
    @Bean
    @ConditionalOnMissingBean
    public Data data(EndpointCache context,
                     GrpcConnectionManager grpcConnectionManager) {
        return new Data(context, grpcConnectionManager);
    }
}

