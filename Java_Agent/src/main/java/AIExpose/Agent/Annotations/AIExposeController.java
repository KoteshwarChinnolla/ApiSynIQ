package AIExpose.Agent.Annotations;
import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AIExposeController {
  String name() default "";
  String description() default "";
}
