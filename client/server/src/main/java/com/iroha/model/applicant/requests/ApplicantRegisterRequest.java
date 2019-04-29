package com.iroha.model.applicant.requests;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@JsonInclude
@Data
public class ApplicantRegisterRequest {
  private String name;
  private String surname;
  private Integer totalPoints;
}
