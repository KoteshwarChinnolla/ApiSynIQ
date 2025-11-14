package AIExpose.Agent.Controller;

import AIExpose.Agent.AIExposeEp.EndpointScanner;
import AIExpose.Agent.Annotations.*;
import AIExpose.Agent.Dtos.EndpointData;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import AIExpose.Agent.Stub.GrpcConnectionManager;
import com.apisyniq.grpc.ControllerGrpc;
import com.apisyniq.grpc.query;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;


@Controller
public class Data {
    private final ApplicationContext applicationContext;
    private final EndpointScanner endpointScanner;
    private final GrpcConnectionManager grpcConnectionManager;
    @Autowired
    public Data(ApplicationContext applicationContext,
                EndpointScanner endpointScanner, GrpcConnectionManager grpcConnectionManager) {
        this.applicationContext = applicationContext;
        this.endpointScanner = endpointScanner;
        this.grpcConnectionManager = grpcConnectionManager;
    }

    @RequestMapping("/endpoints-info")
    @ResponseBody
    public Map<String, List<Map<String, EndpointData>>> getEndpointsInformation() throws JsonProcessingException {
        Map<String, List<Map<String, EndpointData>>> groupedEndpoints = new HashMap<>();
        String[] beanNames = applicationContext.getBeanDefinitionNames();

        for (String beanName : beanNames) {
            Object bean = applicationContext.getBean(beanName);
            RestController RestControllerAnnotation = bean.getClass().getAnnotation(RestController.class);
            AIExposeController aiExposeController = bean.getClass().getAnnotation(AIExposeController.class);

            if (RestControllerAnnotation != null) {
                String controllerName = bean.getClass().getSimpleName();
                List<Map<String, EndpointData>> endpoints = new ArrayList<>();

                for (Method method : bean.getClass().getDeclaredMethods()) {
                    AIExposeEpHttp epAnnotation = method.getAnnotation(AIExposeEpHttp.class);
                    EndpointData schema = endpointScanner.before(method, epAnnotation, aiExposeController);

                    Map<String, EndpointData> entityMap = new HashMap<>();
                    entityMap.put(method.getName(), schema);
                    endpoints.add(entityMap);
                }

                groupedEndpoints.put(controllerName, endpoints);
            }
        }

        return groupedEndpoints;
    }

    @RequestMapping(value = "/connect", method = RequestMethod.GET)
    @ResponseBody
    public Boolean connect() throws JsonProcessingException {
        grpcConnectionManager.connect();
        return grpcConnectionManager.isConnected();
    }


    @RequestMapping(value = "/saveAll", method = RequestMethod.GET)
    @ResponseBody
    public String saveAll() throws JsonProcessingException {
        String[] beanNames = applicationContext.getBeanDefinitionNames();
        ControllerGrpc.ControllerBlockingStub stub = grpcConnectionManager.getStub();
        for (String beanName : beanNames) {
            Object bean = applicationContext.getBean(beanName);
            RestController RestControllerAnnotation = bean.getClass().getAnnotation(RestController.class);
            AIExposeController aiExposeController = bean.getClass().getAnnotation(AIExposeController.class);
            if (RestControllerAnnotation != null) {
                String controllerName = bean.getClass().getSimpleName();
                for (Method method : bean.getClass().getDeclaredMethods()) {
                    AIExposeEpHttp epAnnotation = method.getAnnotation(AIExposeEpHttp.class);
                    EndpointData schema = endpointScanner.before(method, epAnnotation, aiExposeController);

                    query query = stub.save(schema.toGrpcEndpointData());
                    System.out.println(query);
                }
            }
        }
        return "Done with saving";
    }


}
