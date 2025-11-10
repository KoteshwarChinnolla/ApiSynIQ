package AIExpose.Agent.ParamScanner;

import AIExpose.Agent.AIExposeEp.TypeResolver;
import AIExpose.Agent.Annotations.AIExposeEpHttp;
import AIExpose.Agent.Dtos.Describe;
import AIExpose.Agent.Dtos.Inputs;
import AIExpose.Agent.enums.ParamType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class Factory {
    public static Map<String, Inputs> generateCombinedSchema(
            Method method,
            Map<String, Describe> describeDtosForParms) throws JsonProcessingException {

        Inputs inputsDtoActual = new Inputs();
        Inputs inputsDtoDescribe = new Inputs();
        ObjectMapper mapper = JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .build();

        AIExposeEpHttp aiExposeEpHttp = method.getAnnotation(AIExposeEpHttp.class);

        for (Parameter parameter : method.getParameters()) {
            Class<?> paramType = parameter.getType();
            Type paramGenericType = parameter.getParameterizedType();
            String paramName = parameter.getName();

            // ✅ PATH PARAM
            if (parameter.isAnnotationPresent(PathVariable.class)) {
                PathVariable pv = parameter.getAnnotation(PathVariable.class);
                String name = !pv.value().isEmpty() ? pv.value() : paramName;

                // --- Describe ---
                Describe pathParams = AIExpose.Agent.ParamScanner.Describe.getDescribedParams(parameter, aiExposeEpHttp, ParamType.PATH_PARAM);
                describeDtosForParms.put(name, pathParams);
                inputsDtoDescribe.getInputPathParams().put(name, paramType.getName());

                // --- Actual ---
                inputsDtoActual.getInputPathParams().put(name, mapper.writeValueAsString(Actual.sampleValue(paramType)));
            }

            // ✅ REQUEST PARAM
            else if (parameter.isAnnotationPresent(RequestParam.class)) {
                RequestParam rp = parameter.getAnnotation(RequestParam.class);
                String name = !rp.value().isEmpty() ? rp.value() : paramName;

                // --- Describe ---
                Describe reqParams = AIExpose.Agent.ParamScanner.Describe.getDescribedParams(parameter, aiExposeEpHttp, ParamType.REQUEST_PARAM);
                describeDtosForParms.put(name, reqParams);
                inputsDtoDescribe.getInputQueryParams().put(name, paramType.getName());

                // --- Actual ---
                inputsDtoActual.getInputQueryParams().put(name, mapper.writeValueAsString(Actual.sampleValue(paramType)));
            }

            // ✅ REQUEST BODY
            else if (parameter.isAnnotationPresent(RequestBody.class)) {
                // --- Describe ---
                Type genericType = parameter.getParameterizedType();
                Object describedType = TypeResolver.describeType(genericType);
                inputsDtoDescribe.getInputBody().put("requestBody", mapper.writeValueAsString(describedType));

                // --- Actual ---
                inputsDtoActual.getInputBody().put("requestBody",
                        mapper.writeValueAsString(Actual.generateClassSchema(paramGenericType, new HashMap<>(), 0)));
            }

            // ✅ REQUEST HEADER
            else if (parameter.isAnnotationPresent(RequestHeader.class)) {
                RequestHeader rh = parameter.getAnnotation(RequestHeader.class);
                String name = !rh.value().isEmpty() ? rh.value() : paramName;

                // --- Describe ---
                Describe headerParams = AIExpose.Agent.ParamScanner.Describe.getDescribedParams(parameter, aiExposeEpHttp, ParamType.REQUEST_HEADER);
                describeDtosForParms.put(name, headerParams);
                inputsDtoDescribe.getInputHeaders().put(name, paramType.getName());

                // --- Actual ---
                inputsDtoActual.getInputHeaders().put(name, mapper.writeValueAsString(Actual.sampleValue(paramType)));
            }

            // ✅ VARIABLE (UNANNOTATED PARAMETERS, CUSTOM DTOS)
            else if (!parameter.isAnnotationPresent(RequestBody.class)
                    && !parameter.isAnnotationPresent(PathVariable.class)
                    && !parameter.isAnnotationPresent(RequestParam.class)
                    && !parameter.isAnnotationPresent(RequestHeader.class)) {

                if (TypeResolver.isSimpleType(paramType)) {
                    // --- Describe ---
                    Describe d = AIExpose.Agent.ParamScanner.Describe.paramsToDescribe(parameter, ParamType.VARIABLE);
                    describeDtosForParms.put(paramName, d);
                    inputsDtoDescribe.getInputVariables().put(paramName, paramType.getName());

                    // --- Actual ---
                    inputsDtoActual.getInputVariables().put(paramName, mapper.writeValueAsString(Actual.sampleValue(paramType)));
                } else {
                    // --- Describe ---
                    inputsDtoDescribe.getInputVariables().put(paramName, paramType.getSimpleName());

                    // --- Actual ---
                    inputsDtoActual.getInputVariables().put(paramName,
                            mapper.writeValueAsString(Actual.generateClassSchema(paramGenericType, new HashMap<>(), 0)));
                }
            }
        }

        Map<String, Inputs> result = new HashMap<>();
        result.put("describe", inputsDtoDescribe);
        result.put("actual", inputsDtoActual);
        return result;
    }

}
