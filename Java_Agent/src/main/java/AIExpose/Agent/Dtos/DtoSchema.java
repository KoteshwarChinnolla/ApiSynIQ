package AIExpose.Agent.Dtos;

import java.util.*;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class DtoSchema {
    private String className = "";
    private String name = "";
    private String description = "";
    private String example = "";
    private List<Describe> fields = new ArrayList<>();
}
