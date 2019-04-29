package com.iroha.utils;

import com.iroha.model.Applicant;
import com.iroha.model.university.University;
import javax.xml.bind.DatatypeConverter;
import jp.co.soramitsu.crypto.ed25519.Ed25519Sha3;

import java.security.KeyPair;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChainEntitiesUtils {
    private static final Logger logger = LoggerFactory.getLogger(ChainEntitiesUtils.class);

    public final static class Consts {
        public static final String UNIVERSITIES_DOMAIN = "universitySelection";
        public static final String APPLICANT_ROLE = "applicant";
        public static final String WILD_ASSET_NAME = "wild";
        public static final String WILD_SPECIALITY_ASSET_NAME = "wild_speciality";
    }

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
        return university.getName()+"account";
    }

    public static String getAccountId(String accountName, String domain){
        return String.format("%s@%s",accountName,domain);
    }
    public static String getApplicantAccountName(Applicant applicant){
        return applicant.getId();
    }

    public static KeyPair generateKey(){
        Ed25519Sha3 crypto = new Ed25519Sha3();
        return crypto.generateKeypair();
    }

    public static String bytesToHex(byte[] hashInBytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : hashInBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
    
    public static byte[] hexToBytes(String encodedBytes) {
        return DatatypeConverter.parseHexBinary(encodedBytes);
    }

    public static KeyPair getKeys(Applicant applicant) {
        byte[] bytesPkey = ChainEntitiesUtils.hexToBytes(applicant.getPkey());
        byte[] bytesPubKey = ChainEntitiesUtils.hexToBytes(applicant.getPubkey());
        return Ed25519Sha3.keyPairFromBytes(bytesPkey, bytesPubKey);
    }
}
