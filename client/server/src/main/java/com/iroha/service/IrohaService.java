package com.iroha.service;

import com.iroha.model.Applicant;
import com.iroha.model.university.University;
import iroha.protocol.BlockOuterClass;
import java.security.KeyPair;
import java.util.List;
import java.util.Map;

public interface IrohaService {
  Map<String, KeyPair> generateKeyPairsUniversities(List<University> universities);
  KeyPair generateKeyPair(Applicant applicant);
  void startBlockchain(List<University> );

}
