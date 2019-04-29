package com.iroha.service;

import com.iroha.utils.ChainEntitiesUtils;
import java.security.KeyPair;
import java.util.Arrays;
import java.util.List;

import com.iroha.model.Applicant;
import com.iroha.model.university.University;

import com.iroha.utils.IrohaApiSingletone;
import io.reactivex.Observable;
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

  public UniversityService(KeyPair keyPair,
      University university) { //todo rewrite for current user instead of university
    this.universityKeyPair = keyPair;
    this.university = university;
    api = IrohaApiSingletone.getIrohaApiInstance();
  }

  /**
   *
   * @param applicant
   * @param keys
   * @param observer
   * @return trasaction .createAccount hash (probably)
   */
  public String createNewApplicantAccount(Applicant applicant, KeyPair keys, Observer observer) {
    val applicantAccountName = ChainEntitiesUtils.getApplicantAccountName(applicant);
    val domain = ChainEntitiesUtils.getUniversityDomain(university);
    val accountName = getUniversityAccountName(university);

    val transaction = Transaction.builder(ChainEntitiesUtils.getAccountId(accountName, domain))
        .createAccount(ChainEntitiesUtils.getAccountId(applicantAccountName, Consts.UNIVERSITIES_DOMAIN), keys.getPublic())
        .sign(universityKeyPair)
        .build();
    api.transaction(transaction).subscribe(observer);
    return transaction.getPayload().getBatch().getReducedHashes(0);
  }

  /**
   * Method generates 5 tokens for the requested `university`
   * @param amount
   * @param observer
   */
  private void generateWildTokensUniversity(int amount, Observer observer) {
    val addTokens = Transaction
        .builder(getAccountId(getUniversityAccountName(university),getUniversityDomain(university)))
        .addAssetQuantity(getAssetId(WILD_ASSET_NAME,UNIVERSITIES_DOMAIN),Integer.toString(amount))
        .sign(universityKeyPair)
        .build();
    api.transaction(addTokens).blockingSubscribe(observer);
  }

  /**
   * Method sends 5 wild tokens for an applicant
   * 1) Generate 5 tokens from air
   * 2) Send 5 tokens to applicant
   * @param applicant
   * @param observer
   */
  public void getWildTokensTransaction(Applicant applicant, Observer observer) {
    int amount = 5;
    generateWildTokensUniversity(amount, observer);
    val transaction = createTransactionFromUniversity(applicant, WILD_ASSET_NAME, amount, UNIVERSITIES_DOMAIN);
    api.transaction(transaction).subscribe(observer);
  }

//      public Observable chooseUniversity(Applicant applicant, KeyPair keyPair)  {
//        val removeWildTokenTransaction = createTransactionToUniversity(applicant,WILD_ASSET_NAME, 1, keyPair);
//        Transaction.builder().addAssetQuantity().setBatchMeta().sign().build();
//        val getSpecialityTokensTransaction = createTransactionFromUniversity(applicant,WILD_SPECIALITY_ASSET_NAME,3);
//        val transactions = Arrays.asList(removeWildTokenTransaction,getSpecialityTokensTransaction);
//        val chooseUniversityTransaction = buildAtomicTransaction(transactions);
//        removeWildTokenTransaction.toBuilder().
//        return api.transaction(getSpecialityTokensTransaction).distinct();
//    }
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
      String assetType, Integer assetsQuantity, KeyPair keyPair) {
    String applicant_account = ChainEntitiesUtils.getApplicantAccountName(applicant);
    String university_account = ChainEntitiesUtils.getUniversityAccountName(university);
    return Transaction.builder(applicant_account)
        .addAssetQuantity(assetType, assetsQuantity.toString())
        .transferAsset(applicant_account, university_account, assetType
            , "", assetsQuantity.toString())
        .sign(keyPair)
        .build();
  }

  private TransactionOuterClass.Transaction createTransactionFromUniversity(Applicant applicant,
      String assetType, Integer assetsQuantity, String domain) {
    String universityDomain = ChainEntitiesUtils.getUniversityDomain(university);
    String applicantAccount = ChainEntitiesUtils.getApplicantAccountName(applicant);
    String universityAccount = ChainEntitiesUtils.getUniversityAccountName(university);
    String universityAccountId = ChainEntitiesUtils.getAccountId(universityAccount, universityDomain);
    String applicationAccountId = ChainEntitiesUtils.getAccountId(applicantAccount, UNIVERSITIES_DOMAIN);
    String assetId = ChainEntitiesUtils.getAssetId(assetType, domain);

    return Transaction.builder(universityAccountId)
        .transferAsset(universityAccountId, applicationAccountId, assetId, "", assetsQuantity.toString())
        .sign(universityKeyPair)
        .build();
  }


  public int getBalanceOfApplicant(Applicant applicant, String assertType) {
    val api = IrohaApiSingletone.getIrohaApiInstance();
    val assets = getAllAssertsOfApplicant(applicant);
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
    String universityId = ChainEntitiesUtils.getAccountId(ChainEntitiesUtils.getUniversityAccountName(university),
        ChainEntitiesUtils.getUniversityDomain(university));
    String applicantId = ChainEntitiesUtils.getAccountId(ChainEntitiesUtils.getApplicantAccountName(applicant),UNIVERSITIES_DOMAIN);

    val query = Query.builder(universityId, 1)
            .getAccountAssets(applicantId)
            .buildSigned(universityKeyPair);
    val balance = api.query(query);
    return balance.getAccountAssetsResponse().getAccountAssetsList();
  }


}
