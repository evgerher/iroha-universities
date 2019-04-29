package com.iroha;

import com.google.protobuf.util.JsonFormat;
import com.iroha.model.Applicant;
import com.iroha.model.Asset;
import com.iroha.model.university.Speciality;
import com.iroha.model.university.University;
import com.iroha.service.GenesisGenerator;
import com.iroha.service.UniversityService;
import com.iroha.utils.ChainEntitiesUtils;
import com.iroha.utils.IrohaApiSingletone;
import io.reactivex.Observable;
import iroha.protocol.BlockOuterClass;

import java.io.File;
import java.io.IOException;
import java.security.KeyPair;
import java.util.Arrays;
import java.util.List;

import iroha.protocol.QryResponses;
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
    University university = new University("ui", "ui", Arrays.asList(speciality));
    University kai = new University("kai", "kai", Arrays.asList(speciality));
    University kfu = new University("kfu", "kfu", Arrays.asList(speciality));
    kai.setPeerKey(ChainEntitiesUtils.generateKey());
    university.setUri("192.168.0.3:10002");
    kai.setUri("192.168.0.2:10001");
    kfu.setUri("192.168.0.4:10003");
    kfu.setPeerKey(ChainEntitiesUtils.generateKey());
    university.setPeerKey(ChainEntitiesUtils.generateKey());
    saveKey(kai.getPeerKey(),"./docker/genesis-kai");
//		IrohaContainer iroha = new IrohaContainer()
//				.withPeerConfig(getPeerConfig(university));
//		iroha.start();
    BlockOuterClass.Block genesis = GenesisGenerator.getGenesisBlock(Arrays.asList(university,kai,kfu));
    writeGenesisToFiles(genesis, new String[]{
        "./docker/genesis-kai/genesis.block",
        "./docker/genesis-ui/genesis.block",
        "./docker/genesis-kfu/genesis.block"});

    saveKey(university.getPeerKey(),"./docker/genesis-ui");
    saveKey(kfu.getPeerKey(),"./docker/genesis-kfu");

    logger.info("Genesis and keys are generated and stored");

    File dir = new File("./docker");
    Process p = Runtime.getRuntime().exec(new String[]{"docker-compose","up", "-d"},null, dir);
    sleep(15000);
    logger.info("sleep finished");

    UniversityService service = new UniversityService(
        ChainEntitiesUtils.universitiesKeys.get(university.getName()),
        university);
    String pubkey = ChainEntitiesUtils.bytesToHex(ChainEntitiesUtils.universitiesKeys.get(university.getName()).getPublic().getEncoded());
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
    sleep(30000);
    logger.info("Sending wild tokens to applicant={}", applicant);

    service.getWildTokensTransaction(applicant, observer);
    sleep(10000);
    int balance = service.getBalanceOfApplicant(applicant, Consts.WILD_ASSET_NAME);
    logger.info("_______________________________________________");
    logger.info("Balance [{} -> {}]", Consts.WILD_ASSET_NAME, balance);
    logger.info("_______________________________________________");
    List<QryResponses.AccountAsset> assets =service.getAllAssertsOfApplicant(applicant);
    for(QryResponses.AccountAsset asset: assets){
      logger.info(String.format("%s %s",asset.getAssetId(),asset.getBalance()));
    }
  }
}
