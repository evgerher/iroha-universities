package com.iroha.service.impl;

import com.iroha.dao.MongoDBConnector;
import com.iroha.model.Applicant;
import com.iroha.model.Asset;
import com.iroha.model.applicant.TxHash;
import com.iroha.model.applicant.requests.ApplicantRegisterRequest;
import com.iroha.model.applicant.responses.ApplicantResponse;
import com.iroha.model.applicant.requests.ExchangeSpecialityRequest;
import com.iroha.model.applicant.requests.SelectSpecialityRequest;
import com.iroha.model.applicant.UserCode;

import com.iroha.model.applicant.responses.RegistrationTx;
import com.iroha.service.ApplicantService;
import com.iroha.utils.ChainEntitiesUtils;

import iroha.protocol.QryResponses.AccountAsset;
import java.security.KeyPair;
import java.util.List;

import java.util.stream.Collectors;
import jp.co.soramitsu.iroha.java.TransactionStatusObserver;
import jp.co.soramitsu.iroha.java.detail.InlineTransactionStatusObserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ApplicantServiceImpl implements ApplicantService {
  private static final Logger logger = LoggerFactory.getLogger(ApplicantServiceImpl.class);

  private final UniversityWiredService universityService;
  private final MongoDBConnector mongoConnector;

  @Autowired
  public ApplicantServiceImpl(@Qualifier("KAI") UniversityWiredService universityService, @Qualifier("createConnector") MongoDBConnector mongoConnector) {
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
              logger.info("Successfully commited tx={}, now store applicant with usercode={}", t.getTxHash(),
                  applicant.getUserCode());
              // Store applicant into db
              mongoConnector.insertApplicant(applicant);
              mongoConnector.insertRegistrationMapping(new RegistrationTx(t.getTxHash(),
                  applicant.getUserCode()));
        })
        .onComplete(() -> {
          logger.info("Transaction completed");
        })
        .build();

    return new TxHash(universityService.createNewApplicantAccount(applicant, keys, obs));
  }

  @Override
  public UserCode getUserCode(String txHash) {
    logger.info("Request usercode mapping for txhash={}", txHash);
    TxHash tx_hash = new TxHash(txHash);
    if (universityService.getAccountStatus(tx_hash) != null) {
      RegistrationTx registrationTx = mongoConnector.getRegistrationMapping(txHash);
      return new UserCode(registrationTx.getPayload());
    } else {
      return new UserCode("Account does not exist");
    }
  }

  @Override
  public ApplicantResponse getApplicant(UserCode userCode) {
    try {
      Applicant applicant = mongoConnector.getApplicant(userCode.getUserCode());
      List<AccountAsset> irohaAssets = universityService.getAllAssertsOfApplicant(applicant);
      List<Asset> assets = irohaAssets.stream()
          .map(this::convertAsset)
          .collect(Collectors.toList());

      return new ApplicantResponse(applicant, assets);
    } catch (NullPointerException e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Applicant with provided payload not found", e);
    }
  }

  @Override
  public void selectSpeciality(String userCode, SelectSpecialityRequest applicantSelect) {

  }

  @Override
  public void exchangeSpecialities(String userCode, ExchangeSpecialityRequest applicantExchange) {

  }

  private Asset convertAsset(AccountAsset irohaAsset) {
    String name = irohaAsset.getAssetId();
    String domain = irohaAsset.getAssetId();
    int quantity = Integer.parseInt(irohaAsset.getBalance());
    return new Asset(name, domain, quantity);
  }
}
