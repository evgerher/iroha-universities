package com.iroha10.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonInclude
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Speciality {
    String name;
    String description;
    String code;
    int quantity;

    @Override
    public String toString() {
        return "{\"name\": " + name
            + ", \"description\": " + description
            + ", \"code\": " + code
            + ", \"quantity\": " + quantity
            + "}";
    }
}
