package AIExpose.Agent.ParamScanner;

import AIExpose.Agent.Annotations.AIExposeEpHttp;
import AIExpose.Agent.enums.ParamType;

import java.lang.reflect.Parameter;

public class Describe extends Factory {
    protected static AIExpose.Agent.Dtos.Describe getDescribedParams(Parameter parameter, AIExposeEpHttp aiExposeEpHttp, ParamType type) {
        if (aiExposeEpHttp == null) {
            return paramsToDescribe(parameter, type);
        }

        AIExpose.Agent.Annotations.Describe[] annotationArr = switch (type) {
            case PATH_PARAM -> aiExposeEpHttp.pathParams();
            case REQUEST_PARAM -> aiExposeEpHttp.reqParams();
            case REQUEST_HEADER -> aiExposeEpHttp.headers();
            default -> new AIExpose.Agent.Annotations.Describe[0];
        };

        for( AIExpose.Agent.Annotations.Describe d : annotationArr) {
            if (d.name().equals(parameter.getName())) {
                return annotationToDescribe(d);
            }
        }
        return paramsToDescribe(parameter, type);


    }

    /**
     * Generates Describe list from method parameters.
     */
    public static AIExpose.Agent.Dtos.Describe paramsToDescribe(Parameter parameter, ParamType type) {

        AIExpose.Agent.Dtos.Describe describe = new AIExpose.Agent.Dtos.Describe();
        describe.setName(parameter.getName());
        describe.setDataType(parameter.getType().getSimpleName());
        describe.setAutoExecute(true);
        describe.setExample("No example provided.");

        return describe;
    }

    /**
     * Converts @Describe annotations into Describe objects.
     */
    public static AIExpose.Agent.Dtos.Describe annotationToDescribe(AIExpose.Agent.Annotations.Describe describe) {
        AIExpose.Agent.Dtos.Describe dto = new AIExpose.Agent.Dtos.Describe();
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
