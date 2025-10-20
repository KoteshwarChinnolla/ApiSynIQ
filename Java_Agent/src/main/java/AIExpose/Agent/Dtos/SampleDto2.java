package AIExpose.Agent.Dtos;

import AIExpose.Agent.Annotations.Describe;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SampleDto2 {
  private String address;
  private String street;
  @Describe(name = "Fields", description = "This is a nested DTO field", dataType = "SampleDto3", autoExecute = false, example = "{" +
          "\"fieldA\": \"ExampleA\"," +
          "\"fieldB\": 123" +
          "}")
  private SampleDto3 fields;
}
