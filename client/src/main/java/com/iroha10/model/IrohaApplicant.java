package com.iroha10.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Data;

@Data
public class IrohaApplicant {
  private static final Gson gson = new GsonBuilder().create();
  private String userCode;
  private String pkey;
  private String pubkey;

  @Override
  public String toString() {
    return gson.toJson(this);
  }
}
