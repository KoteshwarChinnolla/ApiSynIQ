package AIExpose.Agent.Annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AIExposeDto {
  String name() default "";
  String description() default "";
  String example() default "";
}
