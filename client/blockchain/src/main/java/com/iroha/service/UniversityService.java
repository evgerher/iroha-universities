package com.iroha.service;

import com.iroha.utils.ChainEntitiesUtils;

import java.security.KeyPair;
import java.util.Arrays;
import java.util.List;

import com.iroha.model.Applicant;
import com.iroha.model.university.University;

import com.iroha.utils.IrohaApiSingletone;
import io.reactivex.Observer;
import iroha.protocol.QryResponses;
import iroha.protocol.TransactionOuterClass;
import jp.co.soramitsu.iroha.java.IrohaAPI;
import jp.co.soramitsu.iroha.java.Query;
import jp.co.soramitsu.iroha.java.Transaction;
import lombok.val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.iroha.utils.ChainEntitiesUtils.*;
import static com.iroha.utils.ChainEntitiesUtils.Consts.UNIVERSITIES_DOMAIN;
import static com.iroha.utils.ChainEntitiesUtils.Consts.WILD_ASSET_NAME;


public class UniversityService {

    private static final Logger logger = LoggerFactory.getLogger(UniversityService.class);
    private KeyPair universityKeyPair;
    private University university;
    private IrohaAPI api;
    boolean success;


    public UniversityService(KeyPair keyPair,
                             University university) { //todo rewrite for current user instead of university
        this.universityKeyPair = keyPair;
        this.university = university;
        api = IrohaApiSingletone.getIrohaApiInstance();
    }

    public String createNewApplicantAccount(Applicant applicant, KeyPair keys, Observer observer) {
        val applicantAccountName = ChainEntitiesUtils.getApplicantAccountName(applicant);
        val transaction = Transaction.builder(ChainEntitiesUtils.getAccountId(getUniversityAccountName(university), getUniversityDomain(university)))
                .createAccount(ChainEntitiesUtils.getAccountId(applicantAccountName, Consts.UNIVERSITIES_DOMAIN), keys.getPublic())
                .sign(universityKeyPair)
                .build();
        return transaction.getPayload().getBatch().getReducedHashes(0);
    }

    public void getWildTokensTransaction(Applicant applicant, Observer observer) {
        val addTokens = Transaction
                .builder(getAccountId(getUniversityAccountName(university), getUniversityDomain(university)))
                .addAssetQuantity(getAssetId(WILD_ASSET_NAME, UNIVERSITIES_DOMAIN), "5")
                .sign(universityKeyPair)
                .build();
        api.transaction(addTokens).blockingSubscribe(observer);
        val transaction = createTransactionFromUniversity(applicant, WILD_ASSET_NAME, 5, UNIVERSITIES_DOMAIN);
        api.transaction(transaction).subscribe(observer);
    }

    public void chooseUniversity(Applicant applicant, KeyPair keyPair, Observer observer) {
        List<Transaction> transaction = Arrays.asList(
                createUnsignedAddAssetsToUniversity(getAssetId(Consts.WILD_SPECIALITY_ASSET_NAME,getUniversityDomain(university)),2),
                createUnsignedTransactionToUniversity(applicant,getAssetId(WILD_ASSET_NAME, UNIVERSITIES_DOMAIN),1),
                createUnsignedTransactionFromUniversity(applicant,getAssetId(Consts.WILD_SPECIALITY_ASSET_NAME,getUniversityDomain(university)),3)
        );
        TransactionOuterClass.Transaction atomicTransaction = Transaction.builder("")
                .setBatchMeta(transaction, TransactionOuterClass.Transaction.Payload.BatchMeta.BatchType.ATOMIC)
                .setQuorum(2)
                .sign(universityKeyPair)
                .sign(keyPair)
                .build();
        api.transaction(atomicTransaction).subscribe(observer);
    }

