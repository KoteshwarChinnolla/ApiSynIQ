package com.APISynIq.ApiResolver.Entity;

import java.util.*;

import com.apisyniq.grpc.Describe;
import com.apisyniq.grpc.DtoSchema;

import com.apisyniq.grpc.EndpointData;
import jakarta.persistence.*;
import jakarta.websocket.Endpoint;
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

    @ManyToMany(mappedBy = "dtoSchemas") // inverse side
    private Set<EndpointDataEntity> endpoints = new HashSet<>();

    public void grpcToEntity(DtoSchema dtoSchema) {
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

        List<Describe> grpcDescribeList = dtoSchema.getFieldsList();
        List<DescribeEntity> describeEntities = new ArrayList<>();

        if (grpcDescribeList != null && !grpcDescribeList.isEmpty()) {
            for (Describe describeDto : grpcDescribeList) {
                if (describeDto != null) {
                    DescribeEntity describeEntity = new DescribeEntity();
                    describeEntity.grpcToEntity(describeDto);
                    describeEntities.add(describeEntity);
                }
            }
        }

        this.fields = describeEntities;
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
