package com.iroha.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.AllArgsConstructor;
import lombok.Data;

@JsonInclude
@AllArgsConstructor
@Data
public class Asset {
  private static final Gson gson = new GsonBuilder().create();
  private final String name;
  private final String domain;
  private final int quantity;

  public String irohaNotation() {
    return String.format("%s@%s", name, domain);
  }

  @Override
  public String toString() {
    return gson.toJson(this);
  }
}
