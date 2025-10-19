package AIExpose.Agent.Dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SampleDto2 {
  private String address;
  private String street;
  private SampleDto1 name;
}
