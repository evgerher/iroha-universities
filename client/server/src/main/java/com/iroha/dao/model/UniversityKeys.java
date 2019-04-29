package com.iroha.dao.model;

import java.security.KeyPair;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UniversityKeys {
  private String university;
  private KeyPair keys;
}
