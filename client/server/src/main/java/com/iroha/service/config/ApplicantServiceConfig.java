package com.iroha.service.config;

import com.iroha.dao.MongoDBConnector;
import com.iroha.model.university.University;
import com.iroha.service.ApplicantService;
import com.iroha.service.impl.ApplicantServiceImpl;
import com.iroha.service.impl.UniversityWiredService;

import java.security.KeyPair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicantServiceConfig {

  @Autowired
  @Qualifier("createConnector")
  private MongoDBConnector mongoconnector;

  @Bean
  @ConditionalOnProperty(name = "university.name", havingValue = "KAI", matchIfMissing = true)
  public ApplicantService getApplicantService() {
    KeyPair keys = mongoconnector.getUniversityKeys("KAI");
    University uni = mongoconnector.getUniversity("KAI");

    UniversityWiredService universityService = new UniversityWiredService(keys, uni);
    return new ApplicantServiceImpl(universityService, mongoconnector);
  }
}
