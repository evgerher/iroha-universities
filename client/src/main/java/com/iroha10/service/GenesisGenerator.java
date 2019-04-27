package com.iroha10.service;

import com.iroha10.model.university.Speciality;
import com.iroha10.model.university.University;
import iroha.protocol.BlockOuterClass;
import iroha.protocol.Primitive.RolePermission;

import java.util.*;
import java.security.KeyPair;
import jp.co.soramitsu.crypto.ed25519.Ed25519Sha3;

import jp.co.soramitsu.iroha.java.Transaction;
import jp.co.soramitsu.iroha.testcontainers.detail.GenesisBlockBuilder;

public class GenesisGenerator {
    private static final String unibersitiesDomain = "universitySelection";
    public static final String applicantRole = "applicant";
    private static final Ed25519Sha3 crypto = new Ed25519Sha3();
    public static Map<String,KeyPair> universitiesKeys;
    public static final String wildAssetName = "wild";



    public static BlockOuterClass.Block getGenesisBlock(List<University> universities){
        generateKeys(universities);
        GenesisBlockBuilder genesisbuilder = new GenesisBlockBuilder();
        for(Transaction transaction: getRequiredRoles(universities)){
            genesisbuilder = genesisbuilder.addTransaction(transaction.build());
        }
        for(Transaction transaction: getDomains(universities)){
            genesisbuilder = genesisbuilder.addTransaction(transaction.build());
        }
        for(Transaction transaction: getAccounts(universities)){
            genesisbuilder = genesisbuilder.addTransaction(transaction.build());
        }
        for(Transaction transaction: initialFunding(universities)){
            genesisbuilder = genesisbuilder.addTransaction(transaction.build());
        }
      return genesisbuilder.build();
    }


    private static List<Transaction> initialFunding(List<University> universities) {
        List<Transaction> transactions = new ArrayList<>();
        for(University university: universities){
            for(Speciality speciality:university.getSpecialities()){
                String assetName = getAssetName(speciality.getName(),university.getName());
                String assetId = getAssetId(speciality.getName(),university.getName());
                transactions.add(Transaction.builder(null)
                        .createAsset(assetName,university.getName(),0)
                        .addAssetQuantity(assetId,Integer.toString(speciality.getQuantity()))
                        .build());

            }
        }
        transactions.add(Transaction.builder(null)
            .createAsset(wildAssetName,unibersitiesDomain,0)
            .build());
        return transactions;
    }


    private static List<Transaction> getDomains(List<University> universities) {
        List<Transaction> transactions =  new ArrayList<>();
        for(University university: universities){
            transactions.add(Transaction.builder(null)
                    .createDomain(university.getName(),university.getName())
                    .build()
            );
        }
        transactions.add(Transaction.builder(null)
                .createDomain(unibersitiesDomain,applicantRole)
                .build()
        );
        return transactions;
    }

    private static List<Transaction> getRequiredRoles(List<University> universities) {
        List<Transaction> transactions =  new ArrayList<>();
        for (University university: universities) {
            transactions.add(Transaction.builder(null)
                    .createRole(university.getName(),
                            Arrays.asList(
                                    RolePermission.can_add_asset_qty,
                                    RolePermission.can_add_domain_asset_qty,
                                    RolePermission.can_create_asset,
                                    RolePermission.can_read_assets,
                                    RolePermission.can_receive,
                                    RolePermission.can_transfer,
                                    RolePermission.can_get_my_acc_ast,
                                    RolePermission.can_get_my_txs
                            )
            ).build());

        }
        transactions.add(Transaction.builder(null)
                .createRole(applicantRole,
                        Arrays.asList(
                                RolePermission.can_receive,
                                RolePermission.can_transfer,
                                RolePermission.can_get_my_acc_ast,
                                RolePermission.can_get_my_txs
                        )
                ).build());
        return transactions;
    }

    private static List<Transaction> getAccounts(List<University> universities){
        List<Transaction> transactions =  new ArrayList<>();
        for(University university: universities){
            transactions.add(Transaction.builder(null)
            .createAccount(university.getName(),university.getName(),universitiesKeys.get(university.getName()).getPublic())
                    .build());
        }
        return transactions;


    }

    private static void generateKeys(List<University> universities){
         universitiesKeys = new HashMap<>();
        for(University university: universities){
            universitiesKeys.put(university.getName(),crypto.generateKeypair());
        }
    }

    private static String getAssetName(String specialityName,String universityName){
        return String.format("%s%s",specialityName, universityName);
    }
    private static String getAssetId(String specialityName,String universityName){
        return String.format("%s#%s",specialityName, universityName);
    }
}