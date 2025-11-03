package AIExpose.Agent.Dtos;

import java.util.*;
import lombok.*;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class InputsDto {
  private Map<String, String> inputBody = new HashMap<>();
  private Map<String, String> inputPathParams = new HashMap<>();
  private Map<String, String> inputQueryParams = new HashMap<>();
  private Map<String, String> inputVariables = new HashMap<>();
  private Map<String, String> inputHeaders = new HashMap<>();
  private Map<String, String> inputCookies = new HashMap<>();
}
