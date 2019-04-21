package com.iroha10.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonInclude
@Data
@AllArgsConstructor
@NoArgsConstructor
public class University {
  private String name;
  private String description;
  private List<Speciality> specialities;
  private static final Gson gson = new GsonBuilder().create();

  public University(String name) {
    this.name = name;
    this.specialities = new ArrayList<>();
  }

  @Override
  public String toString() {
//    return "{\"name\": \"" + name + "\""
//        + ", \"specialities\" :"
//        + specialitiesToString()
//        + "}";
    return gson.toJson(this);
  }

  public String specialitiesToString() {
    return gson.toJson(specialities);
//    return "[" + specialities.stream()
//        .map(Speciality::toString)
//        .collect(Collectors.joining(" ")) + "]";
  }

  public void addSpeciality(Speciality speciality) {
    specialities.add(speciality);
  }
}
