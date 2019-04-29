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


    public String getId(){
        String id =  pubkey.replaceAll("[0-9]{1,30}","");
        if (id.length()>30){
            id = id.substring(0,30);
        }
        return id;
    }
    
    public void setPubkey(String pubkey) {
        this.pubkey = pubkey;
        this.userCode = getId();
    }
}
