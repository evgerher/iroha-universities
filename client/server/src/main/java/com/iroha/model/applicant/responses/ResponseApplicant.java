package com.iroha.model.applicant;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.iroha.model.Applicant;
import com.iroha.model.Asset;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@JsonInclude
@Data
@AllArgsConstructor
public class ResponseApplicant {
  private static final Gson gson = new GsonBuilder().create();
  private final String name;
  private final String surname;
  private final String usercode;
  private final List<Asset> assets;

  public ResponseApplicant(Applicant applicant, List<Asset> assets) {
    this.name = applicant.getName();
    this.surname = applicant.getSurname();
    this.usercode = applicant.getUserCode();
    this.assets = assets;
  }

  @Override
  public String toString() {
    return gson.toJson(this);
  }
}
