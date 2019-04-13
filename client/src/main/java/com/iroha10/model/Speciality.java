package com.iroha10.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonInclude
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Speciality {
    private String name;
    private String description;
    private String code;
    private int quantity;

    @Override
    public String toString() {
        return "{\"name\": " + name
            + ", \"description\": " + description
            + ", \"code\": " + code
            + ", \"quantity\": " + quantity
            + "}";
    }
}
