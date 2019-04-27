package com.iroha.model.applicant;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.AllArgsConstructor;
import lombok.Data;

@JsonInclude
@AllArgsConstructor
@Data
public class ApplicantExchangeSpeciality {
  private static final Gson gson = new GsonBuilder().create();
  private final ApplicantSelectSpeciality from;
  private final ApplicantSelectSpeciality to;

  @Override
  public String toString() {
    return gson.toJson(this);
  }
}
