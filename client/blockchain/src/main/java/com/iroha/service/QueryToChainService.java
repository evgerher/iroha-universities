package com.iroha.service;

import com.iroha.model.Applicant;
import com.iroha.model.university.University;
import com.iroha.utils.ChainEntitiesUtils;
import com.iroha.utils.IrohaApiSingletone;
import iroha.protocol.QryResponses;
import iroha.protocol.Queries;
import jp.co.soramitsu.iroha.java.IrohaAPI;
import jp.co.soramitsu.iroha.java.Query;
import lombok.val;

import java.security.KeyPair;
import java.util.List;

import static com.iroha.utils.ChainEntitiesUtils.ChainLogicConstants.UNIVERSITIES_DOMAIN;
import static com.iroha.utils.ChainEntitiesUtils.getAccountId;
import static com.iroha.utils.ChainEntitiesUtils.getUniversityAccountName;
import static com.iroha.utils.ChainEntitiesUtils.getUniversityDomain;

public class QueryToChainService {
    private KeyPair keyPair;
    private University university;
    private IrohaAPI api;

    public QueryToChainService(KeyPair keyPair, University university) { // TODO rewrite for current user instead of university
        this.keyPair = keyPair;
        this.university = university;
        api = IrohaApiSingletone.getIrohaApiInstance();
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

    public List<QryResponses.AccountAsset> getAllAssertsOfApplicant(Applicant applicant) {
        val api = IrohaApiSingletone.getIrohaApiInstance();
        val universityId = ChainEntitiesUtils.getAccountId(ChainEntitiesUtils.getUniversityAccountName(university),
                ChainEntitiesUtils.getUniversityDomain(university));
        val applicantId = ChainEntitiesUtils.getAccountId(ChainEntitiesUtils.getApplicantAccountName(applicant), UNIVERSITIES_DOMAIN);

        val query = Query.builder(universityId, 1)
                .getAccountAssets(applicantId)
                .buildSigned(keyPair);
        val balance = api.query(query);
        return balance.getAccountAssetsResponse().getAccountAssetsList();
    }

    public QryResponses.Account getAccount(String txhash) {
        val q = constructQueryAccount(txhash);
        return api.query(q).getAccountResponse().getAccount();
    }

    private Queries.Query constructQueryAccount(String accountId) {
        val universityAccount = getAccountId(getUniversityAccountName(university), getUniversityDomain(university));
        return Query.builder(universityAccount, 1).getAccount(accountId).buildSigned(keyPair);
    }
}
