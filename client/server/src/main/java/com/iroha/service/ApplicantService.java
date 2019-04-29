package com.iroha.service;

import com.iroha.model.applicant.requests.ApplicantRegisterRequest;
import com.iroha.model.applicant.responses.ApplicantResponse;
import com.iroha.model.applicant.requests.ExchangeSpecialityRequest;
import com.iroha.model.applicant.requests.SelectSpecialityRequest;
import com.iroha.model.applicant.TxHash;
import com.iroha.model.applicant.UserCode;

public interface ApplicantService {
  UserCode registerApplicant(ApplicantRegisterRequest request);

  UserCode getUserCode(String txHash);

  ApplicantResponse getApplicant(UserCode userCode);

  void selectSpeciality(String userCode, SelectSpecialityRequest applicantSelect);
  void exchangeSpecialities(String userCode, ExchangeSpecialityRequest applicantExchange);
}
