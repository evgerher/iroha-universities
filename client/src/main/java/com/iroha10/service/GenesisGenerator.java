package com.iroha10.service;

import com.iroha10.model.University;
import iroha.protocol.BlockOuterClass;
import iroha.protocol.Primitive.RolePermission;

import java.util.Arrays;
import java.util.List;

import jp.co.soramitsu.iroha.java.Transaction;
import jp.co.soramitsu.iroha.testcontainers.detail.GenesisBlockBuilder;

public class GenesisGenerator {
    private static final String unibersitiesDomain = "university_selection";
    private static final String uiDomain = "ui";
    private static final String kaiDomain = "kai";
    private static final String kfuDomain = "kfu";
    private static final String tisbiDomain = "tisbi";
    public static final String applicantRole = "applicant";
    public static final String kaiRole = "kai_user";
    public static final String uiRole = "ui_user";
    public static final String tisbiRole = "tisbi_user";
    public static final String kfuRole = "kfu_user";




    public static BlockOuterClass.Block getGenesisBlock(List<University> universities){
        return new GenesisBlockBuilder()
                .addTransaction(addRequiredRoles(universities))
                .addTransaction(addDomains(universities))
                .addTransaction(initialFunding(universities))
                .build();
    }

    private static Transaction initialFunding(List<University> universities) {
        return Transaction.builder(null)
                .build();
    }

    private static Transaction addDomains(List<University> universities) {
        return Transaction.builder(null)
                .createRole(applicantRole,
                        Arrays.asList(
                                RolePermission.can_transfer,
                                RolePermission.can_get_my_acc_ast,
                                RolePermission.can_get_my_txs,
                                RolePermission.can_receive
                        ))
                .build();
    }

    private static Transaction addRequiredRoles(List<University> universities) {
        return Transaction.builder(null)
                .createRole(applicantRole,
                        Arrays.asList(
                                RolePermission.can_transfer,
                                RolePermission.can_get_my_acc_ast,
                                RolePermission.can_get_my_txs,
                                RolePermission.can_receive
                        ))
                .build();

    }

    private static Transaction createUniversityRole(University university){

    }
}