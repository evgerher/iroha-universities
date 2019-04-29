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
  @Qualifier("kai")
  @ConditionalOnProperty(name = "university.name", havingValue = "kai", matchIfMissing = true)
  public UniversityWiredService createUniversityServiceKAI() {
    return new UniversityWiredService("kai", mongoConnector);
  }

  @Bean
  @Qualifier("ui")
  @ConditionalOnProperty(name = "university.name", havingValue = "ui", matchIfMissing = true)
  public UniversityWiredService createUniversityServiceUI() {
    return new UniversityWiredService("ui", mongoConnector);
  }

  @Bean
  @Qualifier("kfu")
  @ConditionalOnProperty(name = "university.name", havingValue = "kfu", matchIfMissing = true)
  public UniversityWiredService createUniversityServiceKFU() {
    return new UniversityWiredService("kfu", mongoConnector);
  }

  @Bean
  @Qualifier("spibi")
  @ConditionalOnProperty(name = "university.name", havingValue = "spibi", matchIfMissing = true)
  public UniversityWiredService createUniversityServiceSPIBI() {
    return new UniversityWiredService("spibi", mongoConnector);
  }
}
