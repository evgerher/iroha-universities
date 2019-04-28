package com.iroha.controller;

import com.iroha.model.applicant.requests.ApplicantRegisterRequest;
import com.iroha.model.applicant.responses.ResponseApplicant;
import com.iroha.model.applicant.requests.ExchangeSpecialityRequest;
import com.iroha.model.applicant.requests.SelectSpecialityRequest;
import com.iroha.model.applicant.TxHash;
import com.iroha.model.applicant.UserCode;
import com.iroha.service.ApplicantService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/applicant")
public class ApplicantController {
  private static final Logger logger = LoggerFactory.getLogger(ApplicantController.class);
  private final ApplicantService applicantService = null; // todo me

  @RequestMapping(value="/register", method = RequestMethod.POST)
  public TxHash registerApplicant(@RequestBody ApplicantRegisterRequest request) {
    logger.info("Register new applicant={}", request);
    return applicantService.registerApplicant(request);
  }

  @RequestMapping(value = "", method = RequestMethod.GET)
  public ResponseApplicant getApplicant(@RequestHeader(value="User-Code") UserCode userCode) {
    logger.info("Get applicant with usercode={}", userCode);
    return applicantService.getApplicant(userCode);
  }

  @RequestMapping(value="/select", method = RequestMethod.POST)
  public void selectSpeciality(@RequestHeader(value="User-Code") String userCode,
      @RequestBody SelectSpecialityRequest applicantSelect) {
    // todo: return track code ???

    logger.info("ResponseApplicant with usercode={} selects speciality code={}, university={}",
        userCode, applicantSelect.getCode(), applicantSelect.getUniversity());
    applicantService.selectSpeciality(userCode, applicantSelect);
  }

  @RequestMapping(value="/exchange", method = RequestMethod.POST)
  public void exchangeAssets(@RequestHeader(value="User-Code") String userCode,
      @RequestBody ExchangeSpecialityRequest applicantExchange) {
    // todo: return track code ???

    logger.info("ResponseApplicant with usercode={} exchanges speciality from={}, to={}",
        userCode, applicantExchange.getFrom(), applicantExchange.getTo());
    applicantService.exchangeSpecialities(userCode, applicantExchange);
  }
}
