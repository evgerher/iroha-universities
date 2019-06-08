package com.iroha.service.impl;

import com.iroha.model.university.Speciality;
import com.iroha.model.university.University;
import com.iroha.utils.ChainEntitiesUtils;

import iroha.protocol.BlockOuterClass;
import iroha.protocol.Primitive.RolePermission;

import java.math.BigDecimal;
import java.security.KeyPair;
import java.util.*;

import jp.co.soramitsu.iroha.java.Transaction;
import jp.co.soramitsu.iroha.testcontainers.detail.GenesisBlockBuilder;


import lombok.var;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.iroha.utils.ChainEntitiesUtils.*;
import static com.iroha.utils.GenesisGeneratorUtils.applicantRolePermissions;
import static com.iroha.utils.GenesisGeneratorUtils.universityRolePermissions;

public class GenesisGeneratorImpl {
    private static final Logger logger = LoggerFactory.getLogger(GenesisGeneratorImpl.class);

    private List<University> universities;
    private Map<String, KeyPair> keys;

    public GenesisGeneratorImpl(List<University> universities, Map<String, KeyPair> keys){
        this.universities = universities;
        this.keys = keys;
    }

    public  BlockOuterClass.Block getGenesisBlock() {
        logger.info("Generate genesis block for universities, amount={}", universities.size());

        var genesisBuilder = new GenesisBlockBuilder();
        for (University university : universities) {
            KeyPair uniKeys = keys.get(university.getName());
            logger.info("Add peer={}, pubkey={}", university.getUri(), ChainEntitiesUtils.bytesToHex(uniKeys.getPublic().getEncoded()));

            genesisBuilder = genesisBuilder.addTransaction(Transaction.builder(null)  //TODO remove, `dded for testing
                    .addPeer(university.getUri(), keys.get(university.getName()).getPublic())
                    .build().build());
        }

        logger.info("Add roles for each university");
        for (Transaction transaction : getRequiredRoles()) {
            genesisBuilder = genesisBuilder.addTransaction(transaction.build());
        }

        logger.info("Add domains for each university");
        for (Transaction transaction : getDomains()) {
            genesisBuilder = genesisBuilder.addTransaction(transaction.build());
        }

        logger.info("Add accounts for each university");
        for (Transaction transaction : getAccounts()) {
            genesisBuilder = genesisBuilder.addTransaction(transaction.build());
        }

        logger.info("Add initial funding for each university");
        for (Transaction transaction : initialFunding()) {
            genesisBuilder = genesisBuilder.addTransaction(transaction.build());
        }

        return genesisBuilder.build();
    }


    private  List<Transaction> initialFunding() {
        List<Transaction> transactions = new ArrayList<>();
        for (University university : universities) {
            String domain = ChainEntitiesUtils.getUniversityDomain(university);
            logger.info("Initial funding of domain={}", domain);

            transactions.add(Transaction.builder(null)
                    .createAsset(Consts.WILD_SPECIALITY_ASSET_NAME, domain, 0)
                    .build());
            for (Speciality speciality : university.getSpecialities()) {
                String assetName = ChainEntitiesUtils.getAssetName(speciality.getName(), getUniversityDomain(university));
                String assetId = ChainEntitiesUtils.getAssetId(assetName, getUniversityDomain(university));

                logger.info("Initial funding of assetName={}, assetId={}, quantity={}",
                        assetName, assetId, speciality.getQuantity());
                transactions.add(Transaction
                        .builder(null)
                        .createAsset(assetName, ChainEntitiesUtils.getUniversityDomain(university), 0)
                        .build());
                transactions.add(Transaction.builder(getAccountId(getUniversityAccountName(university), getUniversityDomain(university)))
                        .addAssetQuantity(assetId, new BigDecimal(speciality.getQuantity()))
                        .build());

            }
        }
        transactions.add(Transaction.builder(null)
                .createAsset(Consts.WILD_ASSET_NAME, Consts.UNIVERSITIES_DOMAIN, 0)
                .build());
        return transactions;
    }


    private List<Transaction> getDomains() {
        List<Transaction> transactions = new ArrayList<>();
        for (University university : universities) {
            transactions.add(Transaction.builder(null)
                    .createDomain(ChainEntitiesUtils.getUniversityDomain(university), ChainEntitiesUtils
                            .getUniversityRole(university))
                    .build()
            );
        }
        transactions.add(Transaction.builder(null)
                .createDomain(Consts.UNIVERSITIES_DOMAIN, Consts.APPLICANT_ROLE)
                .build()
        );
        return transactions;
    }

    private List<Transaction> getRequiredRoles() {
        List<Transaction> transactions = new ArrayList<>();
        for (University university : universities) {
            transactions.add(Transaction.builder(null)
                    .createRole(ChainEntitiesUtils.getUniversityRole(university),
                        universityRolePermissions
                    ).build());

        }
        transactions.add(Transaction.builder(null)
                .createRole(Consts.APPLICANT_ROLE,
                        applicantRolePermissions
                ).build());
        return transactions;
    }

    private List<Transaction> getAccounts() {
        List<Transaction> transactions = new ArrayList<>();
        for (University university : universities) {
            transactions.add(Transaction.builder(null)
                    .createAccount(ChainEntitiesUtils.getUniversityAccountName(university), ChainEntitiesUtils
                            .getUniversityDomain(university), keys.get(university.getName()).getPublic())
                    .build());
        }
        return transactions;
    }
}
