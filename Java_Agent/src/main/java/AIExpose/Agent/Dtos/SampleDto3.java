package AIExpose.Agent.Dtos;

import AIExpose.Agent.Annotations.AIExposeDto;
import AIExpose.Agent.Annotations.Describe;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@AIExposeDto(name = "SampleDto3", description = "This is a sample DTO 3", example = "{" +
        "\"fieldA\": \"ExampleA\"," +
        "\"fieldB\": 123" +
        "}")
public class SampleDto3 {
    @Describe(name = "Field A", description = "This is field A", dataType = "String", autoExecute = false, example = "ExampleA")
    private String fieldA;
    @Describe(name = "Field B", description = "This is field B", dataType = "int", autoExecute = false, example = "123")
    private int fieldB;
}
