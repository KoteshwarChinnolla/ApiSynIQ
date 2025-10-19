package AIExpose.Agent.Utils;

import AIExpose.Agent.Annotations.*;
import AIExpose.Agent.Dtos.DescribeDto;
import AIExpose.Agent.Dtos.InputsDto;
import AIExpose.Agent.enums.ParamType;

import org.springframework.web.bind.annotation.*;

import java.lang.reflect.*;

public class DescribeSchemaGenerator {

    /**
     * Generates a map of described schemas based on the method parameters
     * and provided ParamType (PATH_PARAM, REQUEST_PARAM, etc.).
     */
    public static InputsDto generateDescribeSchema(Method method) {
        InputsDto inputsDto = new InputsDto();
        AIExposeEpHttp aiExposeEpHttp = method.getAnnotation(AIExposeEpHttp.class) != null
                ? method.getAnnotation(AIExposeEpHttp.class)
                : null;

        for (Parameter parameter : method.getParameters()) {
            Class<?> paramType = parameter.getType();
            String paramName = parameter.getName();

            // ✅ PATH PARAMETER
            if (parameter.isAnnotationPresent(PathVariable.class)) {
                PathVariable pv = parameter.getAnnotation(PathVariable.class);
                String name = !pv.value().isEmpty() ? pv.value() : paramName;
                DescribeDto pathParams = getDescribedParams(parameter, aiExposeEpHttp, ParamType.PATH_PARAM);
                inputsDto.getInputPathParams().put(name, pathParams);
            }

            // ✅ REQUEST PARAMETER
            else if (parameter.isAnnotationPresent(RequestParam.class)) {
                RequestParam rp = parameter.getAnnotation(RequestParam.class);
                String name = !rp.value().isEmpty() ? rp.value() : paramName;
                DescribeDto reqParams = getDescribedParams(parameter, aiExposeEpHttp, ParamType.REQUEST_PARAM);
                inputsDto.getInputQueryParams().put(name, reqParams);
            }

            // ✅ REQUEST BODY
            else if (parameter.isAnnotationPresent(RequestBody.class)) {
                Type genericType = parameter.getParameterizedType();
                Object describedType = TypeResolver.describeType(genericType);

                inputsDto.getInputBody().put("requestBody", describedType);
            }

            // ✅ REQUEST HEADER
            else if (parameter.isAnnotationPresent(RequestHeader.class)) {
                RequestHeader rh = parameter.getAnnotation(RequestHeader.class);
                String name = !rh.value().isEmpty() ? rh.value() : paramName;
                DescribeDto headerParams = getDescribedParams(parameter, aiExposeEpHttp, ParamType.REQUEST_HEADER);
                inputsDto.getInputHeaders().put(name, headerParams);
            }

            // ✅ VARIABLE (UNANNOTATED PARAMETERS, CUSTOM DTOS)
            else if (!parameter.isAnnotationPresent(RequestBody.class) &&
                    !parameter.isAnnotationPresent(PathVariable.class) &&
                    !parameter.isAnnotationPresent(RequestParam.class) &&
                    !parameter.isAnnotationPresent(RequestHeader.class)) {

                if (ParamSchemaGenerator.isSimpleType(paramType)) {
                    inputsDto.getInputVariables().put(paramName, paramsToDescribe(parameter, ParamType.VARIABLE));
                } else {
                    // Store only reference, details can be resolved by DTOCollector
                    inputsDto.getInputVariables().put(paramName, paramType.getSimpleName());
                }
            }
        }

        return inputsDto;
    }

    /**
     * Selects the appropriate annotation data or generates description from
     * parameters.
     */
    private static DescribeDto getDescribedParams(Parameter parameter, AIExposeEpHttp aiExposeEpHttp, ParamType type) {
        if (aiExposeEpHttp == null) {
            return paramsToDescribe(parameter, type);
        }

        Describe[] annotationArr = switch (type) {
            case PATH_PARAM -> aiExposeEpHttp.pathParams();
            case REQUEST_PARAM -> aiExposeEpHttp.reqParams();
            case REQUEST_HEADER -> aiExposeEpHttp.headers();
            default -> new Describe[0];
        };

        for( Describe d : annotationArr) {
            if (d.name() == type.name()) {
                return annotationToDescribe(d);
            }
        } 
        return paramsToDescribe(parameter, type);

        
    }

    /**
     * Generates DescribeDto list from method parameters.
     */
    public static DescribeDto paramsToDescribe(Parameter parameter, ParamType type) {

        DescribeDto describe = new DescribeDto();
        describe.setName(parameter.getName());
        describe.setDataType(parameter.getType().getSimpleName());
        describe.setAutoExecute(true);
        describe.setExample("No example provided.");

        return describe;
    }

    /**
     * Converts @Describe annotations into DescribeDto objects.
     */
    public static DescribeDto annotationToDescribe(Describe describe) {
        DescribeDto dto = new DescribeDto();
        dto.setName(describe.name());
        dto.setDescription(describe.description());
        dto.setDataType(describe.dataType());
        dto.setDefaultValue(describe.defaultValue());
        dto.setOptions(describe.options());
        dto.setAutoExecute(describe.autoExecute());
        dto.setExample(describe.example());

        return dto;
    }
}
