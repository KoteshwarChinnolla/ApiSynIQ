package AIExpose.Agent.Dtos;

import AIExpose.Agent.Annotations.Describe;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class address {
    @Describe(description = "Must be provided with the pin code", autoExecute = true)
  private String city;
  private String state;
}
