package com.APISynIq.ApiResolver.Entity;

import java.util.*;

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
