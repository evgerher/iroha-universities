package com.iroha.utils;

import com.iroha.model.Applicant;
import com.iroha.model.university.University;
import jp.co.soramitsu.crypto.ed25519.Ed25519Sha3;

import java.security.KeyPair;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChainEntitiesUtils {
    public static String getAssetName(String specialityName,String universityName){
        return String.format("%s%s",specialityName, universityName);
    }
    public static String getAssetId(String specialityName,String universityName){
        return String.format("%s#%s",specialityName, universityName);
    }
    public static Map<String,KeyPair> generateKeys(List<String> names){
        Map<String,KeyPair> mapping = new HashMap<>();

        Ed25519Sha3 crypto = new Ed25519Sha3();

        for(String name: names){
          mapping.put(name, crypto.generateKeypair());
        }
        return mapping;
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
    public static String getApplicantAccountName(Applicant applicant){
        return applicant.getPubkey();
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
