package com.iroha.model;


import com.fasterxml.jackson.annotation.JsonInclude;
import java.security.KeyPair;
import lombok.Data;

@JsonInclude
@Data
public class Applicant {
    private String name;
    private String surname;
    private int totalPoints;
    private String pkey;
    private String pubkey;
    private String userCode;

    public Applicant(String name, String surname) {
        this.name = name;
        this.surname = surname;
    }

    public void setPubkey(String pubkey) {
        this.pubkey = pubkey;
        this.userCode = getId();
    }

    public String getId(){
        return pubkey.replaceAll("[0-9]","");
    }
}
