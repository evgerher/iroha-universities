package com.iroha.model.applicant.requests;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.AllArgsConstructor;
import lombok.Data;

@JsonInclude
@Data
@AllArgsConstructor
public class SelectSpecialityRequest {
  private static final Gson gson = new GsonBuilder().create();
  private final String code;
  private final String university;

  @Override
  public String toString() {
    return gson.toJson(this);
  }
}
