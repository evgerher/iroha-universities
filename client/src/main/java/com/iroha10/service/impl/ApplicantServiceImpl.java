package com.iroha10.service.impl;

import com.iroha10.model.applicant.ResponseApplicant;
import com.iroha10.model.applicant.ApplicantExchangeSpeciality;
import com.iroha10.model.applicant.ApplicantSelectSpeciality;
import com.iroha10.model.applicant.UserCode;
import com.iroha10.service.ApplicantService;

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
