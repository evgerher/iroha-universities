package com.iroha.model.applicant.requests;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.AllArgsConstructor;
import lombok.Data;

@JsonInclude
@AllArgsConstructor
@Data
public class ExchangeSpecialityRequest {
  private static final Gson gson = new GsonBuilder().create();
  private final SelectSpecialityRequest from;
  private final SelectSpecialityRequest to;

  @Override
  public String toString() {
    return gson.toJson(this);
  }
}
