package com.iroha.service;

import com.iroha.model.university.Speciality;
import com.iroha.utils.ChainEntitiesUtils;

import java.security.KeyPair;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.iroha.model.Applicant;
import com.iroha.model.university.University;

import com.iroha.utils.IrohaApiSingletone;
import io.reactivex.Observer;
import iroha.protocol.TransactionOuterClass;

import jp.co.soramitsu.iroha.java.IrohaAPI;
import jp.co.soramitsu.iroha.java.Transaction;
import jp.co.soramitsu.iroha.java.Utils;
import jp.co.soramitsu.iroha.java.TransactionStatusObserver;
import jp.co.soramitsu.iroha.java.detail.InlineTransactionStatusObserver;
import jp.co.soramitsu.iroha.java.subscription.WaitForTerminalStatus;
import lombok.Data;
import lombok.val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.iroha.utils.ChainEntitiesUtils.*;
import static com.iroha.utils.ChainEntitiesUtils.ChainLogicConstants.UNIVERSITIES_DOMAIN;
import static com.iroha.utils.ChainEntitiesUtils.ChainLogicConstants.WILD_ASSET_NAME;
import static io.reactivex.Flowable.zip;

@Data
public class UniversityService {

    private static final Logger logger = LoggerFactory.getLogger(UniversityService.class);
    private KeyPair universityKeyPair;
    private University university;
    private IrohaAPI api;

