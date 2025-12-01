package AIExpose.Agent.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import com.fasterxml.jackson.core.JsonProcessingException;

import AIExpose.Agent.AIExposeEp.EndpointScanner;
import AIExpose.Agent.Annotations.AIExposeController;
import AIExpose.Agent.Annotations.AIExposeEpHttp;
import AIExpose.Agent.Dtos.EndpointCache;
import AIExpose.Agent.Dtos.EndpointData;

import java.lang.reflect.Method;
import java.util.Map;

public class EndpointHandlerListener implements ApplicationListener<ContextRefreshedEvent> {

    private final EndpointCache cache;
    private final EndpointScanner scanner;
    private final RequestMappingHandlerMapping handlerMapping;

    public EndpointHandlerListener(EndpointCache cache,
                                   EndpointScanner scanner,
                                   RequestMappingHandlerMapping handlerMapping) {
        this.cache = cache;
        this.scanner = scanner;
        this.handlerMapping = handlerMapping;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {

        handlerMapping.getHandlerMethods().forEach((info, handlerMethod) -> {
            Class<?> controllerClass = handlerMethod.getBeanType();
            Method method = handlerMethod.getMethod();

            AIExposeController cAnn = controllerClass.getAnnotation(AIExposeController.class);
            AIExposeEpHttp eAnn = method.getAnnotation(AIExposeEpHttp.class);

            if (cAnn != null || eAnn != null) {
                try {
                    EndpointData data = scanner.before(method, eAnn, cAnn);
                    cache.add(controllerClass.getSimpleName(), method.getName(), data);
                    cache.getGrpc(data);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}


