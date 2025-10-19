package AIExpose.Agent.Dtos;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InputsDto {
  private Map<String, Object> inputBody;
  private Map<String, Object> inputPathParams;
  private Map<String, Object> inputQueryParams;
  private Map<String, Object> inputVariables;
  private Map<String, Object> inputHeaders;
  private Map<String, Object> inputCookies;
}
