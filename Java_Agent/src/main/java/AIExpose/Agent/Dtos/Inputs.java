package AIExpose.Agent.Dtos;

import java.util.*;
import lombok.*;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class Inputs {
  private Map<String, String> inputBody = new HashMap<>();
  private Map<String, String> inputPathParams = new HashMap<>();
  private Map<String, String> inputQueryParams = new HashMap<>();
  private Map<String, String> inputVariables = new HashMap<>();
  private Map<String, String> inputHeaders = new HashMap<>();
  private Map<String, String> inputCookies = new HashMap<>();

    public void grpcToEntity(com.apisyniq.grpc.Inputs inputsDto){
        this.inputBody = inputsDto.getInputBodyMap();
        this.inputPathParams = inputsDto.getInputPathParamsMap();
        this.inputQueryParams = inputsDto.getInputQueryParamsMap();
        this.inputVariables = inputsDto.getInputVariablesMap();
        this.inputHeaders = inputsDto.getInputHeadersMap();
        this.inputCookies = inputsDto.getInputCookiesMap();
    }

    public com.apisyniq.grpc.Inputs toGrpcInputs() {
        com.apisyniq.grpc.Inputs.Builder builder = com.apisyniq.grpc.Inputs.newBuilder();
        builder.putAllInputBody(this.inputBody)
                .putAllInputPathParams(this.inputPathParams)
                .putAllInputQueryParams(this.inputQueryParams)
                .putAllInputVariables(this.inputVariables)
                .putAllInputHeaders(this.inputHeaders)
                .putAllInputCookies(this.inputCookies);
        return builder.build();
    }
}
