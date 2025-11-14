package com.APISynIq.ApiResolver.Entity;


import com.apisyniq.grpc.Describe;

import jakarta.persistence.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "describe_dto")
public class DescribeEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private String name;
  @Column(columnDefinition = "TEXT")
  private String description;
  private String dataType;
  private String defaultValue;
  private String options;
  private boolean autoExecute;
  @Column(columnDefinition = "TEXT")
  private String example;

    public void grpcToEntity(Describe describeDto) {
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


  public Describe toGrpcDescribe() {
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
