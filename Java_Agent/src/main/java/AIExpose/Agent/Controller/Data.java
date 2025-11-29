package AIExpose.Agent.Controller;

// import AIExpose.Agent.AIExposeEp.EndpointScanner;
// import AIExpose.Agent.Annotations.*;
// import AIExpose.Agent.Dtos.EndpointData;

// import java.lang.reflect.Method;
// import java.util.ArrayList;
// import java.util.HashMap;
// import java.util.List;
// import java.util.Map;


// import AIExpose.Agent.Stub.GrpcConnectionManager;
// import lombok.AllArgsConstructor;
// import lombok.NoArgsConstructor;
// import lombok.RequiredArgsConstructor;

// import com.apisyniq.grpc.ControllerGrpc;
// import com.apisyniq.grpc.query;
// import com.apisyniq.grpc.repeatedInput;
// import com.fasterxml.jackson.core.JsonProcessingException;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.context.ApplicationContext;
// import org.springframework.stereotype.Controller;
// import org.springframework.web.bind.annotation.*;


// @Controller
// @RequiredArgsConstructor
// public class Data {
//     private final ApplicationContext applicationContext;
//     private final EndpointScanner endpointScanner;
//     private final GrpcConnectionManager grpcConnectionManager;


//     @RequestMapping("/endpoints-info")
//     @ResponseBody
//     public Map<String, List<Map<String, EndpointData>>> getEndpointsInformation() throws JsonProcessingException {
//         Map<String, List<Map<String, EndpointData>>> groupedEndpoints = new HashMap<>();
//         String[] beanNames = applicationContext.getBeanDefinitionNames();

//         for (String beanName : beanNames) {
//             Object bean = applicationContext.getBean(beanName);
//             RestController RestControllerAnnotation = bean.getClass().getAnnotation(RestController.class);
//             AIExposeController aiExposeController = bean.getClass().getAnnotation(AIExposeController.class);

//             if (RestControllerAnnotation != null) {
//                 String controllerName = bean.getClass().getSimpleName();
//                 List<Map<String, EndpointData>> endpoints = new ArrayList<>();

//                 for (Method method : bean.getClass().getDeclaredMethods()) {
//                     AIExposeEpHttp epAnnotation = method.getAnnotation(AIExposeEpHttp.class);
//                     EndpointData schema = endpointScanner.before(method, epAnnotation, aiExposeController);

//                     Map<String, EndpointData> entityMap = new HashMap<>();
//                     entityMap.put(method.getName(), schema);
//                     endpoints.add(entityMap);
//                 }

//                 groupedEndpoints.put(controllerName, endpoints);
//             }
//         }

//         return groupedEndpoints;
//     }

//     @RequestMapping(value = "/connect", method = RequestMethod.GET)
//     @ResponseBody
//     public Boolean connect() throws JsonProcessingException {
//         grpcConnectionManager.connect();
//         return grpcConnectionManager.isConnected();
//     }


//     @RequestMapping(value = "/saveAll", method = RequestMethod.GET)
//     @ResponseBody
//     public String saveAll() throws JsonProcessingException {
//         String[] beanNames = applicationContext.getBeanDefinitionNames();
//         ControllerGrpc.ControllerBlockingStub stub = grpcConnectionManager.getStub();
//         List<com.apisyniq.grpc.EndpointData>  endpoints = new ArrayList<>();
//         for (String beanName : beanNames) {
//             Object bean = applicationContext.getBean(beanName);
//             RestController RestControllerAnnotation = bean.getClass().getAnnotation(RestController.class);
//             AIExposeController aiExposeController = bean.getClass().getAnnotation(AIExposeController.class);
//             if (RestControllerAnnotation != null) {
//                 String controllerName = bean.getClass().getSimpleName();
//                 for (Method method : bean.getClass().getDeclaredMethods()) {
//                     AIExposeEpHttp epAnnotation = method.getAnnotation(AIExposeEpHttp.class);
//                     EndpointData schema = endpointScanner.before(method, epAnnotation, aiExposeController);
//                     endpoints.add(schema.toGrpcEndpointData());
// //                    query query = stub.save(schema.toGrpcEndpointData());
// //                    System.out.println(query);
//                 }
//             }
//         }
//         repeatedInput r = repeatedInput.newBuilder().addAllInputs(endpoints).build();
//         System.out.println(endpoints);
//         query query = stub.saveAll(r);
//         return "Done with saving";
//     }
// }



import com.apisyniq.grpc.ControllerGrpc;
import com.apisyniq.grpc.repeatedInput;

import AIExpose.Agent.AIExposeEp.EndpointScanner;
import AIExpose.Agent.Dtos.EndpointCache;
import AIExpose.Agent.Dtos.EndpointData;
import AIExpose.Agent.Stub.GrpcConnectionManager;

import com.apisyniq.grpc.query;
import lombok.RequiredArgsConstructor;

import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class Data {

    private final EndpointCache cache;
    private final GrpcConnectionManager grpcConnectionManager;

    // ====== 1. SHOW ENDPOINT DATA AS JSON ======
    @GetMapping("/endpoints-info")
    public Map<String, List<Map<String, EndpointData>>> getEndpointsInformation() {

        Map<String, List<Map<String, EndpointData>>> grouped = new HashMap<>();

        for (EndpointData data : cache.all()) {

            String controllerName = data.getClass().toString();

            grouped.putIfAbsent(controllerName, new ArrayList<>());

            Map<String, EndpointData> methodMap = new HashMap<>();
            methodMap.put(data.getName(), data);

            grouped.get(controllerName).add(methodMap);
        }

        return grouped;
    }

    // ====== 2. CONNECT ======
    @GetMapping("/connect")
    public Boolean connect() {
        grpcConnectionManager.connect();
        return grpcConnectionManager.isConnected();
    }

    // ====== 3. SAVE ALL ======
    @GetMapping("/saveAll")
    public String saveAll() {

        ControllerGrpc.ControllerBlockingStub stub = grpcConnectionManager.getStub();

        List<com.apisyniq.grpc.EndpointData> grpcEndpoints =
                cache.all()
                     .stream()
                     .map(EndpointData::toGrpcEndpointData)
                     .collect(Collectors.toList());

        repeatedInput packet =repeatedInput.newBuilder().addAllInputs(grpcEndpoints).build();

        query q = stub.saveAll(packet);

        return "Done with saving";
    }
}
