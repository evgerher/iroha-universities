package com.iroha10.controller;

import com.iroha10.model.Speciality;
import com.iroha10.model.University;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/university")
public class UniversityController {
  private static final Logger logger = LoggerFactory.getLogger(UniversityController.class);
  private static HashMap<String, University> universities;

  public UniversityController() {
    universities = new HashMap<>(5); // todo: save to external storage
  }

  @RequestMapping(value = "", method = RequestMethod.POST)
  public University saveUniversity(@RequestParam String name) {
    University uni = new University(name, new ArrayList<>());
    logger.info(uni.toString());
    universities.put(name, uni);
    return uni;
  }

  @RequestMapping(value = "", method = RequestMethod.GET)
  public University getUniversity(@RequestParam String name) {
    return universities.get(name);
  }

  @RequestMapping(value = "/all", method=RequestMethod.GET)
  public University[] getUniversities() {
    return universities.values().toArray(new University[universities.values().size()]);
  }

  @RequestMapping(value = "/speciality", method = RequestMethod.POST)
  public University saveSpeciality(@RequestParam(name="university") String uniName, @RequestBody Speciality speciality) {
    try {
      University uni = universities.get(uniName);
      uni.addSpeciality(speciality);
      return uni;
    } catch (Exception e) {
      logger.error("No uni found with name={}", uniName);
      throw e;
    }
  }
}
