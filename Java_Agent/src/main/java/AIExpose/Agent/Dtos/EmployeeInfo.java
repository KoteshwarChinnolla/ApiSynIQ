package AIExpose.Agent.Dtos;

import AIExpose.Agent.Annotations.*;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@AIExposeDto(
        name = "EmployeeInfo",
        description = "Consists of major details of a particulate employee, can be used when creating a new employee, updating a employee or have to get the major details of the employee"
        )
public class EmployeeInfo {
    @Describe(name = "Name", description = "Full name of the employee, must be conformed before directly executing", autoExecute = false)
  private String name;
  @Describe(name = "age", description = "Age of the user", defaultValue = "20", dataType = "Integer")
  private Integer age;
  private address address;
}
