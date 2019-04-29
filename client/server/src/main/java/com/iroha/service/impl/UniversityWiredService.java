package com.iroha.service.impl;

import com.iroha.dao.MongoDBConnector;
import com.iroha.model.Applicant;
import com.iroha.model.applicant.TxHash;
import com.iroha.model.applicant.responses.RegistrationTx;
import com.iroha.model.university.University;
import com.iroha.service.UniversityService;
import iroha.protocol.QryResponses.Account;
import iroha.protocol.QryResponses.AccountAsset;
import java.security.KeyPair;
import java.util.List;
import jp.co.soramitsu.iroha.java.detail.InlineTransactionStatusObserver;
import org.springframework.beans.factory.annotation.Qualifier;

public class UniversityWiredService {
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
}
