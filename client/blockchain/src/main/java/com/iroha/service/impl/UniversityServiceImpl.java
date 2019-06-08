package com.iroha.service.impl;

import com.iroha.model.university.Speciality;
import com.iroha.utils.ChainEntitiesUtils;
import iroha.protocol.Queries;

import java.security.KeyPair;
import java.time.Instant;
import java.util.*;


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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.iroha.utils.ChainEntitiesUtils.*;
import static com.iroha.utils.ChainEntitiesUtils.Consts.UNIVERSITIES_DOMAIN;
import static com.iroha.utils.ChainEntitiesUtils.Consts.WILD_ASSET_NAME;
import static com.iroha.utils.ChainEntitiesUtils.Consts.WILD_SPECIALITY_ASSET_NAME;
import static com.iroha.utils.IrohaUtils.createBatch;

//TODO add swap speciality feature
public class UniversityServiceImpl {

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

        InlineTransactionStatusObserver obs = TransactionStatusObserver.builder()
                .onComplete(() -> logger.info("Account with userCode={} received {} wild tokens", applicant.getUserCode()))
                .build();

        getWildTokensTransaction(applicant, obs);
        return applicant.getUserCode();
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

    /**
     * Method sends 5 wild tokens for an applicant
     * 1) Generate 5 tokens from air
     * 2) Send 5 tokens to applicant
     *
     * @param applicant
     * @param observer
     */
    public void getWildTokensTransaction(Applicant applicant, Observer observer) {
        Integer amount = 5;
        generateWildTokensUniversity(amount, observer);
        val transaction = createTransactionFromUniversity(applicant, WILD_ASSET_NAME, amount, UNIVERSITIES_DOMAIN);
        api.transaction(transaction).subscribe(observer);
    }

    public void chooseUniversity(Applicant applicant, KeyPair applicantKeyPair, Observer observer, University university, KeyPair universityKeyPair) {
        List<TransactionOuterClass.Transaction> transactions = Arrays.asList(
                createUnsignedAddAssetsToUniversity(getAssetId(WILD_SPECIALITY_ASSET_NAME, getUniversityDomain(university)), 3, university).sign(universityKeyPair).build(),
                createUnsignedTransactionToUniversity(applicant, getAssetId(WILD_ASSET_NAME, UNIVERSITIES_DOMAIN), 1, university).sign(applicantKeyPair).build(),
                createUnsignedTransactionFromUniversity(applicant, getAssetId(WILD_SPECIALITY_ASSET_NAME, getUniversityDomain(university)), 3, university).sign(universityKeyPair).build()
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
        String assetName = ChainEntitiesUtils.getAssetName(speciality.getName(), getUniversityDomain(university));
        List<TransactionOuterClass.Transaction> transactions = Arrays.asList(
                createUnsignedTransactionToUniversity(applicant, getAssetId(WILD_SPECIALITY_ASSET_NAME, getUniversityDomain(university)), 1, university).sign(applicantKeyPair).build(),
                createUnsignedTransactionFromUniversity(applicant, getAssetId(assetName, getUniversityDomain(university)), 1, university).sign(universityKeyPair).build()
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


    public void swapUniversity(Applicant applicant, University sourceUniversity,
                               Speciality speciality, University destinationUniversity, KeyPair aplicantKey,
                               KeyPair destUniKey, Observer observer) {
        String assetNameSource = getAssetName(speciality.getName(), getUniversityDomain(sourceUniversity));
        String assetNameDest = getAssetName(speciality.getName(), getUniversityDomain(destinationUniversity));

        List<TransactionOuterClass.Transaction> transactions = Arrays.asList(
                createUnsignedTransactionToUniversity(applicant, getAssetId(assetNameSource, getUniversityDomain(sourceUniversity)), 1, sourceUniversity).sign(aplicantKey).build(),
                createUnsignedTransactionFromUniversity(applicant, getAssetId(assetNameDest, getUniversityDomain(destinationUniversity)), 1, destinationUniversity).sign(destUniKey).build()
        );
        Map<TransactionOuterClass.Transaction, KeyPair> keys = new HashMap<>();
        keys.put(transactions.get(0), aplicantKey);
        keys.put(transactions.get(1), destUniKey);
        api.transactionListSync((createBatch(transactions, TransactionOuterClass.Transaction.Payload.BatchMeta.BatchType.ATOMIC, keys)));
        val waiter = new WaitForTerminalStatus();
        for (TransactionOuterClass.Transaction tx : transactions) {
            val hash = Utils.hash(tx);
            waiter.subscribe(api, hash)
                    .subscribe(observer);
        }
    }
    public List<QryResponses.AccountAsset> getAllAssertsOfApplicant(Applicant applicant) {
        val api = IrohaApiSingletone.getIrohaApiInstance();
        String universityId =getAccountId(getUniversityAccountName(university),
                getUniversityDomain(university));
        String applicantId = getAccountId(getApplicantAccountName(applicant), UNIVERSITIES_DOMAIN);

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
        String applicant_account = getApplicantAccountName(applicant);
        String university_account = getUniversityAccountName(university);
        return Transaction.builder(applicant_account)
                .addAssetQuantity(assetId, assetsQuantity.toString())
                .transferAsset(applicant_account, university_account, assetId
                        , "", assetsQuantity.toString())
                .sign(keyPair)
                .build();
    }

    private TransactionOuterClass.Transaction createTransactionFromUniversity(Applicant applicant,
                                                                              String assetType, Integer assetsQuantity, String domain) {
        String universityDomain = getUniversityDomain(university);
        String applicantAccount = getApplicantAccountName(applicant);
        String universityAccount = getUniversityAccountName(university);
        String universityAccountId = getAccountId(universityAccount, universityDomain);
        String applicationAccountId = getAccountId(applicantAccount, UNIVERSITIES_DOMAIN);
        String assetId = getAssetId(assetType, domain);

        return Transaction.builder(universityAccountId)
                .transferAsset(universityAccountId, applicationAccountId, assetId, "", assetsQuantity.toString())
                .sign(universityKeyPair)
                .build();
    }



    private Transaction createUnsignedTransactionFromUniversity(Applicant applicant,
                                                                String assetId, Integer assetsQuantity, University university) {
        String accountId = getAccountId(getApplicantAccountName(applicant), UNIVERSITIES_DOMAIN);
        String uniId = getAccountId(getUniversityAccountName(university), getUniversityDomain(university));
        return Transaction.builder(uniId, Instant.now())
                .transferAsset(uniId, accountId, assetId, "", assetsQuantity.toString())
                .build();
    }



    private Transaction createUnsignedTransactionToUniversity(Applicant applicant, String assetId, Integer assetsQuantity, University university) {
        String accountId = getAccountId(getApplicantAccountName(applicant), UNIVERSITIES_DOMAIN);
        String uniId = getAccountId(getUniversityAccountName(university), getUniversityDomain(university));
        return Transaction.builder(accountId, Instant.now())
                .transferAsset(accountId, uniId, assetId, "", assetsQuantity.toString())
                .build();
    }

    private Queries.Query constructQueryAccount(String accountId) {
        String universityAccount = getAccountId(getUniversityAccountName(university), getUniversityDomain(university));
        return Query.builder(universityAccount, 1).getAccount(accountId).buildSigned(universityKeyPair);
    }

    private Transaction createUnsignedAddAssetsToUniversity(String assetId, Integer assetQunatity, University university) {
        String accountId = getAccountId(getUniversityAccountName(university), getUniversityDomain(university));
        return Transaction.builder(accountId)
                .addAssetQuantity(assetId, assetQunatity.toString())
                .build();
    }
}
