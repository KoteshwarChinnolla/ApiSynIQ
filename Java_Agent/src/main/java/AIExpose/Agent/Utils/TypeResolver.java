package AIExpose.Agent.Utils;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class TypeResolver {

    /**
     * Resolves the actual return type of a controller method.
     * Handles void, ResponseEntity<T>, CompletableFuture<T>, Mono<T>, Flux<T>, etc.
     */
    public static Class<?> resolveActualReturnType(Method method) {
        // Case 1: void
        if (method.getReturnType() == void.class) {
            return null;
        }

        // Case 2: Non-generic direct class (e.g. SampleDto1)
        if (!(method.getGenericReturnType() instanceof ParameterizedType
                || ParamSchemaGenerator.isSimpleType(method.getReturnType()))) {
            return method.getReturnType();
        }

        // Case 3: Generic types like ResponseEntity<SampleDto1>
        ParameterizedType paramType = (ParameterizedType) method.getGenericReturnType();
        Type rawType = paramType.getRawType();

        // Extract inner type recursively
        Type actualTypeArg = paramType.getActualTypeArguments()[0];

        // If it's nested like CompletableFuture<ResponseEntity<SampleDto1>>, handle
        // recursively
        if (actualTypeArg instanceof ParameterizedType) {
            return getRawClass(((ParameterizedType) actualTypeArg).getActualTypeArguments()[0]);
        }

        // If the generic type itself is a Class, return it
        if (actualTypeArg instanceof Class<?>) {
            return (Class<?>) actualTypeArg;
        }

        // Fallback — return raw type
        return getRawClass(rawType);
    }

    public static Class<?> resolveActualType(Parameter parameter) {
        Type type = parameter.getParameterizedType();

        if (type instanceof ParameterizedType) {
            ParameterizedType paramType = (ParameterizedType) type;
            if (ParamSchemaGenerator.isSimpleType((Class<?>) paramType.getRawType())) {
                System.out.println("actualTypeArg: " + paramType.getRawType());
                return (Class<?>) paramType.getRawType();
            }
            Type actualTypeArg = paramType.getActualTypeArguments()[0];
            if (actualTypeArg instanceof Class<?>) {
                return (Class<?>) actualTypeArg;
            } else if (actualTypeArg instanceof ParameterizedType) {
                return (Class<?>) ((ParameterizedType) actualTypeArg).getRawType();
            }
        }

        return parameter.getType(); // fallback if not generic
    }

    private static Class<?> getRawClass(Type type) {
        if (type instanceof Class<?>) {
            return (Class<?>) type;
        } else if (type instanceof ParameterizedType) {
            return (Class<?>) ((ParameterizedType) type).getRawType();
        } else {
            return Object.class;
        }
    }

    public static Object describeType(Type type) {
        // Base case: if it's a simple class, return its simple name
        if (type instanceof Class<?> clazz) {
            return clazz.getSimpleName();
        }

        // If it's a parameterized type like Map<String, List<SomeDto>>
        else if (type instanceof ParameterizedType pType) {
            Map<String, Object> typeInfo = new LinkedHashMap<>();
            Type rawType = pType.getRawType();
            typeInfo.put("rawType", ((Class<?>) rawType).getSimpleName());

            Type[] typeArgs = pType.getActualTypeArguments();
            List<Object> params = new ArrayList<>();

            for (Type arg : typeArgs) {
                // 🔁 Recursively describe each generic argument
                params.add(describeType(arg));
            }

            typeInfo.put("typeParameters", params);
            return typeInfo;
        }

        // If it's an array
        else if (type instanceof GenericArrayType arrayType) {
            Map<String, Object> typeInfo = new LinkedHashMap<>();
            typeInfo.put("arrayOf", describeType(arrayType.getGenericComponentType()));
            return typeInfo;
        }

        // If it’s a wildcard or type variable (rare)
        else {
            return type.getTypeName();
        }
    }

}
