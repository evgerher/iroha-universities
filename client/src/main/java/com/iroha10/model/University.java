package com.iroha10.model;

import com.fasterxml.jackson.annotation.JsonInclude;
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
  String name;
  List<Speciality> specialities;

  @Override
  public String toString() {
    return "{\"name\": " + name
        + ", \"specialities\": ["
        + specialities.stream()
            .map(Speciality::toString)
            .collect(Collectors.joining(" "))
        + "]}";
  }

  public void addSpeciality(Speciality speciality) {
    specialities.add(speciality);
  }
}
