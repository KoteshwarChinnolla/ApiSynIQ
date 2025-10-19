package AIExpose.Agent.Annotations;

import java.lang.annotation.*;


@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AIExposeEpHttp {
    String name() default "";
    String[] tags() default {};
    String example() default "";
    String description() default "";
    Describe[] pathParams() default {};
    Describe[] reqParams() default {};
    boolean autoExecute() default false;
    String returnDescription() default "";
}
