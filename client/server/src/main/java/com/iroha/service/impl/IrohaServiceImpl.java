package com.iroha.service.impl;

import com.iroha.dao.MongoDBConnector;
import com.iroha.dao.model.UniversityKeys;
import com.iroha.model.university.Speciality;
import com.iroha.model.university.University;
import com.iroha.service.*;
import com.iroha.utils.GenesisGeneratorUtils;
import iroha.protocol.BlockOuterClass;

import java.io.File;
import java.security.KeyPair;
import java.util.LinkedList;
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

  /**
   * Method creates genesis block from universities in the mongodb (created during initialization process)
   * @param keys - map of university name -> university KeyPair
   * @return genesis block
   */
  private BlockOuterClass.Block createGenesisBlock(Map<String, KeyPair> keys) {
    List<University> universities = mongoConnector.getUniversities();
    List<Speciality> specialities = mongoConnector.getSpecialities();

    // Make sure to initialize specialities
    for (University uni: universities)
      uni.setSpecialities(new LinkedList<>());

    Map<String, List<Speciality>> specsMap = universities.stream().collect(Collectors.toMap(
       University::getName,
       University::getSpecialities
    ));

    for (Speciality spec: specialities) {
      specsMap.get(spec.getUniversity()).add(spec);
    }

    return new GenesisGeneratorImpl(universities, keys).getGenesisBlock();
  }

  /**
   * Initialize blockchain with `docker-compose up` in `docker/` folder
   */
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
      GenesisGeneratorUtils.writeGenesisToFiles(genesis, new String[]{
          "../docker/genesis-kai/genesis.block",
          "../docker/genesis-ui/genesis.block",
          "../docker/genesis-kfu/genesis.block"
      });

      // KeyPairs (node.pub, node.priv)
      GenesisGeneratorUtils.saveKey(uniKeyPairs.get("kai"), "../docker/genesis-kai");
      GenesisGeneratorUtils.saveKey(uniKeyPairs.get("kfu"), "../docker/genesis-kfu");
      GenesisGeneratorUtils.saveKey(uniKeyPairs.get("ui"), "../docker/genesis-ui");
//    GenesisGeneratorImpl.saveKey(uniKeyPairs.get("SPIBI"), "../docker/genesis-spibi/");

      File dir = new File("../docker");
      Runtime.getRuntime().exec(new String[]{"docker-compose", "up", "-d"}, null, dir);
    } catch (Exception e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Exception during iroha network initialization", e);
    }
  }
}
