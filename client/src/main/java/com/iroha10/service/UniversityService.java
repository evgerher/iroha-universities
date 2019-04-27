package com.iroha10.service;

import java.security.Key;
import java.security.KeyPair;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;

import com.iroha10.model.Applicant;
import com.iroha10.model.Speciality;
import com.iroha10.model.University;

import com.iroha10.utils.IrohaApiSingletone;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import iroha.protocol.TransactionOuterClass;
import jp.co.soramitsu.iroha.java.IrohaAPI;
import jp.co.soramitsu.iroha.java.Query;
import jp.co.soramitsu.iroha.java.Transaction;
import jp.co.soramitsu.iroha.java.TransactionStatusObserver;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.val;
import org.jetbrains.annotations.Contract;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.OperationsException;

import static com.iroha10.utils.ChainEntitiesUtils.*;
import static com.iroha10.utils.ChainEntitiesUtils.Consts.WILD_ASSET_NAME;
import static com.iroha10.utils.ChainEntitiesUtils.Consts.WILD_SPECIALITY_ASSET_NAME;


public class UniversityService {
    private static final Logger logger = LoggerFactory.getLogger(UniversityService.class);
    private KeyPair universityKeyPair;
    private University university;
    private IrohaAPI api;
    boolean success;


    public UniversityService(KeyPair keyPair, University university){ //todo rewrite for current user instead of university
        this.universityKeyPair = keyPair;
        this.university = university;
        api = IrohaApiSingletone.getIrohaApiInstance();
    }

    public KeyPair createNewApplicantAccount(Applicant applicant){
        val keys = generateKey();
        val applicantAccountName = getApplicantAccountName(applicant);
        val domain = getUniversityDomain(university);
        val transaction = Transaction.builder(getUniversityAccountName(university))
                .createAccount(getAccountId(applicantAccountName,domain),keys.getPublic())
                .sign(universityKeyPair)
                .build();
        api.transaction(transaction).blockingSubscribe();
        return keys;
    }
    public Observable getWildTokensTransaction(Applicant applicant){
        val transaction = createTransactionFromUniversity(applicant,WILD_ASSET_NAME,5);
        return api.transaction(transaction).distinct();
    }

//    public Observable chooseUniversity(Applicant applicant, KeyPair keyPair)  {
//        val removeWildTokenTransaction = createTransactionToUniversity(applicant,WILD_ASSET_NAME, 1, keyPair);
//        val getSpecialityTokensTransaction = createTransactionFromUniversity(applicant,WILD_SPECIALITY_ASSET_NAME,3);
//        val transactions = Arrays.asList(removeWildTokenTransaction,getSpecialityTokensTransaction);
//        val chooseUniversityTransaction = buildAtomicTransaction(transactions);
//        removeWildTokenTransaction.toBuilder().
//        return api.transaction(getSpecialityTokensTransaction).distinct();
//    }
//
//    public boolean chooseSpeciality(Applicant applicant, Speciality speciality){
//
//    }
//
//    public boolean swapUniversity(Applicant applicant, University destinationUniversity, Speciality speciality){
//
//    }
//
//    public boolean swapSpeciality(Applicant applicant, Speciality sourceSpeciality, Speciality destinationSpecialuty){
//
//    }
//
//
//    private Transaction buildAtomicTransaction(List<Transaction> transactions){
//         return Transaction.builder(getUniversityAccountName(university))
//                 .setB
 //   }
    private TransactionOuterClass.Transaction createTransactionToUniversity(Applicant applicant, String assetType, Integer assetsQuantity, KeyPair keyPair){
        String applicant_account = getApplicantAccountName(applicant);
        String university_account =  getUniversityAccountName(university);
        return Transaction.builder(applicant_account)
                .addAssetQuantity(assetType, assetsQuantity.toString())
                .transferAsset(applicant_account,university_account,assetType
                        ,"",assetsQuantity.toString())
                .sign(keyPair)
                .build();
    }

    private TransactionOuterClass.Transaction createTransactionFromUniversity(Applicant applicant, String assetType, Integer assetsQuantity ){
        String applicant_account = getApplicantAccountName(applicant);
        String university_account =  getUniversityAccountName(university);
        String universityAccountId = getAccountId(university_account,getUniversityDomain(university));
        String applicationAccountId = getAccountId(applicant_account,getUniversityDomain(university));
        return Transaction.builder(university_account)
                .addAssetQuantity(getAssetId(assetType,university.getName()), assetsQuantity.toString())
                .transferAsset(universityAccountId,applicationAccountId,getAssetId(assetType, university.getName())
                        ,"",assetsQuantity.toString())
                .sign(universityKeyPair)
                .build();
    }



    public int getBalanceOfApplicant(Applicant applicant, String assertType) {
        val api = IrohaApiSingletone.getIrohaApiInstance();
        val query = Query.builder(getAccountId(getUniversityAccountName(university),getUniversityDomain(university)),1)
                .getAccountAssets(getAccountId(getApplicantAccountName(applicant),getUniversityDomain(university)))
                .buildSigned(universityKeyPair);
        val balance = api.query(query);
        val assets = balance.getAccountAssetsResponse().getAccountAssetsList();
        val assetWildOptional = assets
                .stream()
                .filter(a -> a.getAssetId().equals(assertType))
                .findFirst();
        return assetWildOptional
                .map(a -> Integer.parseInt(a.getBalance()))
                .orElse(0);
    }


}
