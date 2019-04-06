package com.iroha10.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

@JsonInclude
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UniversityApplication {
  String name;
  List<Speciality> specialities;
}
