package AIExpose.Agent.Dtos;

import java.util.*;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class DtoSchema {
    private String className = "";
    private String name = "";
    private String description = "";
    private String example = "";
    private List<Describe> fields = new ArrayList<>();

    public void grpcToEntity(com.apisyniq.grpc.DtoSchema dtoSchema) {
        if (dtoSchema == null) {
            this.className = null;
            this.name = null;
            this.description = null;
            this.example = null;
            this.fields = new ArrayList<>();
            return;
        }

        this.className = dtoSchema.getClassName();
        this.name = dtoSchema.getName();
        this.description = dtoSchema.getDescription();
        this.example = dtoSchema.getExample();

        List<com.apisyniq.grpc.Describe> grpcDescribeList = dtoSchema.getFieldsList();
        List<Describe> describeEntities = new ArrayList<>();

        if (grpcDescribeList != null && !grpcDescribeList.isEmpty()) {
            for (com.apisyniq.grpc.Describe describeDto : grpcDescribeList) {
                if (describeDto != null) {
                    Describe Describe = new Describe();
                    Describe.grpcToEntity(describeDto);
                    describeEntities.add(Describe);
                }
            }
        }

        this.fields = describeEntities;
    }


    public com.apisyniq.grpc.DtoSchema toGrpcDtoSchema() {
        com.apisyniq.grpc.DtoSchema.Builder builder = com.apisyniq.grpc.DtoSchema.newBuilder()
                .setClassName(this.className != null ? this.className : "")
                .setName(this.name != null ? this.name : "")
                .setDescription(this.description != null ? this.description : "")
                .setExample(this.example != null ? this.example : "");

        for (Describe field : this.fields) {
            builder.addFields(field.toGrpcDescribe());
        }

        return builder.build();
    }

}
