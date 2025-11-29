package AIExpose.Agent.AIExposeEp;

import java.lang.reflect.*;
import java.util.*;


import AIExpose.Agent.ParamScanner.Actual;
import AIExpose.Agent.ParamScanner.Factory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import AIExpose.Agent.AIExposeDto.AIExposeDtoAspect;
import AIExpose.Agent.Dtos.Describe;
import AIExpose.Agent.Dtos.DtoSchema;
import AIExpose.Agent.Dtos.Inputs;
import AIExpose.Agent.Dtos.EndpointData;
import AIExpose.Agent.Annotations.*;
//import AIExpose.Agent.Utils.DescribeSchemaGenerator;
//import AIExpose.Agent.Utils.ParamSchemaGenerator;
import AIExpose.Agent.enums.ParamType;

public class EndpointScanner {

    private final EndpointBuilder endpointBuilder;

    public EndpointScanner(EndpointBuilder endpointBuilder) {
        this.endpointBuilder = endpointBuilder;
    }
    @Value("${aiExpose.global.path:}")
    private String globalPath;

    public EndpointData before(Method method, AIExposeEpHttp aiExposeEpHttp, AIExposeController aiExposeController) throws JsonProcessingException {
        String name = method.getName();
        String des = "";
        if(aiExposeController != null){
            des = aiExposeController.description().isEmpty() ? "" : aiExposeController.description();
        }
        String[] tags = new String[]{};
        List<String> filteringTags = new ArrayList<>();
        if (aiExposeEpHttp == null && aiExposeController == null) {
            filteringTags = Arrays.asList("All");
        } else if (aiExposeEpHttp != null && aiExposeController == null) {
            filteringTags = Arrays.asList("all", "aiExposeEpHttp");   
        } else if (aiExposeEpHttp == null && aiExposeController != null) {
            filteringTags = Arrays.asList("all", "aiExposeController");
        } else {
            filteringTags = Arrays.asList("all", "aiExposeController", "aiExposeEpHttp");
        }
        String endpoint = endpointBuilder.makeEndpoint(method.getDeclaringClass(), method);
        String description = des.isEmpty() ? "No description provided." : des;
        Boolean autoExecute = true;
        String returnDescription = "No return description provided.";
        // ControllerSchema controllerSchema = new ControllerSchema();
        EndpointData controllerSchema = new EndpointData();
        if (aiExposeEpHttp != null) {
            controllerSchema.setName(!aiExposeEpHttp.name().isEmpty() ? aiExposeEpHttp.name() : name);
            controllerSchema.setEndpoint(!aiExposeEpHttp.example().isEmpty() ? aiExposeEpHttp.example() : endpoint);
            controllerSchema.setHttpMethod(endpointBuilder.getHttpMethod(method));
            controllerSchema.setTags(aiExposeEpHttp.tags().length > 0 ?  aiExposeEpHttp.tags() : tags);
            controllerSchema.setDescription(!aiExposeEpHttp.description().isEmpty() ? aiExposeEpHttp.description() : description);
            controllerSchema.setAutoExecute(aiExposeEpHttp.autoExecute() || autoExecute);
            controllerSchema.setResponseBody(String.valueOf(TypeResolver.describeType(TypeResolver.resolveActualReturnType(method))));
            controllerSchema.setReturnDescription(!aiExposeEpHttp.returnDescription().isEmpty() ? aiExposeEpHttp.returnDescription() : returnDescription);
        }
        else {
            controllerSchema.setName(name);
            controllerSchema.setEndpoint(endpoint);
            controllerSchema.setHttpMethod(endpointBuilder.getHttpMethod(method));
            controllerSchema.setTags(tags);
            controllerSchema.setDescription(description);
            controllerSchema.setAutoExecute(true);
            controllerSchema.setResponseBody(String.valueOf(TypeResolver.describeType(TypeResolver.resolveActualReturnType(method))));
            controllerSchema.setReturnDescription(returnDescription);
        }
        Object outputBody = Actual.generateClassSchema(TypeResolver.resolveActualReturnType(method), new HashMap<>(), 0);
        ObjectMapper mapper = JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .build();
        Map<String, AIExpose.Agent.Dtos.Describe> describeDtosForParms = new HashMap<>();
        Map<String, Inputs> inputsMap = Factory.generateCombinedSchema(method, describeDtosForParms);
        controllerSchema.setOutputBody(mapper.writeValueAsString(outputBody));
//        Inputs inputs = ParamSchemaGenerator.generateMethodParamSchema(method);
        Inputs inputs = inputsMap.get("actual");
        Inputs inputsDescribe = inputsMap.get("describe");
//        Inputs inputsDescribe = DescribeSchemaGenerator.generateDescribeSchema(method, describeDtosForParms);
        controllerSchema.setDescribeDtosForParms(describeDtosForParms);
        controllerSchema.setInputsDescribe(inputsDescribe);
        controllerSchema.setInputs(inputs);
        controllerSchema.setFilteringTags(filteringTags);
        controllerSchema.setDtoSchemas(AIExposeDtoAspect.DescribedDtosForMethods(method));
        controllerSchema.setGlobalPath(globalPath==null?"":globalPath);
        return controllerSchema;
    }


    public static List<DtoSchema> buildRequestBodySchemas(Method method) {
        List<DtoSchema> reqBodySchemas = new ArrayList<>();

        for (Parameter parameter : method.getParameters()) {
            
            if (parameter.isAnnotationPresent(RequestBody.class)) {
                Class<?> actualType = TypeResolver.resolveActualType(parameter);
                DtoSchema schema = AIExposeDtoAspect.scan(actualType);
                reqBodySchemas.add(schema);
            }
        }
        return reqBodySchemas;
    }

    public static List<AIExpose.Agent.Dtos.Describe> paramsToDescribe(Parameter[] params, ParamType type) {
        List<Describe> describeList = new ArrayList<>();
        for (Parameter parameter : params) {
            // skip if annotated with RequestBody or Describe
            if (parameter.isAnnotationPresent(RequestBody.class)) continue;
            if (parameter.isAnnotationPresent(AIExpose.Agent.Annotations.Describe.class)) continue;

            // only process parameters based on annotation type
            if (parameter.isAnnotationPresent(PathVariable.class) && type != ParamType.PATH_PARAM) continue;
            if (parameter.isAnnotationPresent(RequestParam.class) && type != ParamType.REQUEST_PARAM) continue;

            AIExpose.Agent.Dtos.Describe describe = new Describe();
            describe.setName(parameter.getName());
            describe.setDataType(parameter.getType().getSimpleName());
            describe.setAutoExecute(true);
            describe.setExample("No example provided.");
            describeList.add(describe);
        }

        return describeList;
    }


    public static List<AIExpose.Agent.Dtos.Describe> annotationToDescribe(AIExpose.Agent.Annotations.Describe[] pathParams) {
        List<AIExpose.Agent.Dtos.Describe> describeList = new ArrayList<>();
        for (AIExpose.Agent.Annotations.Describe describe : pathParams) {
            AIExpose.Agent.Dtos.Describe dto = new AIExpose.Agent.Dtos.Describe();
            dto.setName(describe.name());
            dto.setDescription(describe.description());
            dto.setDataType(describe.dataType());
            dto.setDefaultValue(describe.defaultValue());
            dto.setOptions(describe.options());
            dto.setAutoExecute(describe.autoExecute());
            dto.setExample(describe.example());
            describeList.add(dto);
        }
        return describeList;
    }
}
