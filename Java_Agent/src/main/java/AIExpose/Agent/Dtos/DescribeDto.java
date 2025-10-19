package AIExpose.Agent.Dtos;


import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DescribeDto {
  private String name = "";
  private String description = "No description provided.";
  private String dataType = "String";
  private String defaultValue = "No default values provided";
  private String options = "No Options Provided to choose from.";
  private boolean autoExecute = true;
  private String example = "No example provided.";
}
