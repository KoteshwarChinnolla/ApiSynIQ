package AIExpose.Agent.ParamScanner;

import AIExpose.Agent.Annotations.AIExposeEpHttp;
import AIExpose.Agent.Dtos.DescribeDto;
import AIExpose.Agent.enums.ParamType;

import java.lang.reflect.Parameter;

public class Describe extends Factory {
    protected static DescribeDto getDescribedParams(Parameter parameter, AIExposeEpHttp aiExposeEpHttp, ParamType type) {
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
    public static DescribeDto annotationToDescribe(AIExpose.Agent.Annotations.Describe describe) {
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
