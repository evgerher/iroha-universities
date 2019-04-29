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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.List;

import iroha.protocol.QryResponses;
import jp.co.soramitsu.iroha.java.IrohaAPI;
import jp.co.soramitsu.iroha.java.Transaction;
import jp.co.soramitsu.iroha.java.TransactionBuilder;
import jp.co.soramitsu.iroha.java.TransactionStatusObserver;
import jp.co.soramitsu.iroha.testcontainers.IrohaContainer;
import jp.co.soramitsu.iroha.testcontainers.PeerConfig;
import lombok.val;

import static com.iroha.utils.ChainEntitiesUtils.*;
import static java.lang.Thread.sleep;

public class IrohaMain {

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
    writeGenesisToFile(genesis,"./docker/genesis-kai/genesis.block");
    writeGenesisToFile(genesis,"./docker/genesis-ui/genesis.block");
    writeGenesisToFile(genesis,"./docker/genesis-kfu/genesis.block");

    saveKey(university.getPeerKey(),"./docker/genesis-ui");
    saveKey(kfu.getPeerKey(),"./docker/genesis-kfu");
    System.out.println("genesis generated");
    File dir = new File("./docker");
    Process p = Runtime.getRuntime().exec(new String[]{"docker-compose","up", "-d"},null, dir);
    sleep(15000);
    System.out.println("sleep finished");
    UniversityService service = new UniversityService(
        ChainEntitiesUtils.universitiesKeys.get(university.getName()),
        university);
    System.out.println(ChainEntitiesUtils.bytesToHex(ChainEntitiesUtils.universitiesKeys.get(university.getName()).getPublic().getEncoded()
    ));

    val observer =TransactionStatusObserver.builder()
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
            .onComplete(() -> System.out.println("Complete0"))
            .onTransactionSent(() -> System.out.println("sent"))
            .onNotReceived(e -> System.out.println("not received with: "+ e))
            .onEnoughSignaturesCollected(t -> System.out.println("sigs collected: "+ t))
            .onMstExpired(t -> System.out.println("mst expired: "+ t))
            .onMstPending(t -> System.out.println("pending: "+ t))
            .onStatelessValidationSuccess(t -> System.out.println("sls val: "+ t))
            .onUnrecognizedStatus(t -> System.out.println("unrecognized"+t))
            .onStatefulValidationSuccess(t -> System.out.println("slf val: "+ t))
            .onUnrecognizedStatus(t -> System.out.println("HZ"))
            .onRejected(t -> System.out.println("rejected" + t))
            .build();





    Applicant applicant = new Applicant("name", "surname");

    KeyPair applicantKeys = ChainEntitiesUtils.generateKey();
    applicant.setPkey(applicantKeys.getPrivate().toString());
    applicant.setPubkey(ChainEntitiesUtils.bytesToHex(applicantKeys.getPublic().getEncoded()));

    String uiAccountId = ChainEntitiesUtils.getAccountId(ChainEntitiesUtils.getUniversityAccountName(university),ChainEntitiesUtils.getUniversityDomain(university));
    val studAccountId = ChainEntitiesUtils.getAccountId(ChainEntitiesUtils.getApplicantAccountName(applicant), ChainEntitiesUtils.Consts.UNIVERSITIES_DOMAIN);
//    val transaction = Transaction.builder(uiAccountId)
//            .transferAsset(uiAccountId,studAccountId, ChainEntitiesUtils.getAssetId(speciality.getName(), getUniversityDomain(university)),"",new BigDecimal(1))
//            .sign(universitiesKeys.get(university.getName()))
//            .build();
    IrohaAPI api = IrohaApiSingletone.getIrohaApiInstance();


    //api.transaction(transaction).publish().blockingSubscribe(observer);
    val observer1 =TransactionStatusObserver.builder()
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
            .onComplete(() -> System.out.println("Complete1"))
            .build();

    service.createNewApplicantAccount(applicant, applicantKeys, observer1);
    for(int i =0 ; i < 10; i++  ) {
      val observer2 =TransactionStatusObserver.builder()
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
              .onComplete(() -> System.out.println("Complete2"))
              .build();
      applicant.setName("name"+ Integer.toString(i));
      applicantKeys = ChainEntitiesUtils.generateKey();
      applicant.setPkey(applicantKeys.getPrivate().toString());
      applicant.setPubkey(ChainEntitiesUtils.bytesToHex(applicantKeys.getPublic().getEncoded()));
      service.createNewApplicantAccount(applicant, applicantKeys, observer2);
    }

    sleep(20000);
    service.getWildTokensTransaction(applicant, observer);
    sleep(10000);
    int balance = service
        .getBalanceOfApplicant(applicant, ChainEntitiesUtils.Consts.WILD_ASSET_NAME);
    System.out.println("_______________________________________________");
    System.out.println(balance);
    System.out.println("_______________________________________________");
    List<QryResponses.AccountAsset> assets =service.getAllAssertsOfApplicant(applicant);
    for(QryResponses.AccountAsset asset: assets){
      System.out.println(String.format("%s %s",asset.getAssetId(),asset.getBalance()));
    }
    service.chooseUniversity(applicant,applicantKeys,observer, university, universitiesKeys.get(university.getName()));
   assets =service.getAllAssertsOfApplicant(applicant);
      for(QryResponses.AccountAsset asset: assets){
          System.out.println(String.format("%s %s",asset.getAssetId(),asset.getBalance()));
      }
  }




  private static void writeGenesisToFile(BlockOuterClass.Block genesis, String path) throws FileNotFoundException {
    FileOutputStream file = new FileOutputStream(path);
    try {
      file.write(JsonFormat.printer().print(genesis).getBytes());
      file.flush();
      file.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void saveKey(KeyPair keyPair, String path) throws FileNotFoundException {
    FileOutputStream filePub = new FileOutputStream(path+"/node.pub");
    FileOutputStream filePriv = new FileOutputStream(path+"/node.priv");

    try {
      filePub.write(bytesToHex(keyPair.getPublic().getEncoded()).getBytes());
      filePub.flush();
      filePub.close();
      filePriv.write(bytesToHex(keyPair.getPrivate().getEncoded()).getBytes());
      filePriv.flush();
      filePriv.close();
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
