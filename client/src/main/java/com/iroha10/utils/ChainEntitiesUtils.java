package com.iroha10.utils;

import com.iroha10.model.Applicant;
import com.iroha10.model.University;
import jp.co.soramitsu.crypto.ed25519.Ed25519Sha3;

import java.security.KeyPair;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChainEntitiesUtils {

    public static volatile Map<String,KeyPair> universitiesKeys;

    public static String getAssetName(String specialityName,String universityName){
        return String.format("%s%s",specialityName, universityName);
    }
    public static String getAssetId(String specialityName,String universityName){
        return String.format("%s#%s",specialityName, universityName);
    }
    public static Map<String,KeyPair> generateKeys(List<University> universities){
        Ed25519Sha3 crypto = new Ed25519Sha3();
        universitiesKeys = new HashMap<>();
        for(University university: universities){
            universitiesKeys.put(university.getName(),crypto.generateKeypair());
        }
        return universitiesKeys;
    }
    public static String getUniversityDomain(University university){
        return university.getName()+"domain";
    }

    public static String getUniversityRole(University university){
        return university.getName()+"role";
    }

    public static String getUniversityAccountName(University university){
        return university.getName()+"_account";
    }

    public static String getAccountId(String accountName, String domain){
        return String.format("%s@%s",accountName,domain);
    }
    public static String getApplicantAccountName(Applicant applicant, University university){
        return applicant.toString();
    }
    public static KeyPair generateKey(){
        Ed25519Sha3 crypto = new Ed25519Sha3();
        return crypto.generateKeypair();
    }

    public final static class Consts{
        public static final String UNIVERSITIES_DOMAIN = "universitySelection";
        public static final String APPLICANT_ROLE = "applicant";
        public static final String WILD_ASSET_NAME = "wild";
        public static final String WILD_SPECIALITY_ASSET_NAME = "wild_speciality";


    }

}
