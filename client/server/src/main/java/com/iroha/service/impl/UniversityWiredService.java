package com.iroha.service.impl;

import com.iroha.dao.MongoDBConnector;
import com.iroha.model.Applicant;
import com.iroha.model.applicant.TxHash;
import com.iroha.model.applicant.requests.SelectSpecialityRequest;
import com.iroha.model.applicant.responses.RegistrationTx;
import com.iroha.model.university.Speciality;
import com.iroha.model.university.University;
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

/**
 * Class wrapper around UniversityServiceImpl (provided by blockchain module)
 */
public class UniversityWiredService {

  private static final Logger logger = LoggerFactory.getLogger(UniversityWiredService.class);

  private final MongoDBConnector mongoConnector;
  private final UniversityServiceImpl universityService;

  public UniversityWiredService(String uniName,
      @Qualifier("createConnector") MongoDBConnector mongoConnector) {
    this.mongoConnector = mongoConnector;
    University university = mongoConnector.getUniversity(uniName);
    KeyPair keys = mongoConnector.getUniversityKeys(uniName);
    universityService = new UniversityServiceImpl(keys, university);
  }

  /**
   * Method instantiates new applicant creation in the chain & if success - stores in mongodb
   *
   * @param applicant candidate
   * @param keys KeyPair generated
   * @param observer on events occur during initialization
   * @return userCode of probably created applicant
   */
  public String createNewApplicantAccount(Applicant applicant, KeyPair keys,
      InlineTransactionStatusObserver observer) {
    return universityService.createNewApplicantAccount(applicant, keys, observer);
  }

  /**
   * Method returns all assets
   */
  public List<AccountAsset> getAllAssertsOfApplicant(Applicant applicant) {
    logger.info("Request assets of applicant={}", applicant.getUserCode());
    return universityService.getAllAssertsOfApplicant(applicant);
  }

  /**
   * Method returns mapping from mongodb if such exists (currently not used)
   *
   * @param txhash hash of the transaction
   * @return user account as json
   */
  @Deprecated
  public String getAccountStatus(TxHash txhash) {
    try {
      logger.info("Request account by txhash={}", txhash);
      RegistrationTx regTx = mongoConnector.getRegistrationMapping(txhash.getTxhash());
      String accountId = regTx.getPayload();
      Account account = universityService.getAccount(accountId);
      return account.getJsonData();
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * Method utilizes two atomic batches: select university & select speciality If university is
   * already selected - applicant assets won't change If speciality is already selected - applicant
   * assets won't change If error occured - applicant assets won't change
   *
   * @param userCode of the applicant
   * @param applicantSelect selected speciality
   */
  public void selectSpeciality(String userCode, SelectSpecialityRequest applicantSelect) {
    Applicant applicant = mongoConnector.getApplicant(userCode);
    University uni = mongoConnector.getUniversity(applicantSelect.getUniversity());
    KeyPair uniKey = mongoConnector.getUniversityKeys(applicantSelect.getUniversity());
    KeyPair applicantKey = ChainEntitiesUtils.getKeys(applicant);
    Speciality speciality = mongoConnector
        .getSpecialities(applicantSelect.getCode(), applicantSelect.getUniversity()).get(0);

    universityService.chooseUniversity(applicant, applicantKey, getDefaultObserver(), uni, uniKey);
    universityService
        .chooseSpeciality(applicant, speciality, getDefaultObserver(), applicantKey, uni, uniKey);
  }

  /**
   * Method generates default logging observer
   */
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
        .onNotReceived(e -> logger.info("not received with: " + e))
        .onEnoughSignaturesCollected(t -> logger.info("sigs collected: " + t))
        .onMstExpired(t -> logger.info("mst expired: " + t))
        .onMstPending(t -> logger.info("pending: " + t))
        .onStatelessValidationSuccess(t -> logger.info("sls val: " + t))
        .onUnrecognizedStatus(t -> logger.info("unrecognized" + t))
        .onStatefulValidationSuccess(t -> logger.info("slf val: " + t))
        .onUnrecognizedStatus(t -> logger.info("HZ"))
        .onRejected(t -> logger.info("rejected" + t))
        .build();
  }
}
