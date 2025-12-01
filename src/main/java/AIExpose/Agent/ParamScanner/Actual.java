package AIExpose.Agent.ParamScanner;

import AIExpose.Agent.AIExposeEp.TypeResolver;
import AIExpose.Agent.Annotations.AIExposeEpHttp;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.*;
import java.util.*;

public class Actual extends Factory {
    public static Object generateClassSchema(Type type, Map<String, Object> vis, int depth) {
        // 1. Prevent infinite recursion
        if (depth > 10)
            return "too_deep";

        // 2. Prevent revisiting the same type (cyclic reference)
        String typeName = TypeResolver.getTypeName(type);
        if (vis.containsKey(typeName)) {
            return vis.get(typeName);
        }

        depth++;

        if (type instanceof Class<?> clazz) {

            // Simple types
            if (TypeResolver.isSimpleType(clazz)) {
                Object sample = sampleValue(clazz);
                vis.put(typeName, sample);
                return sample;
            }

            // Arrays
            if (clazz.isArray()) {
                Object elementSchema = generateClassSchema(clazz.getComponentType(), vis, depth);
                List<Object> arraySchema = List.of(elementSchema);
                vis.put(typeName, arraySchema);
                return arraySchema;
            }

            // Collections
            if (Collection.class.isAssignableFrom(clazz)) {
                List<Object> listSchema = List.of("Unknown Element Type");
                vis.put(typeName, listSchema);
                return listSchema;
            }

            // Maps
            if (Map.class.isAssignableFrom(clazz)) {
                Map<String, Object> mapSchema = Map.of("key", "String", "value", "Unknown Type");
                vis.put(typeName, mapSchema);
                return mapSchema;
            }

            // DTO / Custom class
            // --- Store a placeholder before recursion to prevent self-reference loops ---
            Map<String, Object> dtoPlaceholder = new LinkedHashMap<>();
            vis.put(typeName, dtoPlaceholder);

            Map<String, Object> resolved = resolveDtoType(clazz, vis, depth);
            vis.put(typeName, resolved);
            return resolved;
        }

        // ParameterizedType (List<Foo>, Map<String, Foo>, etc.)
        else if (type instanceof ParameterizedType pt) {
            Type rawType = pt.getRawType();

            if (rawType instanceof Class<?> rawClass) {

                // List<T>
                if (Collection.class.isAssignableFrom(rawClass)) {
                    Type elementType = pt.getActualTypeArguments()[0];
                    Object elementSchema = generateClassSchema(elementType, vis, depth);
                    List<Object> listSchema = List.of(elementSchema);
                    vis.put(typeName, listSchema);
                    return listSchema;
                }

                // Map<K, V>
                if (Map.class.isAssignableFrom(rawClass)) {
                    Type keyType = pt.getActualTypeArguments()[0];
                    Type valueType = pt.getActualTypeArguments()[1];

                    Object keySchema = generateClassSchema(keyType, vis, depth);
                    Object valueSchema = generateClassSchema(valueType, vis, depth);

                    Map<Object, Object> mapSchema = new LinkedHashMap<>();
                    mapSchema.put(
                            TypeResolver.isSimpleTypeFromType(keyType) ? TypeResolver.getTypeName(keyType) : keySchema,
                            valueSchema);

                    vis.put(typeName, mapSchema);
                    return mapSchema;
                }

                // Custom generic DTO
                Map<String, Object> dtoPlaceholder = new LinkedHashMap<>();
                vis.put(typeName, dtoPlaceholder);
                Map<String, Object> resolved = resolveDtoType(rawClass, vis, depth);
                vis.put(typeName, resolved);
                return resolved;
            }
        }

        // Unknown type
        Map<String, Object> unknown = Map.of("unknown_type", typeName);
        vis.put(typeName, unknown);
        return unknown;
    }

    public static Map<String, Object> resolveDtoType(Class<?> clazz, Map<String, Object> vis, int depth) {
        Map<String, Object> schema = new HashMap<>();
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            Type fieldType = field.getGenericType();
            schema.put(field.getName(), generateClassSchema(fieldType, vis, depth));
        }
        return schema;
    }

    protected static Object sampleValue(Class<?> type) {
        if (type == String.class)
            return "String";
        if (type == int.class || type == Integer.class)
            return 123;
        if (type == long.class || type == Long.class)
            return 123456L;
        if (type == boolean.class || type == Boolean.class)
            return true;
        if (type == double.class || type == Double.class)
            return 123.45;
        if (type == float.class || type == Float.class)
            return 12.34f;
        if (type == short.class || type == Short.class)
            return (short) 12;
        if (type == byte.class || type == Byte.class)
            return (byte) 1;
        if (type == char.class || type == Character.class)
            return 'A';
        if (type == Date.class)
            return new Date();
        if (type == LocalDate.class)
            return LocalDate.now();
        if (type == LocalDateTime.class)
            return LocalDateTime.now();
        if (type == LocalTime.class)
            return LocalTime.now();
        if (type == UUID.class)
            return UUID.randomUUID();
        if (type.isEnum())
            return type.getEnumConstants().length > 0 ? type.getEnumConstants()[0] : null;
        if (type == MultipartFile.class)
            return "Upload File";
        return null;
    }
}
