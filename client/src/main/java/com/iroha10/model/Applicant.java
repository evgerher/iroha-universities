package com.iroha10.model;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;

@JsonInclude
@Data
public class Applicant {
    private String name;
    private String surname;
    private int totalPoints;
    private String pkey;
    private String pubkey;

    public Applicant(String name, String surname) {
        this.name = name;
        this.surname = surname;
    }
}
