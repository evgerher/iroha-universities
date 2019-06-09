package com.iroha.service.impl;

import com.iroha.model.university.Speciality;
import com.iroha.utils.ChainEntitiesUtils;
import iroha.protocol.Queries;

import java.security.KeyPair;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;


import com.iroha.model.Applicant;
import com.iroha.model.university.University;

import com.iroha.utils.IrohaApiSingletone;
import io.reactivex.Observer;
import iroha.protocol.QryResponses;
import iroha.protocol.TransactionOuterClass;

import jp.co.soramitsu.iroha.java.IrohaAPI;
import jp.co.soramitsu.iroha.java.Query;
import jp.co.soramitsu.iroha.java.Transaction;
import jp.co.soramitsu.iroha.java.Utils;
import jp.co.soramitsu.iroha.java.TransactionStatusObserver;
import jp.co.soramitsu.iroha.java.detail.InlineTransactionStatusObserver;
import jp.co.soramitsu.iroha.java.subscription.WaitForTerminalStatus;
import lombok.val;
import lombok.var;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.iroha.utils.ChainEntitiesUtils.*;
import static com.iroha.utils.ChainEntitiesUtils.Consts.UNIVERSITIES_DOMAIN;
import static com.iroha.utils.ChainEntitiesUtils.Consts.WILD_ASSET_NAME;
import static com.iroha.utils.ChainEntitiesUtils.Consts.WILD_SPECIALITY_ASSET_NAME;
import static com.iroha.utils.IrohaUtils.createBatch;

public class UniversityServiceImpl {

    private final int NUM_UNIVERSITY_OPTIONS = 5;
    private static final Logger logger = LoggerFactory.getLogger(UniversityServiceImpl.class);
    private KeyPair universityKeyPair;
    private University university;
    private IrohaAPI api;

    public UniversityServiceImpl(KeyPair keyPair,
                                 University university) { //todo rewrite for current user instead of university
        this.universityKeyPair = keyPair;
        this.university = university;
        api = IrohaApiSingletone.getIrohaApiInstance();
    }


    public University getUniversity() {
        return university;
    }

    /**
     * Method creates new account and sends him wild tokens
     *
     * @param applicant
     * @param keys
     * @param observer
     * @return trasaction .createAccount hash (probably)
     */
    public String createNewApplicantAccount(Applicant applicant, KeyPair keys, InlineTransactionStatusObserver observer) {
        val applicantAccountName = getApplicantAccountName(applicant);
        val domain = getUniversityDomain(university);
        val accountName = getUniversityAccountName(university);

        val transaction = Transaction.builder(getAccountId(accountName, domain))
                .createAccount(getAccountId(applicantAccountName, UNIVERSITIES_DOMAIN), keys.getPublic())
                .sign(universityKeyPair)
                .build();
        api.transaction(transaction).subscribe(observer);

        val obs = TransactionStatusObserver.builder()
                .onComplete(() -> logger.info("Account with userCode={} received {} wild tokens", applicant.getUserCode()))
                .build();

        getWildTokensTransaction(applicant, obs);
        return applicant.getUserCode();
    }



    /**
     * Method sends 5 wild tokens for an applicant
     * 1) Generate 5 tokens from air
     * 2) Send 5 tokens to applicant
     *
     * @param applicant
     * @param observer
     */
    public void getWildTokensTransaction(Applicant applicant, Observer observer) {
        val amount = NUM_UNIVERSITY_OPTIONS;
        generateWildTokensUniversity(amount, observer);
        val transaction = createTransactionFromUniversity(applicant, WILD_ASSET_NAME, amount, UNIVERSITIES_DOMAIN);
        api.transaction(transaction).subscribe(observer);
    }

