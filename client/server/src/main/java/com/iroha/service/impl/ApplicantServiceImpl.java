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
import com.iroha.model.university.Speciality;
import com.iroha.model.university.University;
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
  public ApplicantServiceImpl(@Qualifier("kai") UniversityWiredService universityService, @Qualifier("createConnector") MongoDBConnector mongoConnector) {
    this.universityService = universityService;
    this.mongoConnector = mongoConnector;
  }

  /**
   * Method returns transaction hash to be viewed later
   * On transaction commited stores a member into mongodb, otherwise aborts
   * @return
   */
  @Override
  public UserCode registerApplicant(ApplicantRegisterRequest request) {
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
          logger.info("Register applicant completed");
        })
        .build();

    return new UserCode(universityService.createNewApplicantAccount(applicant, keys, obs));
  }

  /**
   * Method returns user code if such mapping exists (txhash -> usercode)
   * Currently not used
   * @param txHash
   * @return
   */
  @Override
  @Deprecated
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

  /**
   * Retrieve applicant and its assets
   * @param userCode
   * @return
   */
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
    String uniName = applicantSelect.getUniversity();
    String specCode = applicantSelect.getCode();

    Applicant applicant = mongoConnector.getApplicant(userCode);
    University uni = mongoConnector.getUniversity(uniName);
    Speciality speciality = mongoConnector.getSpecialities(specCode, uniName).get(0);

    universityService.selectSpeciality(applicant, uni, speciality);
  }

  /**
   * Method swaps specialities between universities, if the speciality is the same
   * @param userCode of the applicant
   * @param applicantExchange object with swap items
   */
  @Override
  public void exchangeSpecialities(String userCode, ExchangeSpecialityRequest applicantExchange) {
    // todo: add support for different exchanges
    Applicant applicant = mongoConnector.getApplicant(userCode);
    String uniFrom = applicantExchange.getFrom().getUniversity();
    String uniTo = applicantExchange.getTo().getUniversity();
    String specFrom = applicantExchange.getFrom().getCode();
    String specTo = applicantExchange.getTo().getCode();

    University universityFrom = mongoConnector.getUniversity(uniFrom);
    University universityTo = mongoConnector.getUniversity(uniTo);
    Speciality specialityFrom = mongoConnector.getSpecialities(specFrom, uniFrom).get(0); // todo: looks bad
    Speciality specialityTo = mongoConnector.getSpecialities(specTo, uniTo).get(0);

    universityService.swapUniversity(applicant, universityFrom, specialityFrom,
        universityTo, specialityTo);
  }

  /**
   * Convert internal iroha AccountAsset into user-friendly Asset
   * @param irohaAsset
   * @return
   */
  private Asset convertAsset(AccountAsset irohaAsset) {
    String[] strings = irohaAsset.getAssetId().split("#");
    String name = strings[0];
    String domain = strings[1];
    int quantity = Integer.parseInt(irohaAsset.getBalance());
    return new Asset(name, domain, quantity);
  }
}
