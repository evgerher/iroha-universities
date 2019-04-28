package com.iroha.controller;

import com.iroha.model.university.*;
import com.iroha.dao.MongoDBConnector;

import com.iroha.utils.ChainEntitiesUtils;
import java.security.KeyPair;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/university")
public class UniversityController {
  private static final Logger logger = LoggerFactory.getLogger(UniversityController.class);
  private final MongoDBConnector mongoConnector;

  public UniversityController() {
    mongoConnector = new MongoDBConnector();
  }

  @RequestMapping(value = "", method = RequestMethod.POST)
  @ResponseStatus(HttpStatus.OK)
  public void saveUniversity(@RequestBody University university) {
    university.validate();
    logger.info(university.toString());
    KeyPair keys = ChainEntitiesUtils.generateKey();
    mongoConnector.insertUniversity(university);
    mongoConnector.insertUniversityKeys(university, keys);
  }

  @RequestMapping(value = "", method = RequestMethod.GET)
  public University getUniversity(@RequestParam String name) {
    return mongoConnector.getUniversity(name);
  }

  @RequestMapping(value = "/all", method=RequestMethod.GET)
  public List<University> getUniversities() {
    return mongoConnector.getUniversities();
  }

  @RequestMapping(value = "/speciality", method = RequestMethod.POST)
  public void saveSpeciality(@RequestBody Speciality speciality) {
    try {
      speciality.validate();
      mongoConnector.insertSpeciality(speciality);
    } catch (Exception e) {
      logger.error("Exception occured, e={}", e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }
  }

  @RequestMapping(value = "/speciality", method = RequestMethod.GET)
  public List<Speciality> saveSpeciality(@RequestParam(required = false) String code, @RequestParam(required = false) String university) {
    try {
      return mongoConnector.getSpecialities(code, university);
    } catch (Exception e) {
      logger.error("Exception occured, e={}", e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }
  }

  @RequestMapping(value = "/speciality/all", method = RequestMethod.GET)
  public List<Speciality> saveSpeciality() {
    try {
      return mongoConnector.getSpecialities();
    } catch (Exception e) {
      logger.error("Exception occured, e={}", e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }
  }
}
