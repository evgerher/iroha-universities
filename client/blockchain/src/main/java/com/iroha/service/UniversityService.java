package com.iroha.service;

import com.iroha.model.Applicant;
import com.iroha.model.parameter_objects.SpecialityChangeParameters;
import com.iroha.model.university.Speciality;
import com.iroha.model.university.University;
import io.reactivex.Observer;
import jp.co.soramitsu.iroha.java.detail.InlineTransactionStatusObserver;

import java.security.KeyPair;

public interface UniversityService {
    String createNewApplicantAccount(Applicant applicant, KeyPair keys, InlineTransactionStatusObserver observer);

    void chooseUniversity(Applicant applicant, KeyPair applicantKeyPair, Observer observer, University university, KeyPair universityKeyPair);

    void chooseSpeciality(Applicant applicant, Speciality speciality, Observer observer, KeyPair applicantKeyPair, University university, KeyPair universityKeyPair);

    void returnAllUniversityTokens(Applicant applicant, University university);

    void changeSpeciality(Applicant applicant,
                          SpecialityChangeParameters specialityChangeParameters, KeyPair aplicantKey,
                          KeyPair destUniKey, Observer observer);
}
