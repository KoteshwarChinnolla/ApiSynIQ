package com.APISynIq.ApiResolver.Controller;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Query {
    private String query;
    private int limit;
}
