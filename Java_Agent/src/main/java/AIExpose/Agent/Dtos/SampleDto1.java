package AIExpose.Agent.Dtos;

import AIExpose.Agent.Annotations.*;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@AIExposeDto(name = "sampleDto1", description = "Sample Dto for testing")
public class SampleDto1 {
  private String name;
  @Describe(name = "age", description = "Age of the user", defaultValue = "20", dataType = "Integer")
  private Integer age;
  private SampleDto2 address;
}
