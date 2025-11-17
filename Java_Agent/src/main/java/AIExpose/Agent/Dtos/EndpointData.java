package AIExpose.Agent.Dtos;

import java.util.*;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class EndpointData {
  private String name = "";
  private String endpoint = "";
  private String httpMethod = "GET";
  private String[] tags = new String[]{};
  private String description = "No description provided.";
  private String returnDescription = "No return description provided.";
  private Inputs inputsDescribe;
  private String responseBody = null;
  private boolean autoExecute = true;
  private Inputs inputs;
  private String outputBody;
  private List<String> filteringTags = new ArrayList<>();
  private Map<String, DtoSchema> dtoSchemas = new HashMap<>();
  private Map<String, Describe> describeDtosForParms = new HashMap<>();
  private String globalPath = null;

    public void grpcToEntity(com.apisyniq.grpc.EndpointData inputData) {
        this.name = inputData.getName();
        this.endpoint = inputData.getEndpoint();
        this.httpMethod = inputData.getHttpMethod();
        this.description = inputData.getDescription();
        this.returnDescription = inputData.getReturnDescription();
        this.autoExecute = inputData.getAutoExecute();
        this.outputBody = inputData.getOutputBody();
        this.responseBody = inputData.getResponseBody();

        if (this.inputsDescribe == null) {
            this.inputsDescribe = new Inputs();
        }
        this.inputsDescribe.grpcToEntity(inputData.getInputsDescribe());

        if (this.inputs == null) {
            this.inputs = new Inputs();
        }
        this.inputs.grpcToEntity(inputData.getInputs());

        this.tags = inputData.getTagsList().toArray(new String[0]);
        this.filteringTags = new ArrayList<>(inputData.getFilteringTagsList());
        this.globalPath = inputData.getGlobalPath();
        Map<String, com.apisyniq.grpc.DtoSchema> dtoSchemas = inputData.getDtoSchemasMap();
        if (this.dtoSchemas == null) {
            this.dtoSchemas = new HashMap<>();
        } else {
            this.dtoSchemas.clear();
        }
        for (Map.Entry<String, com.apisyniq.grpc.DtoSchema> entry : dtoSchemas.entrySet()) {
            DtoSchema dtoSchema = new DtoSchema();
            dtoSchema.grpcToEntity(entry.getValue());
            this.dtoSchemas.put(entry.getKey(), dtoSchema);
        }

        Map<String, com.apisyniq.grpc.Describe> grpcDescribeDto = inputData.getDescribeDtosForParmsMap();
        if (this.describeDtosForParms == null) {
            this.describeDtosForParms = new HashMap<>();
        } else {
            this.describeDtosForParms.clear();
        }
        for (Map.Entry<String, com.apisyniq.grpc.Describe> entry : grpcDescribeDto.entrySet()) {
            Describe describe = new Describe();
            describe.grpcToEntity(entry.getValue());
            this.describeDtosForParms.put(entry.getKey(), describe);
        }
    }

    public com.apisyniq.grpc.EndpointData toGrpcEndpointData() {
        com.apisyniq.grpc.EndpointData.Builder builder = com.apisyniq.grpc.EndpointData.newBuilder()
                .setName(this.name != null ? this.name : "")
                .setEndpoint(this.endpoint != null ? this.endpoint : "")
                .setHttpMethod(this.httpMethod != null ? this.httpMethod : "")
                .setDescription(this.description != null ? this.description : "")
                .setReturnDescription(this.returnDescription != null ? this.returnDescription : "")
                .setAutoExecute(this.autoExecute)
                .setInputsDescribe(this.inputsDescribe != null ? this.inputsDescribe.toGrpcInputs() : null)
                .setInputs(this.inputs != null ? this.inputs.toGrpcInputs() : null)
                .setOutputBody(this.outputBody != null ? this.outputBody : null)
                .addAllFilteringTags(this.filteringTags != null ? this.filteringTags : Collections.emptyList())
                .setResponseBody(this.responseBody != null ? this.responseBody : null)
                .setGlobalPath(this.globalPath != null ? this.globalPath : "");
        for (Map.Entry<String, DtoSchema> entry : this.dtoSchemas.entrySet()) {
            builder.putDtoSchemas(entry.getKey(), entry.getValue().toGrpcDtoSchema());
        }
        for (Map.Entry<String, Describe> entry : this.describeDtosForParms.entrySet()) {
            builder.putDescribeDtosForParms(entry.getKey(), entry.getValue().toGrpcDescribe());
        }

        if (this.tags != null) {
            builder.addAllTags(new ArrayList<>(Arrays.asList(this.tags)));
        }

        return builder.build();
    }
}