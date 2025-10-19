package AIExpose.Agent.Annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Describe {
  String name() default "";
  String description() default "";
  String dataType() default "String";
  String defaultValue() default "";
  String options() default "";
  boolean autoExecute() default true;
  String example() default "";
}
