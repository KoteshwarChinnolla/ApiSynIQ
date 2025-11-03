package AIExpose.Agent.Dtos;

import java.util.*;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class OutputData {
  private String name = "";
  private String endpoint = "";
  private String httpMethod = "GET";
  private String[] tags = new String[]{};
  private String description = "No description provided.";
  private String returnDescription = "No return description provided.";
  private InputsDto inputsDescribe;
  private String responseBody = null;
  private boolean autoExecute = true;
  private InputsDto inputs;
  private String outputBody;
  private List<String> filteringTags = new ArrayList<>();
  private Map<String, DtoSchema> dtoSchemas = new HashMap<>();
}