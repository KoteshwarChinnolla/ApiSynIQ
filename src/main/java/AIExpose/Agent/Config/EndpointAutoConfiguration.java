package AIExpose.Agent.Config;

import AIExpose.Agent.AIExposeEp.EndpointBuilder;
import AIExpose.Agent.AIExposeEp.EndpointScanner;
import AIExpose.Agent.Controller.Data;
import AIExpose.Agent.Controller.EndpointHandlerListener;
import AIExpose.Agent.Dtos.EndpointCache;
import AIExpose.Agent.Stub.GrpcConnectionManager;
import jakarta.servlet.ServletContext;
import net.devh.boot.grpc.server.security.authentication.BasicGrpcAuthenticationReader;
import net.devh.boot.grpc.server.security.authentication.GrpcAuthenticationReader;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@Configuration
@AutoConfiguration
public class EndpointAutoConfiguration {

    @Bean
    public EndpointCache endpointCache() {
        return new EndpointCache();
    }

    @Bean
    public EndpointBuilder endpointBuilder(Environment env, ServletContext servletContext) {
        return new EndpointBuilder(env, servletContext);
    }

    @Bean
    public EndpointScanner endpointScanner(EndpointBuilder endpointBuilder) {
        return new EndpointScanner(endpointBuilder);
    }

    @Bean
    @ConditionalOnClass(GrpcAuthenticationReader.class)
    @ConditionalOnMissingBean(GrpcAuthenticationReader.class)
    public GrpcAuthenticationReader grpcAuthenticationReader() {
        return new BasicGrpcAuthenticationReader();
    }


    @Bean
    public EndpointHandlerListener endpointHandlerListener(
            EndpointCache cache,
            EndpointScanner scanner,
            RequestMappingHandlerMapping handlerMapping
    ) {
        return new EndpointHandlerListener(cache, scanner, handlerMapping);
    }

    @Bean
    @ConditionalOnMissingBean(Data.class)
    public Data dataController(GrpcConnectionManager grpcConnectionManager,
                               EndpointCache cache) {
        return new Data(grpcConnectionManager, cache);
    }

    @Bean
    @ConditionalOnMissingBean(GrpcConnectionManager.class)
    public GrpcConnectionManager grpcConnectionManager(){
        return new GrpcConnectionManager();
    }
}
