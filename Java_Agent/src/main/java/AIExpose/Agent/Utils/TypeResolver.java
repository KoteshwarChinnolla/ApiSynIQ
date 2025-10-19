package AIExpose.Agent.Utils;

import java.lang.reflect.*;
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
        if (!(method.getGenericReturnType() instanceof ParameterizedType || ParamSchemaGenerator.isSimpleType(method.getReturnType()))) {
            return method.getReturnType();
        }

        // Case 3: Generic types like ResponseEntity<SampleDto1>
        ParameterizedType paramType = (ParameterizedType) method.getGenericReturnType();
        Type rawType = paramType.getRawType();

        // Extract inner type recursively
        Type actualTypeArg = paramType.getActualTypeArguments()[0];

        // If it's nested like CompletableFuture<ResponseEntity<SampleDto1>>, handle recursively
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
            if(ParamSchemaGenerator.isSimpleType((Class<?>)paramType.getRawType())){
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
}
