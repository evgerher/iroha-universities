package com.iroha.service;

import com.iroha.model.applicant.requests.ApplicantRegisterRequest;
import com.iroha.model.applicant.responses.ResponseApplicant;
import com.iroha.model.applicant.requests.ExchangeSpecialityRequest;
import com.iroha.model.applicant.requests.SelectSpecialityRequest;
import com.iroha.model.applicant.TxHash;
import com.iroha.model.applicant.UserCode;

public interface ApplicantService {
  TxHash registerApplicant(ApplicantRegisterRequest request);
  ResponseApplicant getApplicant(UserCode userCode);

  void selectSpeciality(String userCode, SelectSpecialityRequest applicantSelect);
  void exchangeSpecialities(String userCode, ExchangeSpecialityRequest applicantExchange);
}
