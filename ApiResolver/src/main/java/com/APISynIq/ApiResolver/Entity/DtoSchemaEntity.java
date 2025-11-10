package com.APISynIq.ApiResolver.Entity;

import java.util.*;

import com.apisyniq.grpc.DescribeDto;
import com.apisyniq.grpc.DtoSchema;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    // ✅ One DtoSchemaEntity -> many DescribeDtoEntity
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "dto_schema_id") // foreign key in DescribeDtoEntity
    private List<DescribeFieldsEntity> fields;

    public void grpcToEntity(DtoSchema dtoSchema) {
        this.className = dtoSchema.getClassName();
        this.name = dtoSchema.getName();
        this.description = dtoSchema.getDescription();
        this.example = dtoSchema.getExample();
        List<DescribeDto> grpcDescribe = dtoSchema.getFieldsList();
        List<DescribeFieldsEntity> grpcDtoSchema = new ArrayList<>();
        for(DescribeDto describeDto : grpcDescribe){
            DescribeFieldsEntity describeFieldsEntity = new DescribeFieldsEntity();
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
        
        for (DescribeFieldsEntity field : this.fields) {
            builder.addFields(field.toGrpcDescribeDto());
        }
        
        return builder.build();
    }
}
