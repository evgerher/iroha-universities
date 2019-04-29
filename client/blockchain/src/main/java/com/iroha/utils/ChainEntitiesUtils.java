package com.iroha.utils;

import com.iroha.model.Applicant;
import com.iroha.model.university.University;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import jp.co.soramitsu.crypto.ed25519.Ed25519Sha3;

import java.security.KeyPair;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChainEntitiesUtils {
    private static final Logger logger = LoggerFactory.getLogger(ChainEntitiesUtils.class);

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
    
    public static byte[] hexToBytes(String encodedKeyPair) {
        return encodedKeyPair.getBytes();
    }

    public final static class Consts{
        public static final String UNIVERSITIES_DOMAIN = "universitySelection";
        public static final String APPLICANT_ROLE = "applicant";
        public static final String WILD_ASSET_NAME = "wild";
        public static final String WILD_SPECIALITY_ASSET_NAME = "wild_speciality";


    }

    public static String bytesToHex(byte[] hashInBytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : hashInBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public static String encodeKeyPair(KeyPair keys) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            try (ObjectOutputStream ous = new ObjectOutputStream(baos)) {
                ous.writeObject(keys);
                byte[] bytes = baos.toByteArray();
                return ChainEntitiesUtils.bytesToHex(bytes);
            }
        } catch (IOException e) {
            logger.error("Exception during key storing, {}", e);
            throw e;
        }
    }

    public static KeyPair decodeKeyPair(byte[] bytes) {
        try (ByteArrayInputStream bi = new ByteArrayInputStream(bytes)) {
            try (ObjectInputStream oi = new ObjectInputStream(bi)) {
                Object obj = oi.readObject();
                return (KeyPair) obj;
            }
        } catch (Exception e) {
            logger.error("Unable to parse class from byte object");
            return null;
        }
    }
}
