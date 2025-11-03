//package AIExpose.Agent.GrpcService;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.ApplicationContext;
//import org.springframework.grpc.server.service.GrpcService;
//import org.springframework.web.bind.annotation.RestController;
//
//import com.apisyniq.grpc.ControllerEndpoints;
//import com.apisyniq.grpc.ControllerMap;
//import com.apisyniq.grpc.Empty;
//import com.apisyniq.grpc.EndpointServiceGrpc;
//import com.apisyniq.grpc.EndpointWrapper;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.google.protobuf.util.JsonFormat;
//import com.google.protobuf.Struct;
//import com.google.protobuf.Value;
//
//import AIExpose.Agent.AIExposeEp.EndpointScanner;
//import AIExpose.Agent.Annotations.AIExposeController;
//import AIExpose.Agent.Annotations.AIExposeEpHttp;
//import AIExpose.Agent.Dtos.OutputData;
//import io.grpc.stub.StreamObserver;
//
//import java.lang.reflect.Method;
//import java.util.*;
//
//@GrpcService
//public class EndpointService extends EndpointServiceGrpc.EndpointServiceImplBase {
//    @Autowired
//    private ApplicationContext applicationContext;
//    @Autowired
//    private EndpointScanner endpointScanner;
//
//    @Override
//    public void getEndpointsInformation(Empty request,
//            StreamObserver<ControllerMap> responseObserver) throws JsonProcessingException {
//        Map<String, List<Map<String, OutputData>>> groupedEndpoints = new HashMap<>();
//        String[] beanNames = applicationContext.getBeanDefinitionNames();
//
//        for (String beanName : beanNames) {
//            Object bean = applicationContext.getBean(beanName);
//            RestController restControllerAnnotation = bean.getClass().getAnnotation(RestController.class);
//            AIExposeController aiExposeController = bean.getClass().getAnnotation(AIExposeController.class);
//
//            if (restControllerAnnotation != null) {
//                String controllerName = bean.getClass().getSimpleName();
//                List<Map<String, OutputData>> endpoints = new ArrayList<>();
//
//                for (Method method : bean.getClass().getDeclaredMethods()) {
//                    AIExposeEpHttp epAnnotation = method.getAnnotation(AIExposeEpHttp.class);
//                    OutputData schema = endpointScanner.before(method, epAnnotation, aiExposeController);
//
//                    Map<String, OutputData> entityMap = new HashMap<>();
//                    entityMap.put(method.getName(), schema);
//                    endpoints.add(entityMap);
//                }
//
//                groupedEndpoints.put(controllerName, endpoints);
//            }
//        }
//
//        ControllerMap.Builder controllerMapBuilder = ControllerMap.newBuilder();
//
//        groupedEndpoints.forEach((controllerName, endpointList) -> {
//            ControllerEndpoints.Builder controllerEndpointsBuilder = ControllerEndpoints.newBuilder();
//
//            for (Map<String, OutputData> endpointMap : endpointList) {
//                try {
//                    // Extract method name (key)
//                    String key = endpointMap.keySet().iterator().next();
//                    OutputData outputData = endpointMap.get(key);
//
//                    // ✅ Convert OutputData → JSON
//                    String valueJson = new ObjectMapper().writeValueAsString(outputData);
//
//                    // ✅ Parse JSON → Struct (preserves real field structure)
//                    Struct.Builder structBuilder = Struct.newBuilder();
//                    JsonFormat.parser().merge(valueJson, structBuilder);
//
//                    // ✅ Optionally print for debugging (shows clean JSON)
//                    System.out.println("=== Clean JSON Struct for " + key + " ===");
//                    System.out.println(JsonFormat.printer().includingDefaultValueFields().print(structBuilder));
//
//                    // ✅ Add to wrapper
//                    EndpointWrapper.Builder wrapperBuilder = EndpointWrapper.newBuilder()
//                            .putEndpoints(key, structBuilder.build());
//
//                    controllerEndpointsBuilder.addEndpointList(wrapperBuilder.build());
//
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//
//            controllerMapBuilder.putControllers(controllerName, controllerEndpointsBuilder.build());
//        });
//
//        ControllerMap controllerMap = controllerMapBuilder.build();
//        responseObserver.onNext(controllerMap);
//        responseObserver.onCompleted();
//    }
//
//}
