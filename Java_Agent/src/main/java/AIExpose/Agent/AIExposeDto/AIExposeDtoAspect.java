package AIExpose.Agent.AIExposeDto;


import AIExpose.Agent.AIExposeEp.DTOCollector;
import AIExpose.Agent.AIExposeEp.TypeResolver;
import AIExpose.Agent.Annotations.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import AIExpose.Agent.Dtos.*;
//import AIExpose.Agent.Utils.ParamSchemaGenerator;


@Component
public class AIExposeDtoAspect {
    public static DtoSchema scan(Class<?> clazz) {
        DtoSchema dtoSchema = new DtoSchema();
        dtoSchema.setClassName(clazz.getName());
        // System.out.println("Scanning Class: " + clazz.getName());
        if(TypeResolver.isSimpleType(clazz)){
            dtoSchema.setDescription(null);
            return dtoSchema;
        }

        if (clazz.isAnnotationPresent(AIExposeDto.class)) {
            AIExposeDto aiExposeDto = clazz.getAnnotation(AIExposeDto.class);
            dtoSchema.setName(aiExposeDto.name()!=""? aiExposeDto.name() : clazz.getSimpleName());
            dtoSchema.setDescription(aiExposeDto.description()!=""? aiExposeDto.description() : "No description provided.");
            dtoSchema.setExample(aiExposeDto.example()!=""? aiExposeDto.example() : "No example provided.");
        }

        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            DescribeDto describe = new DescribeDto();
            if (field.isAnnotationPresent(Describe.class)) {
                Describe aiExposeVal = field.getAnnotation(Describe.class);
                describe.setName(aiExposeVal.name()!=""? aiExposeVal.name() : field.getName());
                describe.setDescription(aiExposeVal.description()!=""? aiExposeVal.description() : "No description provided.");
                describe.setDataType(field.getType().getSimpleName());
                describe.setDefaultValue(aiExposeVal.defaultValue()!=""? aiExposeVal.defaultValue() : "");
                describe.setOptions(aiExposeVal.options()!=""? aiExposeVal.options() : "");
                describe.setAutoExecute(aiExposeVal.autoExecute() == true? true : false);
                describe.setExample(aiExposeVal.example()!=""? aiExposeVal.example() : "No example provided.");
                
            } else {
                describe.setName(field.getName());
                describe.setDataType(field.getType().getSimpleName());
                describe.setAutoExecute(true);
                describe.setExample("No example provided.");
                describe.setDefaultValue("No default value provided.");
                describe.setOptions("No options provided.");
                describe.setDescription("No description provided.");
            }
            dtoSchema.getFields().add(describe);
        }
        return dtoSchema;
    }
    public static Map<String, DtoSchema> DescribedDtosForMethods(Method method) {
        Map<String, Class<?>> visited = DTOCollector.collectDTOsForMethod(method);
        Map<String, DtoSchema> describedDtos = new LinkedHashMap<>();
        for (String key : new ArrayList<>(visited.keySet())) {
            Class<?> clazz = visited.get(key);
            describedDtos.put(key, AIExposeDtoAspect.scan(clazz));
        }
        return describedDtos;
    }
}
