package com.iroha.service.impl;

import com.iroha.model.Applicant;
import com.iroha.model.university.University;
import com.iroha.service.IrohaService;
import iroha.protocol.BlockOuterClass;
import java.security.KeyPair;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class IrohaServiceImpl implements IrohaService {
  private BlockOuterClass.Block createGenesisBlock(List<University> universities, Map<String, KeyPair> keys) {
    return null;
  }


  @Override
  public Map<String, KeyPair> generateKeyPairsUniversities(List<University> universities) {
    return null;
  }

  @Override
  public KeyPair generateKeyPair(Applicant applicant) {
    return null;
  }

  @Override
  public void startBlockchain(List<University> universities) {

  }
}
