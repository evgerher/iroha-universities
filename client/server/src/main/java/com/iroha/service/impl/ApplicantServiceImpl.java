package com.iroha.service.impl;

import com.iroha.dao.MongoDBConnector;
import com.iroha.model.Applicant;
import com.iroha.model.Asset;
import com.iroha.model.applicant.TxHash;
import com.iroha.model.applicant.requests.ApplicantRegisterRequest;
import com.iroha.model.applicant.responses.ResponseApplicant;
import com.iroha.model.applicant.requests.ExchangeSpecialityRequest;
import com.iroha.model.applicant.requests.SelectSpecialityRequest;
import com.iroha.model.applicant.UserCode;

import com.iroha.service.ApplicantService;
import com.iroha.service.UniversityService;
import com.iroha.utils.ChainEntitiesUtils;

import java.security.KeyPair;
import java.util.List;

import jp.co.soramitsu.iroha.java.TransactionStatusObserver;
import jp.co.soramitsu.iroha.java.detail.InlineTransactionStatusObserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApplicantServiceImpl implements ApplicantService {
  private static final Logger logger = LoggerFactory.getLogger(ApplicantServiceImpl.class);

  private final UniversityService universityService;
  private final MongoDBConnector mongoConnector;

  public ApplicantServiceImpl(UniversityService universityService, MongoDBConnector mongoConnector) {
    this.universityService = universityService;
    this.mongoConnector = mongoConnector;
  }

  /**
   * Method returns transaction hash to be viewed later
   * On transaction commited stores a member into mongodb, otherwise aborts
   * @return
   */
  @Override
  public TxHash registerApplicant(ApplicantRegisterRequest request) {
    Applicant applicant = new Applicant(request.getName(), request.getSurname());

    // Generate keys
    KeyPair keys = ChainEntitiesUtils.generateKey();
    applicant.setPubkey(ChainEntitiesUtils.bytesToHex(keys.getPublic().getEncoded()));
    applicant.setPkey(ChainEntitiesUtils.bytesToHex(keys.getPrivate().getEncoded()));

    InlineTransactionStatusObserver obs = TransactionStatusObserver.builder()
        .onTransactionFailed(t -> {
            logger.error("Transaction with txhash={} have failed", t.getTxHash());
        })
        .onError(e -> logger.error("Error occured={}", e))
        .onTransactionCommitted((t) -> {
              logger.info("Successfully commited tx={}, now store applicant with usercode={}", t,
                  applicant.getUserCode());
              // Store applicant into db
              mongoConnector.insertApplicant(applicant);
        })
        .onComplete(() -> {
          logger.info("Transaction completed");
        })
        .build();

    return new TxHash(universityService.createNewApplicantAccount(applicant, keys, obs));
  }

  @Override
  public ResponseApplicant getApplicant(UserCode userCode) {
    Applicant applicant = mongoConnector.getApplicant(userCode.getUserCode());


    // todo: retrieve assets from iroha
    List<Asset> assets = null;

    return new ResponseApplicant(applicant, assets);
  }

  @Override
  public void selectSpeciality(String userCode, SelectSpecialityRequest applicantSelect) {

  }

  @Override
  public void exchangeSpecialities(String userCode, ExchangeSpecialityRequest applicantExchange) {

  }
}
