package com.iroha.model.applicant;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@JsonInclude
@AllArgsConstructor
public class TxHash {
  private String txhash;
}
