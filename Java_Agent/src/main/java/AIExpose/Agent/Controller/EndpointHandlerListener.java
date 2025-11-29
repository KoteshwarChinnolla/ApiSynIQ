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

@Component
public class EndpointHandlerListener implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    private RequestMappingHandlerMapping handlerMapping;

    @Autowired
    private EndpointCache cache;

    @Autowired
    private EndpointScanner scanner;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {

        Map<RequestMappingInfo, HandlerMethod> handlers = handlerMapping.getHandlerMethods();

        handlers.forEach((info, handlerMethod) -> {

            Class<?> controllerClass = handlerMethod.getBeanType();
            Method endpointMethod = handlerMethod.getMethod();

            AIExposeController controllerAnn =
                    controllerClass.getAnnotation(AIExposeController.class);

            AIExposeEpHttp endpointAnn =
                    endpointMethod.getAnnotation(AIExposeEpHttp.class);

            if (controllerAnn != null || endpointAnn != null) {

                EndpointData data;
                try {
                  data = scanner.before(endpointMethod, endpointAnn, controllerAnn);
                  cache.add(data);
                } catch (JsonProcessingException e) {
                  e.printStackTrace();
                }

            }
        });
        System.out.print(cache);
    }
}