    //
//    public boolean chooseSpeciality(ResponseApplicant applicant, Speciality speciality){
//
//    }
//
//    public boolean swapUniversity(ResponseApplicant applicant, University destinationUniversity, Speciality speciality){
//
//    }
//
//    public boolean swapSpeciality(ResponseApplicant applicant, Speciality sourceSpeciality, Speciality destinationSpecialuty){
//
//    }
//
//
//    private Transaction buildAtomicTransaction(List<Transaction> transactions){
//         return Transaction.builder(getUniversityAccountName(university))
//                 .setB
    //   }
    private TransactionOuterClass.Transaction createTransactionToUniversity(Applicant applicant,
                                                                            String assetId, Integer assetsQuantity, KeyPair keyPair) {
        String applicant_account = ChainEntitiesUtils.getApplicantAccountName(applicant);
        String university_account = ChainEntitiesUtils.getUniversityAccountName(university);
        return Transaction.builder(applicant_account)
                .addAssetQuantity(assetId, assetsQuantity.toString())
                .transferAsset(applicant_account, university_account, assetId
                        , "", assetsQuantity.toString())
                .sign(keyPair)
                .build();
    }


    private TransactionOuterClass.Transaction createTransactionFromUniversity(Applicant applicant,
                                                                              String assetType, Integer assetsQuantity, String domain) {
        String applicant_account = ChainEntitiesUtils.getApplicantAccountName(applicant);
        String university_account = ChainEntitiesUtils.getUniversityAccountName(university);
        String universityAccountId = ChainEntitiesUtils
                .getAccountId(university_account, ChainEntitiesUtils.getUniversityDomain(university));
        String applicationAccountId = ChainEntitiesUtils
                .getAccountId(applicant_account, UNIVERSITIES_DOMAIN);
        return Transaction.builder(ChainEntitiesUtils.getAccountId(getUniversityAccountName(university), getUniversityDomain(university)))
                .transferAsset(universityAccountId, applicationAccountId,
                        ChainEntitiesUtils.getAssetId(assetType, domain)
                        , "", assetsQuantity.toString())
                .sign(universityKeyPair)
                .build();
    }

    private Transaction createUnsignedTransactionFromUniversity(Applicant applicant,
                                                                String assetId, Integer assetsQuantity) {
        String accountId = getAccountId(getApplicantAccountName(applicant),UNIVERSITIES_DOMAIN);
        String uniId= getAccountId(getUniversityAccountName(university), getUniversityDomain(university));
        return Transaction.builder(uniId)
                .transferAsset(uniId,accountId,assetId,"",assetsQuantity.toString())
                .build();
    }

    private Transaction createUnsignedTransactionToUniversity(Applicant applicant, String assetId, Integer assetsQuantity) {
        String accountId = getAccountId(getApplicantAccountName(applicant),UNIVERSITIES_DOMAIN);
        String uniId= getAccountId(getUniversityAccountName(university), getUniversityDomain(university));
        return Transaction.builder(accountId)
                .transferAsset(accountId,uniId,assetId,"",assetsQuantity.toString())
                .build();

    }

    private Transaction createUnsignedAddAssetsToUniversity(String assetId, Integer assetQunatity) {
        String accountId = getAccountId(getUniversityAccountName(university),getUniversityDomain(university));
        return Transaction.builder(accountId)
                .addAssetQuantity(assetId,assetQunatity.toString())
                .build();
    }

    public int getBalanceOfApplicant(Applicant applicant, String assertType) {
        val api = IrohaApiSingletone.getIrohaApiInstance();
        val query = Query.builder(
                ChainEntitiesUtils.getAccountId(ChainEntitiesUtils.getUniversityAccountName(university), ChainEntitiesUtils
                        .getUniversityDomain(university)), 1)
                .getAccountAssets(
                        ChainEntitiesUtils.getAccountId(ChainEntitiesUtils.getApplicantAccountName(applicant), UNIVERSITIES_DOMAIN))
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

    public List<QryResponses.AccountAsset> getAllAssertsOfApplicant(Applicant applicant) {
        val api = IrohaApiSingletone.getIrohaApiInstance();
        val query = Query.builder(
                ChainEntitiesUtils.getAccountId(ChainEntitiesUtils.getUniversityAccountName(university), ChainEntitiesUtils
                        .getUniversityDomain(university)), 1)
                .getAccountAssets(
                        ChainEntitiesUtils.getAccountId(ChainEntitiesUtils.getApplicantAccountName(applicant), UNIVERSITIES_DOMAIN))
                .buildSigned(universityKeyPair);
        val balance = api.query(query);
        return balance.getAccountAssetsResponse().getAccountAssetsList();

    }


}
