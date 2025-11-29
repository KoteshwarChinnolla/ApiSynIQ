package AIExpose.Agent.AIExposeEp;

import org.springframework.web.bind.annotation.*;
import org.springframework.core.env.Environment;

import jakarta.servlet.ServletContext;

import java.lang.reflect.*;

public class EndpointBuilder {

    private final Environment env;
    private final ServletContext servletContext;

    public EndpointBuilder(Environment env, ServletContext servletContext) {
        this.env = env;
        this.servletContext = servletContext;
    }

    public String makeEndpoint(Class<?> controllerClass, Method method) {
        String contextPath = getContextPath();
        String classPath = getClassBasePath(controllerClass);
        String methodPath = getMethodPath(method);

        StringBuilder endpoint = new StringBuilder();
        endpoint.append(contextPath)
                .append(classPath)
                .append(methodPath);
        // Normalize slashes
        String normalized = endpoint.toString()
                .replaceAll("//+", "/")
                .replaceFirst("(?<!http:|https:)/", "");
        return normalized;
    }

    // ✅ Get global context path from properties or servlet context
    private String getContextPath() {
        if (servletContext != null && servletContext.getContextPath() != null)
            return servletContext.getContextPath();
        if (env != null) {
            String configured = env.getProperty("server.servlet.context-path");
            if (configured != null) return configured;
        }
        return "";
    }

    // ✅ Get class-level @RequestMapping path
    private String getClassBasePath(Class<?> controllerClass) {
        if (controllerClass.isAnnotationPresent(RequestMapping.class)) {
            RequestMapping rm = controllerClass.getAnnotation(RequestMapping.class);
            return getFirstPath(rm.value(), rm.path());
        }
        return "";
    }

    // ✅ Get method-level mapping path
    private String getMethodPath(Method method) {
        if (method.isAnnotationPresent(GetMapping.class)) {
            return getFirstPath(method.getAnnotation(GetMapping.class).value(),
                    method.getAnnotation(GetMapping.class).path());
        } else if (method.isAnnotationPresent(PostMapping.class)) {
            return getFirstPath(method.getAnnotation(PostMapping.class).value(),
                    method.getAnnotation(PostMapping.class).path());
        } else if (method.isAnnotationPresent(PutMapping.class)) {
            return getFirstPath(method.getAnnotation(PutMapping.class).value(),
                    method.getAnnotation(PutMapping.class).path());
        } else if (method.isAnnotationPresent(DeleteMapping.class)) {
            return getFirstPath(method.getAnnotation(DeleteMapping.class).value(),
                    method.getAnnotation(DeleteMapping.class).path());
        } else if (method.isAnnotationPresent(RequestMapping.class)) {
            RequestMapping rm = method.getAnnotation(RequestMapping.class);
            return getFirstPath(rm.value(), rm.path());
        }
        return "";
    }

    public String getHttpMethod(Method method) {
        if (method.isAnnotationPresent(GetMapping.class)) return "GET";
        if (method.isAnnotationPresent(PostMapping.class)) return "POST";
        if (method.isAnnotationPresent(PutMapping.class)) return "PUT";
        if (method.isAnnotationPresent(DeleteMapping.class)) return "DELETE";
        if (method.isAnnotationPresent(RequestMapping.class)) {
            RequestMethod[] m = method.getAnnotation(RequestMapping.class).method();
            if (m.length > 0) return m[0].name();
        }
        return "GET";
    }

    private String getFirstPath(String[] value, String[] path) {
        if (value.length > 0) return value[0];
        if (path.length > 0) return path[0];
        return "";
    }
}
