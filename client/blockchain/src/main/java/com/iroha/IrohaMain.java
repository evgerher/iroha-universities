package com.iroha;

import com.iroha.model.Applicant;
import com.iroha.model.university.Speciality;
import com.iroha.model.university.University;
import com.iroha.service.GenesisGenerator;
import com.iroha.service.UniversityService;
import com.iroha.utils.ChainEntitiesUtils;
import io.reactivex.Observable;
import iroha.protocol.BlockOuterClass;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyPair;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import jp.co.soramitsu.iroha.testcontainers.PeerConfig;

public class IrohaMain {

  public static void main(String[] args) throws FileNotFoundException {
//    Speciality speciality = new Speciality("ui", "cs", "", "code", 1);
//    University university = new University("ui", "ui", "192.168.0.2", 5134, Arrays.asList(speciality));
////		IrohaContainer iroha = new IrohaContainer()
////				.withPeerConfig(getPeerConfig(university));
////		iroha.start();
//    List<University> universities = Arrays.asList(university);
//    Map<String, KeyPair> universitiesKeys = ChainEntitiesUtils.generateKeys(universities.stream()
//        .map(x -> x.getName())
//        .collect(Collectors.toList()));
//
//    BlockOuterClass.Block genesis = GenesisGenerator.getGenesisBlock(universities, universitiesKeys);
//    UniversityService service = new UniversityService(
//        universitiesKeys.get(university.getName()),
//        university);
//    Applicant applicant = new Applicant( "name", "surname");
//    service.createNewApplicantAccount(applicant, null);
//    KeyPair applicantKeys = service.createNewApplicantAccount(applicant);
//    applicant.setPubkey(applicantKeys.getPublic().toString());
//    applicant.setPkey(applicantKeys.getPrivate().toString());
//
//    Observable observable = service.getWildTokensTransaction(applicant);
//    observable.blockingSubscribe();
//    int balance = service
//        .getBalanceOfApplicant(applicant, ChainEntitiesUtils.Consts.WILD_ASSET_NAME);
//    System.out.println("_______________________________________________");
//    System.out.println(balance);
//    System.out.println("_______________________________________________");
  }

  private static String bytesToHex(byte[] hashInBytes) {

    StringBuilder sb = new StringBuilder();
    for (byte b : hashInBytes) {
      sb.append(String.format("%02x", b));
    }
    return sb.toString();

  }


  private static void writeGenesisToFile(BlockOuterClass.Block genesis, String path) throws FileNotFoundException {
    FileOutputStream file = new FileOutputStream(path);
    try {
      file.write(genesis.toString().getBytes());
      file.flush();
      file.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static PeerConfig getPeerConfig(University university, Map<String, KeyPair> universitiesKeys) {

    PeerConfig config = PeerConfig.builder()
        .genesisBlock(GenesisGenerator.getGenesisBlock(Arrays.asList(university), universitiesKeys))
        .build();

    // don't forget to add peer keypair to config
    config.withPeerKeyPair(universitiesKeys.get(university.getName()));
    return config;
  }

}
