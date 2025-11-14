package com.APISynIq.ApiResolver.Entity;

import java.util.*;

import com.apisyniq.grpc.Inputs;

import jakarta.persistence.*;
import lombok.*;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "controller_inputs")
public class InputsEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --------- 1. Input Body -------------
    @ElementCollection
    @CollectionTable(
        name = "input_body_map",
        joinColumns = @JoinColumn(name = "controller_input_id")
    )
    @MapKeyColumn(name = "key")
    // @Lob
    @Column(name = "value", columnDefinition = "TEXT")
    private Map<String, String> inputBody;

    // --------- 2. Path Params -------------
    @ElementCollection
    @CollectionTable(
        name = "input_path_params_map",
        joinColumns = @JoinColumn(name = "controller_input_id")
    )
    @MapKeyColumn(name = "key")
    // @Lob
    @Column(name = "value", columnDefinition = "TEXT")
    private Map<String, String> inputPathParams;

    // --------- 3. Query Params -------------
    @ElementCollection
    @CollectionTable(
        name = "input_query_params_map",
        joinColumns = @JoinColumn(name = "controller_input_id")
    )
    @MapKeyColumn(name = "key")
    // @Lob
    @Column(name = "value", columnDefinition = "TEXT")
    private Map<String, String> inputQueryParams;

    // --------- 4. Variables -------------
    @ElementCollection
    @CollectionTable(
        name = "input_variables_map",
        joinColumns = @JoinColumn(name = "controller_input_id")
    )
    @MapKeyColumn(name = "key")
    // @Lob
    @Column(name = "value", columnDefinition = "TEXT")
    private Map<String, String> inputVariables;

    // --------- 5. Headers -------------
    @ElementCollection
    @CollectionTable(
        name = "input_headers_map",
        joinColumns = @JoinColumn(name = "controller_input_id")
    )
    @MapKeyColumn(name = "key")
    // @Lob
    @Column(name = "value", columnDefinition = "TEXT")
    private Map<String, String> inputHeaders;

    // --------- 6. Cookies -------------
    @ElementCollection
    @CollectionTable(
        name = "input_cookies_map",
        joinColumns = @JoinColumn(name = "controller_input_id")
    )
    @MapKeyColumn(name = "key")
    // // @Lob
    @Column(name = "value", columnDefinition = "TEXT")
    private Map<String, String> inputCookies;

    public void grpcToEntity(Inputs inputsDto){
        this.inputBody = inputsDto.getInputBodyMap();
        this.inputPathParams = inputsDto.getInputPathParamsMap();
        this.inputQueryParams = inputsDto.getInputQueryParamsMap();
        this.inputVariables = inputsDto.getInputVariablesMap();
        this.inputHeaders = inputsDto.getInputHeadersMap();
        this.inputCookies = inputsDto.getInputCookiesMap();
    }

  public Inputs toGrpcInputs() {
    Inputs.Builder builder = Inputs.newBuilder();
    builder.putAllInputBody(this.inputBody)
            .putAllInputPathParams(this.inputPathParams)
            .putAllInputQueryParams(this.inputQueryParams)
            .putAllInputVariables(this.inputVariables)
            .putAllInputHeaders(this.inputHeaders)
            .putAllInputCookies(this.inputCookies);
    return builder.build();
  }
}
