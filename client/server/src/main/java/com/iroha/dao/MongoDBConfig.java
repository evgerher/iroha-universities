package com.iroha.dao;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MongoDBConfig {
  @Bean
  public MongoDBConnector createConnector() {
    return new MongoDBConnector();
  }
}
