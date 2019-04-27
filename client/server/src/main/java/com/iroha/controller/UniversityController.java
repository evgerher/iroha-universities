package com.iroha.controller;

import com.iroha.model.university.Speciality;
import com.iroha.model.university.University;

import com.iroha.dao.MongoDBConnector;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
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
    mongoConnector.insertUniversity(university);
  }

  @RequestMapping(value = "", method = RequestMethod.GET)
  public University getUniversity(@RequestParam String name) {
    return mongoConnector.getUniversity(name);
  }

  @RequestMapping(value = "/all", method=RequestMethod.GET)
  public List<University> getUniversities() {
    return mongoConnector.getUniversities();
  }

  @RequestMapping(value = "/{shortName}", method=RequestMethod.GET)
  public University getUniversityByName(@PathVariable("shortName") String uniName) {
    return mongoConnector.getUniversity(uniName);
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