package com.iroha.service;

import com.iroha.utils.ChainEntitiesUtils;
import java.security.KeyPair;

import com.iroha.model.Applicant;
import com.iroha.model.university.University;

import com.iroha.utils.IrohaApiSingletone;
import io.reactivex.Observable;
import iroha.protocol.TransactionOuterClass;
import jp.co.soramitsu.iroha.java.IrohaAPI;
import jp.co.soramitsu.iroha.java.Query;
import jp.co.soramitsu.iroha.java.Transaction;
import lombok.val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.iroha.utils.ChainEntitiesUtils.*;


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

  public KeyPair createNewApplicantAccount(Applicant applicant) {
    val keys = ChainEntitiesUtils.generateKey();
    val applicantAccountName = ChainEntitiesUtils.getApplicantAccountName(applicant);
    val domain = ChainEntitiesUtils.getUniversityDomain(university);
    val transaction = Transaction.builder(ChainEntitiesUtils.getUniversityAccountName(university))
        .createAccount(ChainEntitiesUtils.getAccountId(applicantAccountName, domain), keys.getPublic())
        .sign(universityKeyPair)
        .build();
    api.transaction(transaction).blockingSubscribe();
    return keys;
  }

  public Observable getWildTokensTransaction(Applicant applicant) {
    val transaction = createTransactionFromUniversity(applicant, Consts.WILD_ASSET_NAME, 5);
    return api.transaction(transaction).distinct();
  }

//    public Observable chooseUniversity(ResponseApplicant applicant, KeyPair keyPair)  {
//        val removeWildTokenTransaction = createTransactionToUniversity(applicant,WILD_ASSET_NAME, 1, keyPair);
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
      String assetType, Integer assetsQuantity) {
    String applicant_account = ChainEntitiesUtils.getApplicantAccountName(applicant);
    String university_account = ChainEntitiesUtils.getUniversityAccountName(university);
    String universityAccountId = ChainEntitiesUtils
        .getAccountId(university_account, ChainEntitiesUtils.getUniversityDomain(university));
    String applicationAccountId = ChainEntitiesUtils
        .getAccountId(applicant_account, ChainEntitiesUtils.getUniversityDomain(university));
    return Transaction.builder(university_account)
        .addAssetQuantity(ChainEntitiesUtils.getAssetId(assetType, university.getName()), assetsQuantity.toString())
        .transferAsset(universityAccountId, applicationAccountId,
            ChainEntitiesUtils.getAssetId(assetType, university.getName())
            , "", assetsQuantity.toString())
        .sign(universityKeyPair)
        .build();
  }


  public int getBalanceOfApplicant(Applicant applicant, String assertType) {
    val api = IrohaApiSingletone.getIrohaApiInstance();
    val query = Query.builder(
        ChainEntitiesUtils.getAccountId(ChainEntitiesUtils.getUniversityAccountName(university), ChainEntitiesUtils
            .getUniversityDomain(university)), 1)
        .getAccountAssets(
            ChainEntitiesUtils.getAccountId(ChainEntitiesUtils.getApplicantAccountName(applicant), ChainEntitiesUtils
                .getUniversityDomain(university)))
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
