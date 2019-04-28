package com.iroha.service;

import com.iroha.model.university.Speciality;
import com.iroha.model.university.University;
import com.iroha.utils.ChainEntitiesUtils;
import iroha.protocol.BlockOuterClass;
import iroha.protocol.Primitive.RolePermission;

import java.util.*;
import jp.co.soramitsu.iroha.java.Transaction;
import jp.co.soramitsu.iroha.testcontainers.detail.GenesisBlockBuilder;

import static com.iroha.utils.ChainEntitiesUtils.*;
import static com.iroha.utils.ChainEntitiesUtils.Consts.APPLICANT_ROLE;
import static com.iroha.utils.ChainEntitiesUtils.Consts.UNIVERSITIES_DOMAIN;
import static com.iroha.utils.ChainEntitiesUtils.Consts.WILD_ASSET_NAME;

public class GenesisGenerator {
    public static BlockOuterClass.Block getGenesisBlock(List<University> universities) {
        ChainEntitiesUtils.generateKeys(universities);
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

        genesisbuilder.addTransaction(Transaction.builder(null)  //TODO remove, `dded for testing
                .addPeer("0.0.0.0:10001", ChainEntitiesUtils.universitiesKeys.get("ui").getPublic())
                .build().build());
        return genesisbuilder.build();
    }


    private static List<Transaction> initialFunding(List<University> universities) {
        List<Transaction> transactions = new ArrayList<>();
        for (University university : universities) {
            transactions.add(Transaction.builder(null)
                    .createAsset(Consts.WILD_ASSET_NAME, ChainEntitiesUtils.getUniversityDomain(university), 0)
                    .build());
            for (Speciality speciality : university.getSpecialities()) {
                String assetName = ChainEntitiesUtils.getAssetName(speciality.getName(), university.getName());
                String assetId = ChainEntitiesUtils.getAssetId(speciality.getName(), university.getName());
                transactions.add(Transaction.builder(null)
                        .createAsset(assetName, ChainEntitiesUtils.getUniversityDomain(university), 0)
                        .addAssetQuantity(assetId, Integer.toString(speciality.getQuantity()))
                        .build());

            }
        }
        transactions.add(Transaction.builder(null)
                .createAsset(Consts.WILD_ASSET_NAME, Consts.UNIVERSITIES_DOMAIN, 0)
                .build());
        return transactions;
    }


    private static List<Transaction> getDomains(List<University> universities) {
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

    private static List<Transaction> getRequiredRoles(List<University> universities) {
        List<Transaction> transactions = new ArrayList<>();
        for (University university : universities) {
            transactions.add(Transaction.builder(null)
                    .createRole(ChainEntitiesUtils.getUniversityRole(university),
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
                                    RolePermission.can_get_all_acc_detail,
                                    RolePermission.can_create_account
                            )
                    ).build());

        }
        transactions.add(Transaction.builder(null)
                .createRole(Consts.APPLICANT_ROLE,
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
                    .createAccount(ChainEntitiesUtils.getUniversityAccountName(university), ChainEntitiesUtils
                        .getUniversityDomain(university), ChainEntitiesUtils.universitiesKeys.get(university.getName()).getPublic())
                    .build());
        }
        return transactions;


    }


}