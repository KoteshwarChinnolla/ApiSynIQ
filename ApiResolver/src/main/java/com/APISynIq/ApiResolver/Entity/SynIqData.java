package com.APISynIq.ApiResolver.Entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.*;

import com.apisyniq.grpc.InputData;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "syniq_data")
public class SynIqData {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String name;
  private String endpoint;
  private String httpMethod;

  // ✅ Use ElementCollection for lists
  @ElementCollection
  @CollectionTable(name = "syniq_tags", joinColumns = @JoinColumn(name = "syniq_id"))
  @Column(name = "tag")
  private List<String> tags;

  private String description;
  private String returnDescription;

  // ✅ Define proper join columns for OneToOne
  @OneToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = "inputs_describe_id")
  private InputsDtoEntity inputsDescribe;

  private String responseBody;
  private boolean autoExecute;

  @OneToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = "inputs_id")
  private InputsDtoEntity inputs;

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
  private Map<String, DescribeDtoEntity> describeDtosForParms = new HashMap<>();


  @Column(columnDefinition = "vector(768)")
  private float[] descriptionEmbedding;

  @Column(columnDefinition = "vector(768)")
  private float[] returnDescriptionEmbedding;

  public InputData toGrpcInputData() {
    InputData.Builder builder = InputData.newBuilder()
        .setName(this.name != null ? this.name : "")
        .setEndpoint(this.endpoint != null ? this.endpoint : "")
        .setHttpMethod(this.httpMethod != null ? this.httpMethod : "")
        .setDescription(this.description != null ? this.description : "")
        .setReturnDescription(this.returnDescription != null ? this.returnDescription : "")
        .setAutoExecute(this.autoExecute)
        .setInputsDescribe(this.inputsDescribe != null ? this.inputsDescribe.toGrpcInputsDto() : null)
        .setInputs(this.inputs != null ? this.inputs.toGrpcInputsDto() : null)
        .setOutputBody(this.outputBody != null ? this.outputBody : null)
        .addAllFilteringTags(this.filteringTags != null ? this.filteringTags : Collections.emptyList())
        .setResponseBody(this.responseBody != null ? this.responseBody : null);

      for (Map.Entry<String, DtoSchemaEntity> entry : this.dtoSchemas.entrySet()) {
          builder.putDtoSchemas(entry.getKey(), entry.getValue().toGrpcDtoSchema());
      }
      for (Map.Entry<String, DescribeDtoEntity> entry : this.describeDtosForParms.entrySet()) {
          builder.putDescribeDtosForParms(entry.getKey(), entry.getValue().toGrpcDescribeDto());
      }

      if (this.tags != null) {
          builder.addAllTags(this.tags);
      }

    return builder.build(); 
  }
}