    public void chooseUniversity(Applicant applicant, KeyPair applicantKeyPair, Observer observer, University university, KeyPair universityKeyPair) {
        List<TransactionOuterClass.Transaction> transactions = Arrays.asList(
                createUnsignedAddAssetsToUniversity(getAssetId(WILD_SPECIALITY_ASSET_NAME, getUniversityDomain(university)), 3, university)
                        .sign(universityKeyPair)
                        .build(),
                createUnsignedTransactionToUniversity(applicant, university, WILD_ASSET_NAME, 1)
                        .sign(applicantKeyPair)
                        .build(),
                createUnsignedTransactionFromUniversity(applicant, university, WILD_SPECIALITY_ASSET_NAME,3)
                        .sign(universityKeyPair)
                        .build()
        );
        Map<TransactionOuterClass.Transaction, KeyPair> keys = new HashMap<>();
        keys.put(transactions.get(0), universityKeyPair);
        keys.put(transactions.get(1), applicantKeyPair);
        keys.put(transactions.get(2), universityKeyPair);
        api.transactionListSync(createBatch(transactions, TransactionOuterClass.Transaction.Payload.BatchMeta.BatchType.ATOMIC, keys));

        val waiter = new WaitForTerminalStatus();
        for (TransactionOuterClass.Transaction tx : transactions) {
            val hash = Utils.hash(tx);
            waiter.subscribe(api, hash)
                    .subscribe(observer);
        }

    }

    public void chooseSpeciality(Applicant applicant, Speciality speciality, Observer observer, KeyPair applicantKeyPair, University university, KeyPair universityKeyPair) {
        val assetName = getAssetName(speciality.getName(), getUniversityDomain(university));
        List<TransactionOuterClass.Transaction> transactions = Arrays.asList(
                createUnsignedTransactionToUniversity(applicant, university, WILD_SPECIALITY_ASSET_NAME, 1)
                        .sign(applicantKeyPair)
                        .build(),
                createUnsignedTransactionFromUniversity(applicant, university, assetName, 1)
                        .sign(universityKeyPair)
                        .build()
        );
        Map<TransactionOuterClass.Transaction, KeyPair> keys = new HashMap<>();
        keys.put(transactions.get(0), applicantKeyPair);
        keys.put(transactions.get(1), universityKeyPair);

        api.transactionListSync((createBatch(transactions, TransactionOuterClass.Transaction.Payload.BatchMeta.BatchType.ATOMIC, keys)));
        val waiter = new WaitForTerminalStatus();
        for (TransactionOuterClass.Transaction tx : transactions) {
            val hash = Utils.hash(tx);
            waiter.subscribe(api, hash)
                    .subscribe(observer);
        }
    }

    public void returnAllUniversityTokens(Applicant applicant, University university){

    }

    public void changeSpeciality(Applicant applicant, University sourceUniversity,
                                University destinationUniversity,Speciality currentSpeciality,Speciality newSpeciality, KeyPair aplicantKey,
                               KeyPair destUniKey, Observer observer) {
        if (!canApplicantChooseUniversity(applicant,destinationUniversity)){
            throw new IllegalArgumentException("Can't get speciality for destination university");
        }

        val assetNameSource = getAssetName(currentSpeciality.getName(), getUniversityDomain(sourceUniversity));
        val assetNameDest = getAssetName(newSpeciality.getName(), getUniversityDomain(destinationUniversity));

        List<TransactionOuterClass.Transaction> transactions = Arrays.asList(
                createUnsignedTransactionToUniversity(applicant,destinationUniversity , assetNameSource, 1 )
                        .sign(aplicantKey)
                        .build(),
                createUnsignedTransactionFromUniversity(applicant, sourceUniversity, assetNameDest, 1)
                        .sign(destUniKey)
                        .build()
        );
        Map<TransactionOuterClass.Transaction, KeyPair> keys = new HashMap<>();
        keys.put(transactions.get(0), aplicantKey);
        keys.put(transactions.get(1), destUniKey);
        api.transactionListSync((createBatch(transactions, TransactionOuterClass.Transaction.Payload.BatchMeta.BatchType.ATOMIC, keys)));
        val waiter = new WaitForTerminalStatus();
        for (TransactionOuterClass.Transaction tx : transactions) {
            val hash = Utils.hash(tx);
            waiter.subscribe(api, hash).subscribe(observer);
        }
    }

    public boolean canApplicantChooseUniversity(Applicant applicant, University university){
        List<QryResponses.AccountAsset> assets = getAllAssertsOfApplicant(applicant);
        Set<String> universities = assets.stream()
                                        .map(asset -> {
                                            var assetId =  asset.getAssetId();
                                            int splitPosition = assetId.indexOf("@");
                                            return assetId.substring(splitPosition+1);
                                        })
                                        .distinct()
                                        .collect(Collectors.toSet());

        return !universities.contains(university.getName()) && universities.size() > NUM_UNIVERSITY_OPTIONS;
    }
    public List<QryResponses.AccountAsset> getAllAssertsOfApplicant(Applicant applicant) {
        val api = IrohaApiSingletone.getIrohaApiInstance();
        val universityId =getAccountId(getUniversityAccountName(university), getUniversityDomain(university));
        val applicantId = getAccountId(getApplicantAccountName(applicant), UNIVERSITIES_DOMAIN);

        val query = Query.builder(universityId, 1)
                .getAccountAssets(applicantId)
                .buildSigned(universityKeyPair);
        val balance = api.query(query);
        return balance.getAccountAssetsResponse().getAccountAssetsList();
    }



