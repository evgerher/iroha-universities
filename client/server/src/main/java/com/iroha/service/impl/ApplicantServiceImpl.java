package com.iroha.service.impl;

import com.iroha.model.applicant.ResponseApplicant;
import com.iroha.model.applicant.ApplicantExchangeSpeciality;
import com.iroha.model.applicant.ApplicantSelectSpeciality;
import com.iroha.model.applicant.UserCode;
import com.iroha.service.ApplicantService;

public class ApplicantServiceImpl implements ApplicantService {

  @Override
  public UserCode registerApplicant(String name) {
    return null;
  }

  @Override
  public ResponseApplicant getApplicant(UserCode userCode) {
    return null;
  }

  @Override
  public void selectSpeciality(String userCode, ApplicantSelectSpeciality applicantSelect) {

  }

  @Override
  public void exchangeSpecialities(String userCode, ApplicantExchangeSpeciality applicantExchange) {

  }
}
