package AIExpose.Agent.AIExposeEp;

import java.lang.reflect.*;
import java.util.*;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import AIExpose.Agent.AIExposeDto.AIExposeDtoAspect;
import AIExpose.Agent.Dtos.DescribeDto;
import AIExpose.Agent.Dtos.DtoSchema;
import AIExpose.Agent.Dtos.InputsDto;
import AIExpose.Agent.Dtos.ControllerSchema;
import AIExpose.Agent.Annotations.*;
import AIExpose.Agent.Utils.EndpointBuilder;
import AIExpose.Agent.Utils.ParamSchemaGenerator;
import AIExpose.Agent.Utils.TypeResolver;
import AIExpose.Agent.enums.ParamType;

// @Aspect

@Component
public class AIExposeEPHttpAspect {

    @Autowired
    private AIExposeDtoAspect aiExposeDtoAspect;
    @Autowired
    private EndpointBuilder endpointBuilder;
    // @Before("@annotation(aiExposeEpHttp)")
    public ControllerSchema before(Method method, AIExposeEpHttp aiExposeEpHttp, AIExposeController aiExposeController) {
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
        ControllerSchema controllerSchema = new ControllerSchema();
        if (aiExposeEpHttp != null) {
            controllerSchema.setName(!aiExposeEpHttp.name().isEmpty() ? aiExposeEpHttp.name() : name);
            controllerSchema.setEndpoint(!aiExposeEpHttp.example().isEmpty() ? aiExposeEpHttp.example() : endpoint);
            controllerSchema.setHttpMethod(endpointBuilder.getHttpMethod(method));
            controllerSchema.setTags(aiExposeEpHttp.tags().length > 0 ?  aiExposeEpHttp.tags() : tags);
            controllerSchema.setDescription(!aiExposeEpHttp.description().isEmpty() ? aiExposeEpHttp.description() : description);
            controllerSchema.setAutoExecute(aiExposeEpHttp.autoExecute() || autoExecute);
            controllerSchema.setResponseBody(aiExposeDtoAspect.scan(TypeResolver.resolveActualReturnType(method)));
            controllerSchema.setRequestBody(buildRequestBodySchemas(method, aiExposeDtoAspect));
            controllerSchema.setReturnDescription(!aiExposeEpHttp.returnDescription().isEmpty() ? aiExposeEpHttp.returnDescription() : returnDescription);
            controllerSchema.setPathParams(
                annotationToDescribe(aiExposeEpHttp.pathParams()).isEmpty()
                    ? paramsToDescribe(method.getParameters(), ParamType.PATH_PARAM)
                    : annotationToDescribe(aiExposeEpHttp.pathParams())
            );

            controllerSchema.setReqParams(
                annotationToDescribe(aiExposeEpHttp.reqParams()).isEmpty()
                    ? paramsToDescribe(method.getParameters(), ParamType.REQUEST_PARAM)
                    : annotationToDescribe(aiExposeEpHttp.reqParams())
            );
        }
        else {
            controllerSchema.setName(name);
            controllerSchema.setEndpoint(endpoint);
            controllerSchema.setHttpMethod(endpointBuilder.getHttpMethod(method));
            controllerSchema.setTags(tags);
            controllerSchema.setDescription(description);
            controllerSchema.setAutoExecute(true);
            controllerSchema.setResponseBody(aiExposeDtoAspect.scan(TypeResolver.resolveActualReturnType(method)));
            controllerSchema.setRequestBody(buildRequestBodySchemas(method, aiExposeDtoAspect));
            controllerSchema.setReturnDescription(returnDescription);
            controllerSchema.setPathParams(paramsToDescribe(method.getParameters(), ParamType.PATH_PARAM));
            controllerSchema.setReqParams(paramsToDescribe(method.getParameters(), ParamType.REQUEST_PARAM));
        }
        Object outputBody = ParamSchemaGenerator.generateClassSchema(TypeResolver.resolveActualReturnType(method), new HashMap<>(), 0);
        controllerSchema.setOutputBody(outputBody);
        InputsDto inputs = inputs(method);
        controllerSchema.setInputs(inputs);
        controllerSchema.setFilteringTags(filteringTags);
        return controllerSchema;
    }

    public InputsDto inputs(Method method) {
        InputsDto inputs = new InputsDto();
        inputs.setInputBody(ParamSchemaGenerator.generateMethodParamSchema(method, ParamType.REQUEST_BODY));
        inputs.setInputPathParams(ParamSchemaGenerator.generateMethodParamSchema(method, ParamType.PATH_PARAM));
        inputs.setInputQueryParams(ParamSchemaGenerator.generateMethodParamSchema(method, ParamType.REQUEST_PARAM));
        inputs.setInputHeaders(ParamSchemaGenerator.generateMethodParamSchema(method, ParamType.REQUEST_HEADER));
        inputs.setInputVariables(ParamSchemaGenerator.generateMethodParamSchema(method, ParamType.VARIABLE));
        return inputs;
    }


    public static List<DtoSchema> buildRequestBodySchemas(Method method, AIExposeDtoAspect aiExposeDtoAspect) {
        List<DtoSchema> reqBodySchemas = new ArrayList<>();

        for (Parameter parameter : method.getParameters()) {
            
            if (parameter.isAnnotationPresent(RequestBody.class)) {
                Class<?> actualType = TypeResolver.resolveActualType(parameter);
                DtoSchema schema = aiExposeDtoAspect.scan(actualType);
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
