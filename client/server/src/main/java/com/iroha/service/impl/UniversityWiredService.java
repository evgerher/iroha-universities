package com.iroha.service.impl;

import com.iroha.model.university.University;
import com.iroha.service.UniversityService;
import java.security.KeyPair;
import org.springframework.stereotype.Service;

@Service
public class UniversityWiredService extends UniversityService {

  public UniversityWiredService(KeyPair keyPair, University university) {
    super(keyPair, university);
  }
}
