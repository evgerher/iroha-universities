package com.iroha.controller;

import com.iroha.model.applicant.requests.ApplicantRegisterRequest;
import com.iroha.model.applicant.requests.SelectUniversityRequest;
import com.iroha.model.applicant.responses.ApplicantResponse;
import com.iroha.model.applicant.requests.ExchangeSpecialityRequest;
import com.iroha.model.applicant.requests.SelectSpecialityRequest;
import com.iroha.model.applicant.TxHash;
import com.iroha.model.applicant.UserCode;
import com.iroha.service.ApplicantService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.*;
import org.springframework.web.bind.annotation.*;
import org.testcontainers.shaded.javax.ws.rs.QueryParam;

@RestController
@RequestMapping("/applicant")
public class ApplicantController { // todo: expected to be used only after blockchain start
  private static final Logger logger = LoggerFactory.getLogger(ApplicantController.class);
  private final ApplicantService applicantService;

  @Autowired
  public ApplicantController(@Qualifier("getApplicantService") ApplicantService applicantService) {
    this.applicantService = applicantService;
  }

  @RequestMapping(value="/register", method = RequestMethod.POST)
  public UserCode registerApplicant(@RequestBody ApplicantRegisterRequest request) {
    logger.info("Register new applicant={}", request);
    return applicantService.registerApplicant(request);
  }

  @RequestMapping(value="/registration-result", method = RequestMethod.GET)
  public UserCode registrationResult(@QueryParam("txhash") String txhash) {
    logger.info("Request the result of registration on txhash={}", txhash);
    return applicantService.getUserCode(txhash);
  }

  @RequestMapping(value = "", method = RequestMethod.GET)
  public ApplicantResponse getApplicant(@RequestHeader(value="User-Code") UserCode userCode) {
    logger.info("Get applicant with usercode={}", userCode);
    return applicantService.getApplicant(userCode);
  }

  @RequestMapping(value="/select-speciality", method = RequestMethod.POST)
  public void selectSpeciality(@RequestHeader(value="User-Code") String userCode,
      @RequestBody SelectSpecialityRequest applicantSelect) {
    // todo: return track code ???

    logger.info("SelectSpecialityRequest={} with usercode={}", applicantSelect, userCode);
    applicantService.selectSpeciality(userCode, applicantSelect);
  }

  @RequestMapping(value="/select-university", method = RequestMethod.POST)
  public void selectUniversity(@RequestHeader(value="User-Code") String userCode,
      @RequestBody SelectUniversityRequest applicantSelect) {
    // todo: return track code ???

    logger.info("SelectUniversityRequest={} with usercode={}", applicantSelect, userCode);
    applicantService.selectUniversity(userCode, applicantSelect);
  }


  @RequestMapping(value="/exchange", method = RequestMethod.POST)
  public void exchangeAssets(@RequestHeader(value="User-Code") String userCode,
      @RequestBody ExchangeSpecialityRequest applicantExchange) {
    // todo: return track code ???

    logger.info("ExchangeSpecialityRequest={} with usercode={}", applicantExchange, userCode);
    applicantService.exchangeSpecialities(userCode, applicantExchange);
  }
}
