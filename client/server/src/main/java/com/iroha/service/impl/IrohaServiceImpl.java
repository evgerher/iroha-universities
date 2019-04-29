package com.iroha.service.impl;

import com.iroha.dao.MongoDBConnector;
import com.iroha.dao.model.UniversityKeys;
import com.iroha.model.university.Speciality;
import com.iroha.model.university.University;
import com.iroha.service.*;
import iroha.protocol.BlockOuterClass;

import java.io.File;
import java.security.KeyPair;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class IrohaServiceImpl implements IrohaService {
  private final MongoDBConnector mongoConnector;

  @Autowired
  public IrohaServiceImpl(@Qualifier("createConnector") MongoDBConnector mongoConnector) {
    this.mongoConnector = mongoConnector;
  }

  private BlockOuterClass.Block createGenesisBlock(Map<String, KeyPair> keys) {
    List<University> universities = mongoConnector.getUniversities();
    List<Speciality> specialities = mongoConnector.getSpecialities();
    Map<String, List<Speciality>> specsMap = universities.stream().collect(Collectors.toMap(
       University::getName,
       University::getSpecialities
    ));

    for (Speciality spec: specialities) {
      specsMap.get(spec.getUniversity()).add(spec);
    }

    return GenesisGenerator.getGenesisBlock(universities, keys);
  }

  @Override
  public void startBlockchain() {
    try {
      List<UniversityKeys> uniKeys = mongoConnector.getUniversityKeys();
      Map<String, KeyPair> uniKeyPairs = uniKeys.stream()
          .collect(Collectors.toMap(
              UniversityKeys::getUniversity,
              UniversityKeys::getKeys
          ));

      BlockOuterClass.Block genesis = createGenesisBlock(uniKeyPairs);
      GenesisGenerator.writeGenesisToFiles(genesis, new String[]{
          "../docker/genesis-kai/genesis.block",
          "../docker/genesis-ui/genesis.block",
          "../docker/genesis-kfu/genesis.block"
      });

      GenesisGenerator.saveKey(uniKeyPairs.get("KAI"), "../docker/genesis-kai");
      GenesisGenerator.saveKey(uniKeyPairs.get("KFU"), "../docker/genesis-kfu");
      GenesisGenerator.saveKey(uniKeyPairs.get("UI"), "../docker/genesis-ui");
//    GenesisGenerator.saveKey(uniKeyPairs.get("SPIBI"), "../docker/genesis-spibi/");

      File dir = new File("../docker");
      Runtime.getRuntime().exec(new String[]{"docker-compose", "up", "-d"}, null, dir);
    } catch (Exception e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Exception during iroha network initialization", e);
    }
  }
}
