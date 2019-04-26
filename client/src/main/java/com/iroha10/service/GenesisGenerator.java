package com.iroha10.service;

import com.iroha10.model.Speciality;
import com.iroha10.model.University;
import iroha.protocol.BlockOuterClass;
import iroha.protocol.Primitive.RolePermission;

import java.util.*;


import jp.co.soramitsu.iroha.java.Transaction;
import jp.co.soramitsu.iroha.testcontainers.detail.GenesisBlockBuilder;

import static com.iroha10.utils.ChainEntitiesUtils.*;
import static com.iroha10.utils.ChainEntitiesUtils.Consts.APPLICANT_ROLE;
import static com.iroha10.utils.ChainEntitiesUtils.Consts.UNIVERSITIES_DOMAIN;
import static com.iroha10.utils.ChainEntitiesUtils.Consts.WILD_ASSET_NAME;

public class GenesisGenerator {
    public static BlockOuterClass.Block getGenesisBlock(List<University> universities) {
        generateKeys(universities);
        GenesisBlockBuilder genesisbuilder = new GenesisBlockBuilder();
        for (Transaction transaction : getRequiredRoles(universities)) {
            genesisbuilder = genesisbuilder.addTransaction(transaction.build());
        }
        for (Transaction transaction : getDomains(universities)) {
            genesisbuilder = genesisbuilder.addTransaction(transaction.build());
        }
        for (Transaction transaction : getAccounts(universities)) {
            genesisbuilder = genesisbuilder.addTransaction(transaction.build());
        }
        for (Transaction transaction : initialFunding(universities)) {
            genesisbuilder = genesisbuilder.addTransaction(transaction.build());
        }
        return genesisbuilder.build();
    }


    private static List<Transaction> initialFunding(List<University> universities) {
        List<Transaction> transactions = new ArrayList<>();
        for (University university : universities) {
            for (Speciality speciality : university.getSpecialities()) {
                String assetName = getAssetName(speciality.getName(), university.getName());
                String assetId = getAssetId(speciality.getName(), university.getName());
                transactions.add(Transaction.builder(null)
                        .createAsset(assetName, getUniversityDomain(university), 0)
                        .addAssetQuantity(assetId, Integer.toString(speciality.getQuantity()))
                        .build());

            }
        }
        transactions.add(Transaction.builder(null)
                .createAsset(WILD_ASSET_NAME, UNIVERSITIES_DOMAIN, 0)
                .build());
        return transactions;
    }


    private static List<Transaction> getDomains(List<University> universities) {
        List<Transaction> transactions = new ArrayList<>();
        for (University university : universities) {
            transactions.add(Transaction.builder(null)
                    .createDomain(getUniversityDomain(university), getUniversityRole(university))
                    .build()
            );
        }
        transactions.add(Transaction.builder(null)
                .createDomain(UNIVERSITIES_DOMAIN, APPLICANT_ROLE)
                .build()
        );
        return transactions;
    }

    private static List<Transaction> getRequiredRoles(List<University> universities) {
        List<Transaction> transactions = new ArrayList<>();
        for (University university : universities) {
            transactions.add(Transaction.builder(null)
                    .createRole(getUniversityRole(university),
                            Arrays.asList(
                                    RolePermission.can_add_asset_qty,
                                    RolePermission.can_add_domain_asset_qty,
                                    RolePermission.can_create_asset,
                                    RolePermission.can_read_assets,
                                    RolePermission.can_receive,
                                    RolePermission.can_transfer,
                                    RolePermission.can_get_my_acc_ast,
                                    RolePermission.can_get_my_txs,
                                    RolePermission.can_get_all_acc_ast,
                                    RolePermission.can_get_all_acc_detail
                            )
                    ).build());

        }
        transactions.add(Transaction.builder(null)
                .createRole(APPLICANT_ROLE,
                        Arrays.asList(
                                RolePermission.can_receive,
                                RolePermission.can_transfer,
                                RolePermission.can_get_my_acc_ast,
                                RolePermission.can_get_my_txs
                        )
                ).build());
        return transactions;
    }

    private static List<Transaction> getAccounts(List<University> universities) {
        List<Transaction> transactions = new ArrayList<>();
        for (University university : universities) {
            transactions.add(Transaction.builder(null)
                    .createAccount(getUniversityAccountName(university), getUniversityDomain(university), universitiesKeys.get(university.getName()).getPublic())
                    .build());
        }
        return transactions;


    }


}