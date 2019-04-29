package com.iroha.service.impl;

import com.iroha.dao.MongoDBConnector;
import com.iroha.dao.model.UniversityKeys;
import com.iroha.model.university.University;
import com.iroha.service.*;
import iroha.protocol.BlockOuterClass;

import java.security.KeyPair;
import java.util.List;
import java.util.Map;
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
    List<UniversityKeys> uniKeys = mongoConnector.getUniversityKeys();
    Map<String, KeyPair> uniKeyPairs = uniKeys.stream()
        .collect(Collectors.toMap(
            UniversityKeys::getUniversity,
            UniversityKeys::getKeys
        ));

    return GenesisGenerator.getGenesisBlock(universities, uniKeyPairs);
  }

  @Override
  public void startBlockchain(List<University> universities) {
    BlockOuterClass.Block genesis = createGenesisBlock();
    // todo: store genesis block in corresponding folders
    // todo: init docker-compose ...
  }
}
