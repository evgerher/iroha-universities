package com.iroha.service;

import com.iroha.model.applicant.ResponseApplicant;
import com.iroha.model.applicant.ApplicantExchangeSpeciality;
import com.iroha.model.applicant.ApplicantSelectSpeciality;
import com.iroha.model.applicant.UserCode;

public interface ApplicantService {
  UserCode registerApplicant(String name);
  ResponseApplicant getApplicant(UserCode userCode);

  void selectSpeciality(String userCode, ApplicantSelectSpeciality applicantSelect);
  void exchangeSpecialities(String userCode, ApplicantExchangeSpeciality applicantExchange);
}
