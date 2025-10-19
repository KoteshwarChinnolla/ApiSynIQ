package AIExpose.Agent.Utils;

import java.lang.reflect.*;
import java.time.*;
import java.util.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import AIExpose.Agent.enums.ParamType;

public class ParamSchemaGenerator {

    public static Map<String, Object> generateMethodParamSchema(Method method, ParamType type) {
        Map<String, Object> paramSchema = new LinkedHashMap<>();

        for (Parameter parameter : method.getParameters()) {
            Class<?> paramType = parameter.getType();
            Type paramGenericType = parameter.getParameterizedType();
            String paramName = parameter.getName();

            // ✅ PATH PARAM
            if (parameter.isAnnotationPresent(PathVariable.class) && type == ParamType.PATH_PARAM) {
                PathVariable pv = parameter.getAnnotation(PathVariable.class);
                String name = !pv.value().isEmpty() ? pv.value() : paramName;
                paramSchema.put(name, sampleValue(paramType));
            }

            // ✅ REQUEST PARAM
            else if (parameter.isAnnotationPresent(RequestParam.class) && type == ParamType.REQUEST_PARAM) {
                RequestParam rp = parameter.getAnnotation(RequestParam.class);
                String name = !rp.value().isEmpty() ? rp.value() : paramName;
                paramSchema.put(name, sampleValue(paramType));
            }

            // ✅ REQUEST BODY
            else if (parameter.isAnnotationPresent(RequestBody.class) && type == ParamType.REQUEST_BODY) {
                paramSchema.put("requestBody", generateClassSchema(paramGenericType, new HashMap<>(), 0));
            }

            // ✅ REQUEST HEADER
            else if (parameter.isAnnotationPresent(RequestHeader.class) && type == ParamType.REQUEST_HEADER) {
                RequestHeader rh = parameter.getAnnotation(RequestHeader.class);
                String name = !rh.value().isEmpty() ? rh.value() : paramName;
                paramSchema.put(name, sampleValue(paramType));
            }

            // ✅ VARIABLE / DTO PARAM
            else if (type == ParamType.VARIABLE
                    && !parameter.isAnnotationPresent(RequestBody.class)
                    && !parameter.isAnnotationPresent(PathVariable.class)
                    && !parameter.isAnnotationPresent(RequestParam.class)
                    && !parameter.isAnnotationPresent(RequestHeader.class)) {

                if (isSimpleType(paramType)) {
                    paramSchema.put(paramName, sampleValue(paramType));
                } else {
                    paramSchema.put(paramName, generateClassSchema(paramGenericType,  new HashMap<>(), 0));
                }
            }
        }
        return paramSchema;
    }

    // 🔁 Recursive Class Schema Generator
    public static Object generateClassSchema(Type type, Map<String, Object> vis, int depth) {
        // 1. Prevent infinite recursion
        if (depth > 5) return "too_deep";

        // 2. Prevent revisiting the same type (cyclic reference)
        String typeName = type.getTypeName();
        if (vis.containsKey(typeName)) {
            return vis.get(typeName);
        }

        depth++;

        if (type instanceof Class<?> clazz) {

            // Simple types
            if (isSimpleType(clazz)) {
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
                        isSimpleTypeFromType(keyType) ? getTypeName(keyType) : keySchema,
                        valueSchema
                    );

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
            schema.put(field.getName(), generateClassSchema(fieldType,  vis, depth));
        }
        return schema;
    }

    
public static boolean isSimpleType(Class<?> type) {
    return type.isPrimitive()
            // ✅ Primitive wrappers and strings
            || type == String.class
            || type == Boolean.class
            || type == Character.class
            || Number.class.isAssignableFrom(type)
            || type == Integer.class
            || type == Long.class
            || type == Double.class
            || type == Float.class
            || type == Short.class
            || type == Byte.class

            // ✅ Java Time API
            || type == java.util.Date.class
            || type == java.sql.Date.class
            || type == java.sql.Timestamp.class
            || type == LocalDate.class
            || type == LocalDateTime.class
            || type == LocalTime.class
            || type == OffsetDateTime.class
            || type == OffsetTime.class
            || type == ZonedDateTime.class
            || type == Instant.class
            || type == Duration.class
            || type == Period.class

            // ✅ Common utility / identifier types
            || type == java.util.UUID.class
            || type == java.net.URI.class
            || type == java.net.URL.class
            || type == java.net.InetAddress.class
            || type == java.util.Locale.class
            || type == java.util.Currency.class

            // ✅ Big Numbers
            || type == java.math.BigDecimal.class
            || type == java.math.BigInteger.class

            // ✅ Optional (treat as simple — unwrapped separately if needed)
            || type == java.util.Optional.class

            // ✅ Enum types
            || type.isEnum();
}

    
    private static boolean isSimpleTypeFromType(Type type) {
        return type instanceof Class<?> clazz && isSimpleType(clazz);
    }

    private static String getTypeName(Type type) {
        if (type instanceof Class<?> clazz)
            return clazz.getSimpleName();
        return type.getTypeName();
    }

    
    private static Object sampleValue(Class<?> type) {
        if (type == String.class) return "String";
        if (type == int.class || type == Integer.class) return 123;
        if (type == long.class || type == Long.class) return 123456L;
        if (type == boolean.class || type == Boolean.class) return true;
        if (type == double.class || type == Double.class) return 123.45;
        if (type == float.class || type == Float.class) return 12.34f;
        if (type == short.class || type == Short.class) return (short) 12;
        if (type == byte.class || type == Byte.class) return (byte) 1;
        if (type == char.class || type == Character.class) return 'A';
        if (type == Date.class) return new Date();
        if (type == LocalDate.class) return LocalDate.now();
        if (type == LocalDateTime.class) return LocalDateTime.now();
        if (type == LocalTime.class) return LocalTime.now();
        if (type == UUID.class) return UUID.randomUUID();
        if (type.isEnum()) return type.getEnumConstants().length > 0 ? type.getEnumConstants()[0] : null;
        if (type == MultipartFile.class) return "Upload File";
        return null;
    }
}
