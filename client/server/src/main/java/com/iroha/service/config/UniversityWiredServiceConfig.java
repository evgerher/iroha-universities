package com.iroha.service.config;

import com.iroha.dao.MongoDBConnector;
import com.iroha.service.impl.UniversityWiredService;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.*;

@Configuration
public class UniversityWiredServiceConfig {
  @Autowired
  @Qualifier("createConnector")
  private MongoDBConnector mongoConnector;

  @Bean
  @Qualifier("KAI")
  @ConditionalOnProperty(name = "university.name", havingValue = "KAI", matchIfMissing = true)
  public UniversityWiredService createUniversityServiceKAI() {
    return new UniversityWiredService("KAI", mongoConnector);
  }

  @Bean
  @Qualifier("UI")
  @ConditionalOnProperty(name = "university.name", havingValue = "UI", matchIfMissing = true)
  public UniversityWiredService createUniversityServiceUI() {
    return new UniversityWiredService("UI", mongoConnector);
  }

  @Bean
  @Qualifier("KFU")
  @ConditionalOnProperty(name = "university.name", havingValue = "KFU", matchIfMissing = true)
  public UniversityWiredService createUniversityServiceKFU() {
    return new UniversityWiredService("KFU", mongoConnector);
  }

  @Bean
  @Qualifier("SPIBI")
  @ConditionalOnProperty(name = "university.name", havingValue = "SPIBI", matchIfMissing = true)
  public UniversityWiredService createUniversityServiceSPIBI() {
    return new UniversityWiredService("SPIBI", mongoConnector);
  }
}
