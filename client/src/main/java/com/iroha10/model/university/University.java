package com.iroha10.model.university;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.List;
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

  public University(String name, String description) {
    this.name = name;
    this.description = description;
  }

  @Override
  public String toString() {
    return gson.toJson(this);
  }

  public void validate() {
    if (name == null || name.isEmpty())
      throw new RuntimeException("Invalid parameters");
  }
}
