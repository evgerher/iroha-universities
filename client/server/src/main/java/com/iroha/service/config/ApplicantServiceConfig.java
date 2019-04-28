package com.iroha.service.config;

import com.iroha.dao.MongoDBConnector;
import com.iroha.service.ApplicantService;
import com.iroha.service.impl.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.*;

@Configuration
public class ApplicantServiceConfig {

  @Autowired
  @Qualifier("createConnector")
  private MongoDBConnector mongoconnector;

  @Bean
  @ConditionalOnProperty(name = "university.name", havingValue = "KAI", matchIfMissing = true)
  public ApplicantService getApplicantService() {
    UniversityWiredService universityService = new UniversityWiredService("KAI", mongoconnector);
    return new ApplicantServiceImpl(universityService, mongoconnector);
  }
}
