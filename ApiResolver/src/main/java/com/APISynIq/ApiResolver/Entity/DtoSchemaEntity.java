package com.APISynIq.ApiResolver.Entity;

import java.util.*;

import com.apisyniq.grpc.Describe;
import com.apisyniq.grpc.DtoSchema;

import jakarta.persistence.*;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Builder
@Table(name = "dto_schema")
public class DtoSchemaEntity {
    @Id
    private String className;
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String example;

    // ✅ One DtoSchemaEntity -> many DescribeEntity
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "dto_schema_id") // foreign key in DescribeEntity
    private List<DescribeEntity> fields;

    public void grpcToEntity(DtoSchema dtoSchema) {
        this.className = dtoSchema.getClassName();
        this.name = dtoSchema.getName();
        this.description = dtoSchema.getDescription();
        this.example = dtoSchema.getExample();
        List<Describe> grpcDescribe = dtoSchema.getFieldsList();
        List<DescribeEntity> grpcDtoSchema = new ArrayList<>();
        for(Describe describeDto : grpcDescribe){
            DescribeEntity describeFieldsEntity = new DescribeEntity();
            describeFieldsEntity.grpcToEntity(describeDto);
            grpcDtoSchema.add(describeFieldsEntity);
        }
        this.fields = grpcDtoSchema;
    }

    public DtoSchema toGrpcDtoSchema() {
        DtoSchema.Builder builder = DtoSchema.newBuilder()
                .setClassName(this.className != null ? this.className : "")
                .setName(this.name != null ? this.name : "")
                .setDescription(this.description != null ? this.description : "")
                .setExample(this.example != null ? this.example : "");
        
        for (DescribeEntity field : this.fields) {
            builder.addFields(field.toGrpcDescribe());
        }
        
        return builder.build();
    }
}
