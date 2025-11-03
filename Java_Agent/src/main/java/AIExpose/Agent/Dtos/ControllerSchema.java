package AIExpose.Agent.Dtos;

import java.util.*;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class ControllerSchema {
  private String name;
  private String endpoint;
  private String httpMethod;
  private String[] tags;
  private String description;
  private List<DescribeDto> pathParams;
  private List<DescribeDto> reqParams;
  private String returnDescription;
  private List<DtoSchema> requestBody;
  private DtoSchema responseBody;
  private boolean autoExecute;
  private InputsDto inputs;
  private String outputBody;
  private List<String> filteringTags;
}