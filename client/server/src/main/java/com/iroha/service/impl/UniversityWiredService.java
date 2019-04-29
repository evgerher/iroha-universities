package com.iroha.service.impl;

import com.iroha.dao.MongoDBConnector;
import com.iroha.model.Applicant;
import com.iroha.model.applicant.TxHash;
import com.iroha.model.applicant.requests.SelectSpecialityRequest;
import com.iroha.model.applicant.responses.RegistrationTx;
import com.iroha.model.university.Speciality;
import com.iroha.model.university.University;
import com.iroha.service.UniversityService;
import com.iroha.utils.ChainEntitiesUtils;
import io.reactivex.Observer;
import iroha.protocol.QryResponses.Account;
import iroha.protocol.QryResponses.AccountAsset;
import java.security.KeyPair;
import java.util.List;
import jp.co.soramitsu.iroha.java.TransactionStatusObserver;
import jp.co.soramitsu.iroha.java.detail.InlineTransactionStatusObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;

public class UniversityWiredService {
  private static final Logger logger = LoggerFactory.getLogger(UniversityWiredService.class);

  private final MongoDBConnector mongoConnector;
  private final UniversityService universityService;

  public UniversityWiredService(String uniName, @Qualifier("createConnector") MongoDBConnector mongoConnector) {
    this.mongoConnector = mongoConnector;
    University university = mongoConnector.getUniversity(uniName);
    KeyPair keys = mongoConnector.getUniversityKeys(uniName);
    universityService = new UniversityService(keys, university);
  }

  public String createNewApplicantAccount(Applicant applicant, KeyPair keys, InlineTransactionStatusObserver observer) {
    String userCode = universityService.createNewApplicantAccount(applicant, keys, observer);
    return userCode;
  }

  public List<AccountAsset> getAllAssertsOfApplicant(Applicant applicant) {
    return universityService.getAllAssertsOfApplicant(applicant);
  }

  public String getAccountStatus(TxHash txhash) {
    try {
      RegistrationTx regTx = mongoConnector.getRegistrationMapping(txhash.getTxhash());
      String accountId = regTx.getPayload();
      Account account = universityService.getAccount(accountId);
      return account.getJsonData();
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  public void selectSpeciality(String userCode, SelectSpecialityRequest applicantSelect) {
    Applicant applicant = mongoConnector.getApplicant(userCode);
    University uni = mongoConnector.getUniversity(applicantSelect.getUniversity());
    KeyPair uniKey = mongoConnector.getUniversityKeys(applicantSelect.getUniversity());
    KeyPair applicantKey = ChainEntitiesUtils.getKeys(applicant);
    Speciality speciality = mongoConnector.getSpecialities(applicantSelect.getCode(), applicantSelect.getUniversity()).get(0);

    universityService.chooseUniversity(applicant, applicantKey, getDefaultObserver(), uni, uniKey);
    universityService.chooseSpeciality(applicant, speciality, getDefaultObserver(), applicantKey, uni, uniKey);
  }

  public static Observer getDefaultObserver() {
    return TransactionStatusObserver.builder()
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
  }
}
