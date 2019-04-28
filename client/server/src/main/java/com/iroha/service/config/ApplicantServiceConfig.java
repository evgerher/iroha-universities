package com.iroha.service.config;

import com.iroha.dao.MongoDBConnector;
import com.iroha.service.ApplicantService;
import com.iroha.service.impl.ApplicantServiceImpl;
import com.iroha.service.impl.UniversityWiredService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicantServiceConfig {

  @Autowired
  @Qualifier("createConnector")
  private MongoDBConnector mongoconnector;

  @Bean
//  @ConditionalOnProperty(name = "language.name", havingValue = "english", matchIfMissing = true)
  public ApplicantService getApplicantService() {
    UniversityWiredService universityService = new UniversityWiredService();
    return new ApplicantServiceImpl(universityService, mongoconnector);
  }
}
