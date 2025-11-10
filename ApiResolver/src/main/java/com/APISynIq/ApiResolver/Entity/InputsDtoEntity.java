package com.APISynIq.ApiResolver.Entity;

import java.util.*;

import com.apisyniq.grpc.InputsDto;

import jakarta.persistence.*;
import lombok.*;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "controller_inputs")
public class InputsDtoEntity {
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

    public void grpcToEntity(InputsDto inputsDto){
        this.inputBody = inputsDto.getInputBodyMap();
        this.inputPathParams = inputsDto.getInputPathParamsMap();
        this.inputQueryParams = inputsDto.getInputQueryParamsMap();
        this.inputVariables = inputsDto.getInputVariablesMap();
        this.inputHeaders = inputsDto.getInputHeadersMap();
        this.inputCookies = inputsDto.getInputCookiesMap();
    }

  public InputsDto toGrpcInputsDto() {
    InputsDto.Builder builder = InputsDto.newBuilder();
    builder.putAllInputBody(inputBody)
            .putAllInputPathParams(inputPathParams)
            .putAllInputQueryParams(inputQueryParams)
            .putAllInputVariables(inputVariables)
            .putAllInputHeaders(inputHeaders)
            .putAllInputCookies(inputCookies);
    return builder.build();
  }
}
