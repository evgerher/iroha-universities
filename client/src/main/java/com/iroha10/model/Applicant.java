package com.iroha10.model;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;

@JsonInclude
@Data
@AllArgsConstructor
public class Applicant {
    private String name;
    private String surname;
    private int totalPoints;




}
