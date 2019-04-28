package com.iroha.service.impl;

import com.iroha.dao.MongoDBConnector;
import com.iroha.model.Applicant;
import com.iroha.model.university.University;
import com.iroha.service.GenesisGenerator;
import com.iroha.service.IrohaService;
import iroha.protocol.BlockOuterClass;
import java.security.KeyPair;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class IrohaServiceImpl implements IrohaService {
  private final MongoDBConnector mongoConnector;

  @Autowired
  public IrohaServiceImpl(@Qualifier("createConnector") MongoDBConnector mongoConnector) {
    this.mongoConnector = mongoConnector;
  }

  private BlockOuterClass.Block createGenesisBlock() {
    List<University> universities = mongoConnector.getUniversities();
    Map<String, KeyPair> uniKeyPairs = universities
        .stream()
        .collect(Collectors.toMap(
            University::getName,
            t -> mongoConnector.getUniversityKeys(t.getName())
        )); // create map of keys

    return GenesisGenerator.getGenesisBlock(universities, uniKeyPairs);
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
