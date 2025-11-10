package com.APISynIq.ApiResolver.Dtos;

import com.APISynIq.ApiResolver.Entity.SynIqData;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InputsAndReturnsMatch {
    private List<SynIqData> inputsMatchData;
    private List<SynIqData> returnMatchData;
}
