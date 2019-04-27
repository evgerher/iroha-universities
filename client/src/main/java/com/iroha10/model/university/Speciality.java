package com.iroha10.model.university;

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
    }

    public void validate() {
        if (university == null || university.isEmpty())
            throw new RuntimeException("Invalid parameter");
        if (name == null || name.isEmpty())
            throw new RuntimeException("Invalid parameter");
        if (description == null || description.isEmpty())
            throw new RuntimeException("Invalid parameter");
        if (code == null || code.isEmpty())
            throw new RuntimeException("Invalid parameter");
        if (quantity <= 0)
            throw new RuntimeException("Invalid paramteter");
    }
}
