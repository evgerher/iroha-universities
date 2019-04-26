package com.iroha10.service;

import java.security.KeyPair;

import com.iroha10.model.Applicant;
import com.iroha10.model.Speciality;
import com.iroha10.model.University;

import com.iroha10.utils.IrohaApiSingletone;
import jp.co.soramitsu.iroha.java.IrohaAPI;
import jp.co.soramitsu.iroha.java.Query;
import jp.co.soramitsu.iroha.java.Transaction;
import jp.co.soramitsu.iroha.java.TransactionStatusObserver;
import lombok.val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.iroha10.utils.ChainEntitiesUtils.Consts.WILD_ASSET_NAME;
import static com.iroha10.utils.ChainEntitiesUtils.generateKey;
import static com.iroha10.utils.ChainEntitiesUtils.getApplicantAccountName;
import static com.iroha10.utils.ChainEntitiesUtils.getUniversityAccountName;

public class UniversityService {
    private static final Logger logger = LoggerFactory.getLogger(UniversityService.class);
    private KeyPair universityKeyPair;
    private University university;

    UniversityService(KeyPair keyPair, University university){ //todo rewrite for current user instead of university
        this.universityKeyPair = keyPair;
        this.university = university;
    }

    public KeyPair createNewApplicantAccount(Applicant applicant){
        val keys = generateKey();
        Transaction.builder(getUniversityAccountName(university))
                .createAccount(getApplicantAccountName(applicant),keys.getPublic());
        return keys;
    }
    public boolean getWildTokensTransaction(Applicant applicant){
        val transaction = Transaction.builder(getUniversityAccountName(university))
            .addAssetQuantity(WILD_ASSET_NAME, "5")
        .transferAsset(getUniversityAccountName(university),getApplicantAccountName(applicant),WILD_ASSET_NAME
                ,"new tokens for university choosing","5")
                .sign(universityKeyPair)
                .build();
        val api = IrohaApiSingletone.getIrohaApiInstance();
        val observer = TransactionStatusObserver.builder()
                .onTransactionFailed(t ->
                {
                    logger.debug(String.format(
                        "transaction %s failed with msg: %s",
                        t.getTxHash(),
                        t.getErrOrCmdName()));
                })
                .onError(e -> logger.debug("Failed with exception: " + e))
                .onTransactionCommitted((t) -> System.out.println("Committed :)"))
                .onComplete(() -> logger.debug("Complete"))
                .build();

        api.transaction(transaction)
                .blockingSubscribe(observer);
        return getBalance(applicant)== 5;
    }

    public boolean chooseUniversity(Applicant applicant){

    }

    public boolean chooseSpeciality(Applicant applicant, Speciality speciality){

    }

    private int getBalance(Applicant applicant) {
        val api = IrohaApiSingletone.getIrohaApiInstance();
        val query = Query.builder(getUniversityAccountName(university), 1)
                .getAccountAssets(getApplicantAccountName(applicant))
                .buildSigned(universityKeyPair);
        val balance = api.query(query);
        val assets = balance.getAccountAssetsResponse().getAccountAssetsList();
        val assetWildOptional = assets
                .stream()
                .filter(a -> a.getAssetId().equals(WILD_ASSET_NAME))
                .findFirst();
        return assetWildOptional
                .map(a -> Integer.parseInt(a.getBalance()))
                .orElse(0);
    }
}
