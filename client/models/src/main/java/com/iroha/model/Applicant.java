package com.iroha.model;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.security.KeyPair;
import lombok.Data;

@JsonInclude
@Data
public class Applicant {
    private static final Gson gson = new GsonBuilder().create();

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
        String id =  pubkey.replaceAll("[0-9]{1,30}",""); //for satisfying iroha accountId/name requirements
        if (id.length()>30){
            id = id.substring(0,30);
        }
        return id;
    }
    
    public void setPubkey(String pubkey) {
        this.pubkey = pubkey;
        this.userCode = getId();
    }

    @Override
    public String toString() {
        return gson.toJson(this);
    }
}
