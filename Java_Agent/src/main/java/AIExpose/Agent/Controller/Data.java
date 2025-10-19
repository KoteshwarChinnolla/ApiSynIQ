package AIExpose.Agent.Controller;

import AIExpose.Agent.AIExposeEp.AIExposeEPHttpAspect;
import AIExpose.Agent.Annotations.*;
import AIExpose.Agent.Dtos.ControllerSchema;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;


@Controller
public class Data {
  @Autowired
  private ApplicationContext applicationContext;
  @Autowired
  private AIExposeEPHttpAspect aiExposeEPHttpAspect;
  

    @RequestMapping("/entity-info")
    @ResponseBody
    public Map<String, List<Map<String, ControllerSchema>>> getEntityInformation() {
        Map<String, List<Map<String, ControllerSchema>>> groupedEndpoints = new HashMap<>();
        String[] beanNames = applicationContext.getBeanDefinitionNames();

        for (String beanName : beanNames) {
            Object bean = applicationContext.getBean(beanName);
            RestController RestControllerAnnotation = bean.getClass().getAnnotation(RestController.class);
            AIExposeController aiExposeController = bean.getClass().getAnnotation(AIExposeController.class);

            if (RestControllerAnnotation != null) {
                String controllerName = bean.getClass().getSimpleName();
                List<Map<String, ControllerSchema>> endpoints = new ArrayList<>();

                for (Method method : bean.getClass().getDeclaredMethods()) {
                    AIExposeEpHttp epAnnotation = method.getAnnotation(AIExposeEpHttp.class);
                    ControllerSchema schema = aiExposeEPHttpAspect.before(method, epAnnotation, aiExposeController);

                    Map<String, ControllerSchema> entityMap = new HashMap<>();
                    entityMap.put(method.getName(), schema);
                    endpoints.add(entityMap);
                }

                groupedEndpoints.put(controllerName, endpoints);
            }
        }

        return groupedEndpoints;
    }
}
