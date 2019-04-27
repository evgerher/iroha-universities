package com.iroha10.service;

import com.iroha10.model.applicant.Applicant;
import com.iroha10.model.applicant.ApplicantExchangeSpeciality;
import com.iroha10.model.applicant.ApplicantSelectSpeciality;
import com.iroha10.model.applicant.UserCode;

public interface ApplicantService {
  UserCode registerApplicant(String name);
  Applicant getApplicant(UserCode userCode);

  void selectSpeciality(String userCode, ApplicantSelectSpeciality applicantSelect);
  void exchangeSpecialities(String userCode, ApplicantExchangeSpeciality applicantExchange);
}
