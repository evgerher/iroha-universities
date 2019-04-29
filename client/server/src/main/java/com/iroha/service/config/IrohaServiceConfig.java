package com.iroha.service.config;

import com.iroha.dao.MongoDBConnector;
import com.iroha.service.IrohaService;
import com.iroha.service.impl.IrohaServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IrohaServiceConfig {
  @Autowired
  @Qualifier("createConnector")
  private MongoDBConnector mongoConnector;

  @Bean
  public IrohaService getIrohaService() {
    return new IrohaServiceImpl(mongoConnector);
  }
}
