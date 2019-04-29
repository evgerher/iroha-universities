package com.iroha.model.university;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.security.KeyPair;
import java.util.LinkedList;
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
  private KeyPair peerKey;
  private String uri;

  public University(String name, String description, String uri) {
    this.name = name;
    this.description = description;
    this.uri = uri;
    this.specialities = new LinkedList<>();
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
