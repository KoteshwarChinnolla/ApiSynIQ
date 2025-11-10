package com.APISynIq.ApiResolver.Entity;

import com.apisyniq.grpc.Describe;
import com.apisyniq.grpc.DtoSchema;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.util.*;

import com.apisyniq.grpc.EndpointData;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "syniq_data")
public class EndpointDataEntity {

  @Id
  private String id;

  private String name;
  private String endpoint;
  private String httpMethod;


    @PrePersist
    public void generateId() {
        if (this.id == null && this.httpMethod != null && this.endpoint != null) {
            this.id = String.format("%s-%s", this.httpMethod, this.endpoint);
        }
    }

  // ✅ Use ElementCollection for lists
  @ElementCollection
  @CollectionTable(name = "syniq_tags", joinColumns = @JoinColumn(name = "syniq_id"))
  @Column(name = "tag")
  private List<String> tags;
    @Column(columnDefinition = "TEXT")
  private String description;
  @Column(columnDefinition = "TEXT")
  private String returnDescription;

  // ✅ Define proper join columns for OneToOne
  @OneToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = "inputs_describe_id")
  private InputsEntity inputsDescribe;
    // @Lob
    @Column(columnDefinition = "TEXT")
  private String responseBody;
  private boolean autoExecute;

  @OneToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = "inputs_id")
  private InputsEntity inputs;
  // @Lob
    @Column(columnDefinition = "TEXT")
  private String outputBody;


  @ElementCollection
  @CollectionTable(name = "syniq_filtering_tags", joinColumns = @JoinColumn(name = "syniq_id"))
  @Column(name = "filter_tag")
  private List<String> filteringTags;


  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "syniq_data_id")
  @MapKeyColumn(name = "key_name")
  private Map<String, DtoSchemaEntity> dtoSchemas = new HashMap<>();

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "syniq_data_id")
  @MapKeyColumn(name = "key_name")
  private Map<String, DescribeEntity> describeDtosForParms = new HashMap<>();

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "input_description_embedding_id")
    @JsonIgnore
    private DescriptionEmbeddingEntity inputDescriptionEmbedding;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "return_description_embedding_id")
    @JsonIgnore
    private DescriptionEmbeddingEntity returnDescriptionEmbedding;

    public void grpcToEntity(EndpointData inputData){
        this.name = inputData.getName();
        this.endpoint = inputData.getEndpoint();
        this.httpMethod = inputData.getHttpMethod();
        this.description = inputData.getDescription();
        this.returnDescription = inputData.getReturnDescription();
        this.autoExecute = inputData.getAutoExecute();
        InputsEntity inputsDtoEntity = new InputsEntity();
        inputsDtoEntity.grpcToEntity(inputData.getInputsDescribe());
        this.inputsDescribe = inputsDtoEntity;
        InputsEntity inputsEntity = new InputsEntity();
        inputsEntity.grpcToEntity(inputData.getInputs());
        this.inputs = inputsEntity;
        this.outputBody =  inputData.getOutputBody();
        this.responseBody =  inputData.getResponseBody();
        this.tags = inputData.getTagsList();
        Map<String, DtoSchemaEntity> entityOfDtoSchemas = new HashMap<>();
        Map<String, DtoSchema> dtoSchemas = inputData.getDtoSchemasMap();
        for(String key:dtoSchemas.keySet()){
            DtoSchemaEntity dtoSchemaEntity = new DtoSchemaEntity();
            DtoSchema value = dtoSchemas.get(key);
            dtoSchemaEntity.grpcToEntity(value);
            entityOfDtoSchemas.put(key, dtoSchemaEntity);
        }
        this.dtoSchemas = entityOfDtoSchemas;
        Map<String, DescribeEntity> entityOfDescribeDtos = new HashMap<>();
        Map<String, Describe> grpcDescribeDto = inputData.getDescribeDtosForParmsMap();
        for(String key:grpcDescribeDto.keySet()){
            DescribeEntity describeDtoEntity = new DescribeEntity();
            Describe value  = grpcDescribeDto.get(key);
            describeDtoEntity.grpcToEntity(value);
            entityOfDescribeDtos.put(key, describeDtoEntity);
        }
        this.describeDtosForParms = entityOfDescribeDtos;
        this.filteringTags = inputData.getFilteringTagsList();

    }

  public EndpointData toGrpcEndpointData() {
    EndpointData.Builder builder = EndpointData.newBuilder()
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
        .setResponseBody(this.responseBody != null ? this.responseBody : null);

      for (Map.Entry<String, DtoSchemaEntity> entry : this.dtoSchemas.entrySet()) {
          builder.putDtoSchemas(entry.getKey(), entry.getValue().toGrpcDtoSchema());
      }
      for (Map.Entry<String, DescribeEntity> entry : this.describeDtosForParms.entrySet()) {
          builder.putDescribeDtosForParms(entry.getKey(), entry.getValue().toGrpcDescribe());
      }

      if (this.tags != null) {
          builder.addAllTags(this.tags);
      }

    return builder.build(); 
  }
}