    public UniversityService(KeyPair keyPair, University university) { // TODO rewrite for current user instead of university
        this.universityKeyPair = keyPair;
        this.university = university;
        api = IrohaApiSingletone.getIrohaApiInstance();
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
        val applicantAccountName = ChainEntitiesUtils.getApplicantAccountName(applicant);
        val domain = ChainEntitiesUtils.getUniversityDomain(university);
        val accountName = getUniversityAccountName(university);

        val transaction = Transaction.builder(ChainEntitiesUtils.getAccountId(accountName, domain))
                .createAccount(ChainEntitiesUtils.getAccountId(applicantAccountName, ChainLogicConstants.UNIVERSITIES_DOMAIN), keys.getPublic())
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
    private void createWildTokensUniversity(int amount, Observer<iroha.protocol.Endpoint.ToriiResponse> observer) {
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
    public void getWildTokensTransaction(Applicant applicant, Observer<iroha.protocol.Endpoint.ToriiResponse> observer) {
        val amount = 5;
        createWildTokensUniversity(amount, observer);
        val transaction = createTransactionFromUniversity(applicant, WILD_ASSET_NAME, amount, UNIVERSITIES_DOMAIN);
        api.transaction(transaction).subscribe(observer);
    }

    /**
     * Method chooses university for applicant
     * 1) Applicant send 1 wild token for university
     * 2) University sends 3 wild_speciality tokens for applicant
     * Method is atomic
     * @param applicant
     * @param applicantKeyPair
     * @param observer
     * @param university
     * @param universityKeyPair
     */
    public void chooseUniversity(Applicant applicant, KeyPair applicantKeyPair, Observer<iroha.protocol.Endpoint.ToriiResponse> observer, University university, KeyPair universityKeyPair) {
        val transactions = Arrays.asList(
                createUnsignedAddAssetsToUniversity(getAssetId(ChainLogicConstants.WILD_SPECIALITY_ASSET_NAME, getUniversityDomain(university)), 3, university).sign(universityKeyPair).build(),
                createUnsignedTransactionToUniversity(applicant, getAssetId(WILD_ASSET_NAME, UNIVERSITIES_DOMAIN), 1, university).sign(applicantKeyPair).build(),
                createUnsignedTransactionFromUniversity(applicant, getAssetId(ChainLogicConstants.WILD_SPECIALITY_ASSET_NAME, getUniversityDomain(university)), 3, university).sign(universityKeyPair).build()
        );
        val keys = new HashMap<TransactionOuterClass.Transaction, KeyPair>();
        keys.put(transactions.get(0), universityKeyPair);
        keys.put(transactions.get(1), applicantKeyPair);
        keys.put(transactions.get(2), universityKeyPair);
        api.transactionListSync(createBatch(transactions, TransactionOuterClass.Transaction.Payload.BatchMeta.BatchType.ATOMIC, keys));

        val waiter = new WaitForTerminalStatus();
        for (val tx : transactions) {
            val hash = Utils.hash(tx);
            waiter.subscribe(api, hash)
                    .subscribe(observer);
        }

    }

    /**
     * Method chooses speciality for applicant
     * 1) applicant send 1 wild_speciality token to university
     * 2) university send 1 particular speciality token to applicant
     * Method is atomic
     * @param applicant
     * @param speciality
     * @param observer
     * @param applicantKeyPair
     * @param university
     * @param universityKeyPair
     */
    public void chooseSpeciality(Applicant applicant, Speciality speciality, Observer<iroha.protocol.Endpoint.ToriiResponse> observer, KeyPair applicantKeyPair, University university, KeyPair universityKeyPair) {
        val assetName = ChainEntitiesUtils.getAssetName(speciality.getName(), getUniversityDomain(university));
        val transactions = Arrays.asList(
                createUnsignedTransactionToUniversity(applicant, getAssetId(ChainLogicConstants.WILD_SPECIALITY_ASSET_NAME, getUniversityDomain(university)), 1, university).sign(applicantKeyPair).build(),
                createUnsignedTransactionFromUniversity(applicant, getAssetId(assetName, getUniversityDomain(university)), 1, university).sign(universityKeyPair).build()
        );
        val keys = new HashMap<TransactionOuterClass.Transaction, KeyPair>();
        keys.put(transactions.get(0), applicantKeyPair);
        keys.put(transactions.get(1), universityKeyPair);

        api.transactionListSync((createBatch(transactions, TransactionOuterClass.Transaction.Payload.BatchMeta.BatchType.ATOMIC, keys)));
        val waiter = new WaitForTerminalStatus();
        for (val tx : transactions) {
            val hash = Utils.hash(tx);
            waiter.subscribe(api, hash)
                    .subscribe(observer);
        }
    }

    /**
     * Method exchanges speciality for applicant
     * 1) Applicant returns particular speciality token to old university
     * 2) New university send to applicant new particular speciality token
     * Method is atomic
     * TODO finish exchanging logic according laws
     * TODO 1) Restrict to choose sixth university
     * TODO 2) Get rest 2 wild_speciality token from new univeristy
     * TODO 3) Return the wild speciality token from old university, due to releasing speciality position
     * @param applicant
     * @param sourceUniversity
     * @param speciality
     * @param destinationUniversity
     * @param aplicantKey
     * @param destUniKey
     * @param observer
     */
    public void swapUniversity(Applicant applicant, University sourceUniversity,
                               Speciality speciality, University destinationUniversity, KeyPair aplicantKey,
                               KeyPair destUniKey, Observer<iroha.protocol.Endpoint.ToriiResponse> observer) {
        val assetNameSource = ChainEntitiesUtils.getAssetName(speciality.getName(), getUniversityDomain(sourceUniversity));
        val assetNameDest = ChainEntitiesUtils.getAssetName(speciality.getName(), getUniversityDomain(destinationUniversity));

        val transactions = Arrays.asList(
                createUnsignedTransactionToUniversity(applicant, getAssetId(assetNameSource, getUniversityDomain(sourceUniversity)), 1, sourceUniversity).sign(aplicantKey).build(),
                createUnsignedTransactionFromUniversity(applicant, getAssetId(assetNameDest, getUniversityDomain(destinationUniversity)), 1, destinationUniversity).sign(destUniKey).build()
        );
        val keys = new HashMap<TransactionOuterClass.Transaction, KeyPair>();
        keys.put(transactions.get(0), aplicantKey);
        keys.put(transactions.get(1), destUniKey);
        api.transactionListSync((createBatch(transactions, TransactionOuterClass.Transaction.Payload.BatchMeta.BatchType.ATOMIC, keys)));
        val waiter = new WaitForTerminalStatus();
        for (val tx : transactions) {
            val hash = Utils.hash(tx);
            waiter.subscribe(api, hash)
                    .subscribe(observer);
        }
    }


    private TransactionOuterClass.Transaction createTransactionFromUniversity(Applicant applicant,
                                                                              String assetType, Integer assetsQuantity, String domain) {
        val universityDomain = ChainEntitiesUtils.getUniversityDomain(university);
        val applicantAccount = ChainEntitiesUtils.getApplicantAccountName(applicant);
        val universityAccount = ChainEntitiesUtils.getUniversityAccountName(university);
        val universityAccountId = ChainEntitiesUtils.getAccountId(universityAccount, universityDomain);
        val applicationAccountId = ChainEntitiesUtils.getAccountId(applicantAccount, UNIVERSITIES_DOMAIN);
        val assetId = ChainEntitiesUtils.getAssetId(assetType, domain);

        return Transaction.builder(universityAccountId)
                .transferAsset(universityAccountId, applicationAccountId, assetId, "", assetsQuantity.toString())
                .sign(universityKeyPair)
                .build();
    }


    private Transaction createUnsignedTransactionFromUniversity(Applicant applicant,
                                                                String assetId, Integer assetsQuantity, University university) {
        val accountId = getAccountId(getApplicantAccountName(applicant), UNIVERSITIES_DOMAIN);
        val uniId = getAccountId(getUniversityAccountName(university), getUniversityDomain(university));
        return Transaction.builder(uniId, Instant.now())
                .transferAsset(uniId, accountId, assetId, "", assetsQuantity.toString())
                .build();
    }


    private Transaction createUnsignedTransactionToUniversity(Applicant applicant, String assetId, Integer assetsQuantity, University university) {
        val accountId = getAccountId(getApplicantAccountName(applicant), UNIVERSITIES_DOMAIN);
        val uniId = getAccountId(getUniversityAccountName(university), getUniversityDomain(university));
        return Transaction.builder(accountId, Instant.now())
                .transferAsset(accountId, uniId, assetId, "", assetsQuantity.toString())
                .build();
    }


    private Transaction createUnsignedAddAssetsToUniversity(String assetId, Integer assetQunatity, University university) {
        val accountId = getAccountId(getUniversityAccountName(university), getUniversityDomain(university));
        return Transaction.builder(accountId)
                .addAssetQuantity(assetId, assetQunatity.toString())
                .build();
    }

    private static Iterable<TransactionOuterClass.Transaction> createBatch(Iterable<TransactionOuterClass.Transaction> list,
                                                                           TransactionOuterClass.Transaction.Payload.BatchMeta.BatchType batchType,
                                                                           Map<TransactionOuterClass.Transaction, KeyPair> keys) {
        final val batchHashes = Utils.getProtoBatchHashesHex(list);

        return StreamSupport.stream(list.spliterator(), false)
                .map(tx -> Transaction
                        .parseFrom(tx)
                        .makeMutable()
                        .setBatchMeta(batchType, batchHashes)
                        .sign(keys.get(tx))
                        .build()
                )
                .collect(Collectors.toList());
    }


}