    public int getBalanceOfApplicant(Applicant applicant, String assertType) {
        val assets = getAllAssertsOfApplicant(applicant);
        val assetWildOptional = assets
                .stream()
                .filter(a -> a.getAssetId().equals(assertType))
                .findFirst();
        return assetWildOptional
                .map(a -> Integer.parseInt(a.getBalance()))
                .orElse(0);
    }

    private TransactionOuterClass.Transaction createTransactionToUniversity(Applicant applicant,
                                                                            String assetId, Integer assetsQuantity, KeyPair keyPair) {
        val applicant_account = getApplicantAccountName(applicant);
        val university_account = getUniversityAccountName(university);
        return Transaction.builder(applicant_account)
                .addAssetQuantity(assetId, assetsQuantity.toString())
                .transferAsset(applicant_account, university_account, assetId
                        , "", assetsQuantity.toString())
                .sign(keyPair)
                .build();
    }

    private TransactionOuterClass.Transaction createTransactionFromUniversity(Applicant applicant,
                                                                              String assetType, Integer assetsQuantity, String domain) {
        val universityDomain = getUniversityDomain(university);
        val applicantAccount = getApplicantAccountName(applicant);
        val universityAccount = getUniversityAccountName(university);
        val universityAccountId = getAccountId(universityAccount, universityDomain);
        val applicationAccountId = getAccountId(applicantAccount, UNIVERSITIES_DOMAIN);
        val assetId = getAssetId(assetType, domain);

        return Transaction.builder(universityAccountId)
                .transferAsset(universityAccountId, applicationAccountId, assetId, "", assetsQuantity.toString())
                .sign(universityKeyPair)
                .build();
    }



    private Transaction createUnsignedTransactionFromUniversity(Applicant to,University from,
                                                                String assetName, Integer assetsQuantity) {
        val accountId = getAccountId(getApplicantAccountName(to), UNIVERSITIES_DOMAIN);
        val uniId = getAccountId(getUniversityAccountName(university), getUniversityDomain(university));
        val assetId = getAssetId(assetName, getUniversityDomain(from));

        return Transaction.builder(uniId, Instant.now())
                .transferAsset(uniId, accountId, assetId, "", assetsQuantity.toString())
                .build();
    }



    private Transaction createUnsignedTransactionToUniversity(Applicant from, University to, String assetName, Integer assetsQuantity) {
        val accountId = getAccountId(getApplicantAccountName(from), UNIVERSITIES_DOMAIN);
        val uniId = getAccountId(getUniversityAccountName(to), getUniversityDomain(university));
        val assetId = getAssetId(assetName, getUniversityDomain(to));
        return Transaction.builder(accountId, Instant.now())
                .transferAsset(accountId, uniId, assetId, "", assetsQuantity.toString())
                .build();
    }

    private Queries.Query constructQueryAccount(String accountId) {
        val universityAccount = getAccountId(getUniversityAccountName(university), getUniversityDomain(university));
        return Query.builder(universityAccount, 1).getAccount(accountId).buildSigned(universityKeyPair);
    }

    private Transaction createUnsignedAddAssetsToUniversity(String assetId, Integer assetQunatity, University university) {
        val accountId = getAccountId(getUniversityAccountName(university), getUniversityDomain(university));
        return Transaction.builder(accountId)
                .addAssetQuantity(assetId, assetQunatity.toString())
                .build();
    }

    /**
     * Method generates 5 tokens for the requested `university`
     *
     * @param amount
     * @param observer
     */
    private void generateWildTokensUniversity(int amount, Observer observer) {
        val addTokens = Transaction
                .builder(getAccountId(getUniversityAccountName(university), getUniversityDomain(university)))
                .addAssetQuantity(getAssetId(WILD_ASSET_NAME, UNIVERSITIES_DOMAIN), Integer.toString(amount))
                .sign(universityKeyPair)
                .build();
        api.transaction(addTokens).blockingSubscribe(observer);
    }
}
