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
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import AIExpose.Agent.AIExposeDto.AIExposeDtoAspect;
import AIExpose.Agent.Dtos.DescribeDto;
import AIExpose.Agent.Dtos.DtoSchema;
import AIExpose.Agent.Dtos.InputsDto;
import AIExpose.Agent.Dtos.OutputData;
import AIExpose.Agent.Annotations.*;
//import AIExpose.Agent.Utils.DescribeSchemaGenerator;
//import AIExpose.Agent.Utils.ParamSchemaGenerator;
import AIExpose.Agent.enums.ParamType;


@Component
public class EndpointScanner {

    @Autowired
    private EndpointBuilder endpointBuilder;

    public OutputData before(Method method, AIExposeEpHttp aiExposeEpHttp, AIExposeController aiExposeController) throws JsonProcessingException {
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
        OutputData controllerSchema = new OutputData();
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
        Map<String, DescribeDto> describeDtosForParms = new HashMap<>();
        Map<String, InputsDto> inputsMap = Factory.generateCombinedSchema(method, describeDtosForParms);
        controllerSchema.setOutputBody(mapper.writeValueAsString(outputBody));
//        InputsDto inputs = ParamSchemaGenerator.generateMethodParamSchema(method);
        InputsDto inputs = inputsMap.get("actual");
        InputsDto inputsDescribe = inputsMap.get("describe");
//        InputsDto inputsDescribe = DescribeSchemaGenerator.generateDescribeSchema(method, describeDtosForParms);
        controllerSchema.setDescribeDtosForParms(describeDtosForParms);
        controllerSchema.setInputsDescribe(inputsDescribe);
        controllerSchema.setInputs(inputs);
        controllerSchema.setFilteringTags(filteringTags);
        controllerSchema.setDtoSchemas(AIExposeDtoAspect.DescribedDtosForMethods(method));
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

    public static List<DescribeDto> paramsToDescribe(Parameter[] params, ParamType type) {
        List<DescribeDto> describeList = new ArrayList<>();
        for (Parameter parameter : params) {
            // skip if annotated with RequestBody or Describe
            if (parameter.isAnnotationPresent(RequestBody.class)) continue;
            if (parameter.isAnnotationPresent(Describe.class)) continue;

            // only process parameters based on annotation type
            if (parameter.isAnnotationPresent(PathVariable.class) && type != ParamType.PATH_PARAM) continue;
            if (parameter.isAnnotationPresent(RequestParam.class) && type != ParamType.REQUEST_PARAM) continue;

            DescribeDto describe = new DescribeDto();
            describe.setName(parameter.getName());
            describe.setDataType(parameter.getType().getSimpleName());
            describe.setAutoExecute(true);
            describe.setExample("No example provided.");
            describeList.add(describe);
        }

        return describeList;
    }


    public static List<DescribeDto> annotationToDescribe(Describe[] pathParams) {
        List<DescribeDto> describeList = new ArrayList<>();
        for (Describe describe : pathParams) {
            DescribeDto dto = new DescribeDto();
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
