package com.APISynIq.ApiResolver.Entity;


import com.apisyniq.grpc.DescribeDto;

import jakarta.persistence.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "describe_dto")
public class DescribeFieldsEntity {
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

  public void grpcToEntity(DescribeDto describeDto) {
      this.name = describeDto.getName();
      this.description = describeDto.getDescription();
      this.dataType = describeDto.getDataType();
      this.defaultValue = describeDto.getDefaultValue();
      this.options = describeDto.getOptions();
      this.autoExecute = describeDto.getAutoExecute();
      this.example = describeDto.getExample();
      this.autoExecute = describeDto.getAutoExecute();
  }

  public DescribeDto toGrpcDescribeDto() {
    com.apisyniq.grpc.DescribeDto.Builder builder = com.apisyniq.grpc.DescribeDto.newBuilder()
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
