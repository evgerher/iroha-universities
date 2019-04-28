package com.iroha;

import com.iroha.model.Applicant;
import com.iroha.model.university.Speciality;
import com.iroha.model.university.University;
import com.iroha.service.GenesisGenerator;
import com.iroha.service.UniversityService;
import com.iroha.utils.ChainEntitiesUtils;
import io.reactivex.Observable;
import iroha.protocol.BlockOuterClass;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyPair;
import java.util.Arrays;

import jp.co.soramitsu.iroha.java.TransactionStatusObserver;
import jp.co.soramitsu.iroha.testcontainers.PeerConfig;

import static java.lang.Thread.sleep;

public class IrohaMain {

  public static void main(String[] args) throws IOException, InterruptedException {
    Speciality speciality = new Speciality("ui", "cs", "", "code", 1);
    University university = new University("ui", "ui", Arrays.asList(speciality));
//		IrohaContainer iroha = new IrohaContainer()
//				.withPeerConfig(getPeerConfig(university));
//		iroha.start();
    BlockOuterClass.Block genesis = GenesisGenerator.getGenesisBlock(Arrays.asList(university));
    writeGenesisToFile(genesis,"./docker/genesis-kai/genesis.block");
    writeGenesisToFile(genesis,"./docker/genesis-ui/genesis.block");
    File dir = new File("./docker");
    Process p = Runtime.getRuntime().exec(new String[]{"docker-compose","up", "-d"},null, dir);
    sleep(5000);
    UniversityService service = new UniversityService(
        ChainEntitiesUtils.universitiesKeys.get(university.getName()),
        university);
    System.out.println(ChainEntitiesUtils.bytesToHex(ChainEntitiesUtils.universitiesKeys.get(university.getName()).getPublic().getEncoded()
    ));
    Applicant applicant = new Applicant( "name", "surname");
    KeyPair applicantKeys = ChainEntitiesUtils.generateKey();
    applicant.setPubkey(ChainEntitiesUtils.bytesToHex(applicantKeys.getPublic().getEncoded()));
    Observable accountCreation = service.createNewApplicantAccount(applicant, applicantKeys);
    accountCreation.blockingSubscribe(TransactionStatusObserver.builder()
            // executed when stateless or stateful validation is failed
            .onTransactionFailed(t -> System.out.println(String.format(
                    "transaction %s failed with msg: %s",
                    t.getTxHash(),
                    t.getErrOrCmdName()

            )))
            // executed when got any exception in handlers or grpc
            .onError(e -> System.out.println("Failed with exception: " + e))
            // executed when we receive "committed" status
            .onTransactionCommitted((t) -> System.out.println("Committed :)"))
            // executed when transfer is complete (failed or succeed) and observable is closed
            .onComplete(() -> System.out.println("Complete"))
            .build());
    applicant.setPkey(applicantKeys.getPrivate().toString());

    Observable observable = service.getWildTokensTransaction(applicant);
    observable.blockingSubscribe();
    int balance = service
        .getBalanceOfApplicant(applicant, ChainEntitiesUtils.Consts.WILD_ASSET_NAME);
    System.out.println("_______________________________________________");
    System.out.println(balance);
    System.out.println("_______________________________________________");
  }




  private static void writeGenesisToFile(BlockOuterClass.Block genesis, String path) throws FileNotFoundException {
    FileOutputStream file = new FileOutputStream(path);
    try {
      System.out.println(genesis.getBlockV1().toString());
      file.write(genesis.getBlockV1().toString().getBytes());
      file.flush();
      file.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static PeerConfig getPeerConfig(University university) {

    PeerConfig config = PeerConfig.builder()
        .genesisBlock(GenesisGenerator.getGenesisBlock(Arrays.asList(university)))
        .build();

    // don't forget to add peer keypair to config
    config.withPeerKeyPair(ChainEntitiesUtils.universitiesKeys.get(university.getName()));
    return config;
  }

}
