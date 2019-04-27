package com.iroha10.model.applicant;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;

@JsonInclude
@Data
@AllArgsConstructor
public class UserCode {
  private final String userCode;

  @Override
  public String toString() {
    return userCode;
  }
}
