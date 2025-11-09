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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String className;
    private String name;
    private String description;
    private String example;

    // ✅ One DtoSchemaEntity -> many DescribeDtoEntity
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "dto_schema_id") // foreign key in DescribeDtoEntity

    private List<DescribeDtoEntity> fields;

    public void grpcToEntity(DtoSchema dtoSchema) {
        this.className = dtoSchema.getClassName();
        this.name = dtoSchema.getName();
        this.description = dtoSchema.getDescription();
        this.example = dtoSchema.getExample();
        List<DescribeDto> grpcDescribe = dtoSchema.getFieldsList();
        List<DescribeDtoEntity> grpcDtoSchema = new ArrayList<>();
        for(DescribeDto describeDto : grpcDescribe){
            DescribeDtoEntity describeDtoEntity = new DescribeDtoEntity();
            describeDtoEntity.grpcToEntity(describeDto);
            grpcDtoSchema.add(describeDtoEntity);
        }
        this.fields = grpcDtoSchema;
    }

    public DtoSchema toGrpcDtoSchema() {
        DtoSchema.Builder builder = DtoSchema.newBuilder()
                .setClassName(this.className != null ? this.className : "")
                .setName(this.name != null ? this.name : "")
                .setDescription(this.description != null ? this.description : "")
                .setExample(this.example != null ? this.example : "");
        
        for (DescribeDtoEntity field : this.fields) {
            builder.addFields(field.toGrpcDescribeDto());
        }
        
        return builder.build();
    }
}
