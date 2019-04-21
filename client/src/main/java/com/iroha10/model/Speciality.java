package com.iroha10.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
    private static final Gson gson = new GsonBuilder().create();
    private String university;
    private String name;
    private String description;
    private String code;
    private int quantity;

    @Override
    public String toString() {
        return gson.toJson(this);
//        return "{\"name\": \"" + name  + "\""
//            + ", \"description\": \"" + description  + "\""
//            + ", \"code\": \"" + code  + "\""
//            + ", \"quantity\": " + quantity
//            + "}";
    }
}
