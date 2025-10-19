package AIExpose.Agent.Dtos;

import java.util.*;
import lombok.*;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class InputsDto {
  private Map<String, Object> inputBody = new HashMap<>();
  private Map<String, Object> inputPathParams = new HashMap<>();
  private Map<String, Object> inputQueryParams = new HashMap<>();
  private Map<String, Object> inputVariables = new HashMap<>();
  private Map<String, Object> inputHeaders = new HashMap<>();
  private Map<String, Object> inputCookies = new HashMap<>();
}
