package AIExpose.Agent.Dtos;


import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Describe {
  private String name = "";
  private String description = "No description provided.";
  private String dataType = "String";
  private String defaultValue = "No default values provided";
  private String options = "No Options Provided to choose from.";
  private boolean autoExecute = true;
  private String example = "No example provided.";

    public void grpcToEntity(com.apisyniq.grpc.Describe describeDto) {
        if (describeDto == null) {
            this.name = null;
            this.description = null;
            this.dataType = null;
            this.defaultValue = null;
            this.options = null;
            this.autoExecute = false;
            this.example = null;
            return;
        }

        this.name = describeDto.getName();
        this.description = describeDto.getDescription();
        this.dataType = describeDto.getDataType();
        this.defaultValue = describeDto.getDefaultValue();
        this.options = describeDto.getOptions();
        this.example = describeDto.getExample();
        this.autoExecute = describeDto.getAutoExecute();
    }


    public com.apisyniq.grpc.Describe toGrpcDescribe() {
        com.apisyniq.grpc.Describe.Builder builder = com.apisyniq.grpc.Describe.newBuilder()
                .setName(this.name != null ? this.name : "")
                .setDescription(this.description != null ? this.description : "")
                .setDataType(this.dataType != null ? this.dataType : "")
                .setDefaultValue(this.defaultValue != null ? this.defaultValue : "")
                .setOptions(this.options != null ? this.options : "")
                .setAutoExecute(this.autoExecute)
                .setExample(this.example != null ? this.example : "");
        return builder.build();
    }

}
