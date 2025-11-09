package AIExpose.Agent.Utils;


import java.lang.reflect.*;
import java.util.*;

import AIExpose.Agent.AIExposeDto.AIExposeDtoAspect;
import AIExpose.Agent.Dtos.DtoSchema;

public class DTOCollector {

    public static Map<String, Class<?>> collectDTOs(Type type, Map<String, Class<?>> visited, int depth) {
        if (depth > 10) return visited;

        String typeName = type.getTypeName();
        if (visited.containsKey(typeName)) return visited;
        depth++;

        if (type instanceof Class<?> clazz) {
            if (ParamSchemaGenerator.isSimpleType(clazz)) return visited;

            if (!visited.containsKey(typeName)) {
                visited.put(clazz.getSimpleName(), clazz);
            }
            if (clazz.isArray()) {
                collectDTOs(clazz.getComponentType(), visited, depth);
            }
            else if (Collection.class.isAssignableFrom(clazz)) {}
            else if (Map.class.isAssignableFrom(clazz)) {}
            else {
                for (Field field : clazz.getDeclaredFields()) {
                    field.setAccessible(true);
                    collectDTOs(field.getGenericType(), visited, depth);
                }
            }

        } else if (type instanceof ParameterizedType pt) {

            Type rawType = pt.getRawType();
            Type[] typeArgs = pt.getActualTypeArguments();

            if (rawType instanceof Class<?> rawClazz) {

                if (Collection.class.isAssignableFrom(rawClazz)) {
                    collectDTOs(typeArgs[0], visited, depth);
                }

                else if (Map.class.isAssignableFrom(rawClazz)) {
                    collectDTOs(typeArgs[1], visited, depth);
                }

                else {
                    for (Type arg : typeArgs) {
                        collectDTOs(arg, visited, depth);
                    }
                    for (Field field : rawClazz.getDeclaredFields()) {
                        field.setAccessible(true);
                        collectDTOs(field.getGenericType(), visited, depth);
                    }
                }
            }
        } else if (type instanceof GenericArrayType gat) {
            collectDTOs(gat.getGenericComponentType(), visited, depth);
        }
        return visited;
    }

    public static Map<String, Class<?>> collectDTOsForMethod(Method method) {
        Map<String, Class<?>> visited = new LinkedHashMap<>();
        for (Parameter param : method.getParameters()) {
            collectDTOs(param.getParameterizedType(), visited, 0);
        }
        collectDTOs(method.getGenericReturnType(), visited, 0);
        return visited;
    }

    public static Map<String, DtoSchema> DescribedDtosForMethods(Method method) {
        Map<String, Class<?>> visited = collectDTOsForMethod(method);
        Map<String, DtoSchema> describedDtos = new LinkedHashMap<>();
        for (String key : new ArrayList<>(visited.keySet())) {
            Class<?> clazz = visited.get(key);
            describedDtos.put(key, AIExposeDtoAspect.scan(clazz));
        }
        return describedDtos;
    }
}
