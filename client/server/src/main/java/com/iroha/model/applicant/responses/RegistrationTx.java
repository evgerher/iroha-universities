package com.iroha.model.applicant.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.AllArgsConstructor;
import lombok.Data;

@JsonInclude
@Data
@AllArgsConstructor
public class RegistrationTx {
  private static final Gson gson = new GsonBuilder().create();
  private String txHash;
  private String payload;

  @Override
  public String toString() {
    return gson.toJson(this);
  }
}
