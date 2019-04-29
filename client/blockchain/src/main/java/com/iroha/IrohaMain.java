package com.iroha;

import com.iroha.model.Applicant;
import com.iroha.model.Asset;
import com.iroha.model.university.Speciality;
import com.iroha.model.university.University;
import com.iroha.service.GenesisGenerator;
import com.iroha.service.UniversityService;
import com.iroha.utils.ChainEntitiesUtils;
import iroha.protocol.BlockOuterClass;

import java.io.File;
import java.io.IOException;
import java.security.KeyPair;
import java.util.Arrays;
import java.util.List;

import iroha.protocol.QryResponses;
import java.util.Map;
import java.util.stream.Collectors;
import jp.co.soramitsu.iroha.java.TransactionStatusObserver;
import lombok.val;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.iroha.service.GenesisGenerator.saveKey;
import static com.iroha.service.GenesisGenerator.writeGenesisToFiles;
import static com.iroha.utils.ChainEntitiesUtils.*;
import static java.lang.Thread.sleep;

public class IrohaMain {
  private static final Logger logger = LoggerFactory.getLogger(IrohaMain.class);

  public static void main(String[] args) throws IOException, InterruptedException {
    Speciality speciality = new Speciality("ui", "cs", "", "code", 1);
    List<Speciality> specialities = Arrays.asList(speciality);

    University university = new University("ui", "ui", "192.168.0.3:10002");
    University kai = new University("kai", "kai", "192.168.0.2:10001");
    University kfu = new University("kfu", "kfu", "192.168.0.4:10003");

    List<University> universities = Arrays.asList(university,kai,kfu);
    Map<String, KeyPair> uniKeys = ChainEntitiesUtils.generateKeys(
        universities.stream()
        .map(t -> t.getName())
        .collect(Collectors.toList())
    );

    university.setSpecialities(specialities);
    kai.setSpecialities(specialities);
    kfu.setSpecialities(specialities);

    kai.setPeerKey(uniKeys.get("kai"));
    kfu.setPeerKey(uniKeys.get("kfu"));
    university.setPeerKey(uniKeys.get("ui"));

//		IrohaContainer iroha = new IrohaContainer()
//				.withPeerConfig(getPeerConfig(university));
//		iroha.start();
    BlockOuterClass.Block genesis = GenesisGenerator.getGenesisBlock(universities, uniKeys);
    writeGenesisToFiles(genesis, new String[]{
        "./docker/genesis-kai/genesis.block",
        "./docker/genesis-ui/genesis.block",
        "./docker/genesis-kfu/genesis.block"
    });

    saveKey(kai.getPeerKey(),"./docker/genesis-kai");
    saveKey(university.getPeerKey(),"./docker/genesis-ui");
    saveKey(kfu.getPeerKey(),"./docker/genesis-kfu");

    logger.info("Genesis and keys are generated and stored");

    File dir = new File("./docker");
    Process p = Runtime.getRuntime().exec(new String[]{"docker-compose","up", "-d"},null, dir);
    logger.info("Sleep for 30 seconds");
    sleep(30000);
    logger.info("Sleep finished");

    UniversityService service = new UniversityService(
        uniKeys.get(university.getName()),
        university);
    String pubkey = ChainEntitiesUtils.bytesToHex(uniKeys.get(university.getName()).getPublic().getEncoded());
    logger.info("University pubkey={}", pubkey);

    val observer =TransactionStatusObserver.builder()
            // executed when stateless or stateful validation is failed
            .onTransactionFailed(t -> logger.info(String.format(
                    "transaction %s failed with msg: %s",
                    t.getTxHash(),
                    t.getErrOrCmdName()

            )))
            // executed when got any exception in handlers or grpc
            .onError(e -> logger.info("Failed with exception: " + e))
            // executed when we receive "committed" status
            .onTransactionCommitted((t) -> logger.info("Committed :)"))
            // executed when transfer is complete (failed or succeed) and observable is closed
            .onComplete(() -> logger.info("Complete0"))
            .onTransactionSent(() -> logger.info("sent"))
            .onNotReceived(e -> logger.info("not received with: "+ e))
            .onEnoughSignaturesCollected(t -> logger.info("sigs collected: "+ t))
            .onMstExpired(t -> logger.info("mst expired: "+ t))
            .onMstPending(t -> logger.info("pending: "+ t))
            .onStatelessValidationSuccess(t -> logger.info("sls val: "+ t))
            .onUnrecognizedStatus(t -> logger.info("unrecognized"+t))
            .onStatefulValidationSuccess(t -> logger.info("slf val: "+ t))
            .onUnrecognizedStatus(t -> logger.info("HZ"))
            .onRejected(t -> logger.info("rejected" + t))
            .build();

    Applicant applicant = new Applicant("name", "surname");

    KeyPair applicantKeys = ChainEntitiesUtils.generateKey();
    applicant.setPkey(applicantKeys.getPrivate().toString());
    applicant.setPubkey(ChainEntitiesUtils.bytesToHex(applicantKeys.getPublic().getEncoded()));

    val observer1 =TransactionStatusObserver.builder()
            // executed when stateless or stateful validation is failed
            .onTransactionFailed(t -> logger.info(String.format(
                    "transaction %s failed with msg: %s",
                    t.getTxHash(),
                    t.getErrOrCmdName()

            )))
            // executed when got any exception in handlers or grpc
            .onError(e -> logger.info("Failed with exception: " + e))
            // executed when we receive "committed" status
            .onTransactionCommitted((t) -> logger.info("Committed :)"))
            // executed when transfer is complete (failed or succeed) and observable is closed
            .onComplete(() -> logger.info("Complete1"))
            .build();

    service.createNewApplicantAccount(applicant, applicantKeys, observer1);
    for(int i =0 ; i < 10; i++  ) {
      val observer2 =TransactionStatusObserver.builder()
              // executed when stateless or stateful validation is failed
              .onTransactionFailed(t -> logger.info(String.format(
                      "transaction %s failed with msg: %s",
                      t.getTxHash(),
                      t.getErrOrCmdName()

              )))
              // executed when got any exception in handlers or grpc
              .onError(e -> logger.info("Failed with exception: " + e))
              // executed when we receive "committed" status
              .onTransactionCommitted((t) -> logger.info("Committed :)"))
              // executed when transfer is complete (failed or succeed) and observable is closed
              .onComplete(() -> logger.info("Complete2"))
              .build();
      applicant.setName("name"+ Integer.toString(i));
      applicantKeys = ChainEntitiesUtils.generateKey();
      applicant.setPkey(applicantKeys.getPrivate().toString());
      applicant.setPubkey(ChainEntitiesUtils.bytesToHex(applicantKeys.getPublic().getEncoded()));
      service.createNewApplicantAccount(applicant, applicantKeys, observer2);
    }

    logger.info("Sleep for 30 secs");
    sleep(15000);
    logger.info("Sleep finished");
    logger.info("Sending wild tokens to applicant={}", applicant);

    service.getWildTokensTransaction(applicant, observer);
    logger.info("Sleep for 10 seconds");
    sleep(10000);
    logger.info("Sleep finished");
    int balance = service.getBalanceOfApplicant(applicant, Consts.WILD_ASSET_NAME);
    logger.info("_______________________________________________");
    logger.info("Balance [{} -> {}]", Consts.WILD_ASSET_NAME, balance);
    logger.info("_______________________________________________");
    List<QryResponses.AccountAsset> assets = service.getAllAssertsOfApplicant(applicant);
    for(QryResponses.AccountAsset asset: assets){
      logger.info(String.format("%s %s",asset.getAssetId(),asset.getBalance()));
    }
    service.chooseUniversity(applicant,applicantKeys,observer, university, uniKeys.get(university.getName()));
    logger.info("Sleep for 10 seconds");

    sleep(10000);
    logger.info("Sleep finished1");
    assets =service.getAllAssertsOfApplicant(applicant);
      for(QryResponses.AccountAsset asset: assets){
          logger.info(String.format("%s %s",asset.getAssetId(),asset.getBalance()));
      }

    service.chooseSpeciality(applicant,speciality,observer,applicantKeys, university, uniKeys.get(university.getName()));
    logger.info("Sleep for 10 seconds");

    sleep(10000);
    logger.info("Sleep finished2");
    assets =service.getAllAssertsOfApplicant(applicant);
    for(QryResponses.AccountAsset asset: assets){
      logger.info(String.format("%s %s",asset.getAssetId(),asset.getBalance()));
    }

    service.swapUniversity(applicant,university,speciality,kai,applicantKeys, uniKeys.get(kai.getName()), observer);
    logger.info("Sleep for 10 seconds");

    sleep(20000);
    logger.info("Sleep finished2");
    assets =service.getAllAssertsOfApplicant(applicant);
    for(QryResponses.AccountAsset asset: assets){
      logger.info(String.format("%s %s",asset.getAssetId(),asset.getBalance()));
    }
  }
}
