package com.iroha.service;

import com.google.protobuf.util.JsonFormat;
import com.iroha.model.university.Speciality;
import com.iroha.model.university.University;
import com.iroha.utils.ChainEntitiesUtils;

import iroha.protocol.BlockOuterClass;
import iroha.protocol.Primitive.RolePermission;

import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.security.KeyPair;
import java.util.*;

import jp.co.soramitsu.iroha.java.Transaction;
import jp.co.soramitsu.iroha.testcontainers.detail.GenesisBlockBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.iroha.utils.ChainEntitiesUtils.*;

public class GenesisGenerator {
    private static final Logger logger = LoggerFactory.getLogger(GenesisGenerator.class);


    public static BlockOuterClass.Block getGenesisBlock(List<University> universities, Map<String, KeyPair> keys) {
        logger.info("Generate genesis block for universities, amount={}", universities.size());

        GenesisBlockBuilder genesisbuilder = new GenesisBlockBuilder();
        for( University university: universities) {
            KeyPair uniKeys = keys.get(university.getName());
            logger.info("Add peer={}, pubkey={}", university.getUri(), ChainEntitiesUtils.bytesToHex(uniKeys.getPublic().getEncoded()));

            genesisbuilder = genesisbuilder.addTransaction(Transaction.builder(null)  //TODO remove, `dded for testing
                    .addPeer(university.getUri(), keys.get(university.getName()).getPublic())
                    .build().build());
        }

        logger.info("Add roles for each university");
        for (Transaction transaction : getRequiredRoles(universities)) {
            genesisbuilder = genesisbuilder.addTransaction(transaction.build());
        }

        logger.info("Add domains for each university");
        for (Transaction transaction : getDomains(universities)) {
            genesisbuilder = genesisbuilder.addTransaction(transaction.build());
        }

        logger.info("Add accounts for each university");
        for (Transaction transaction : getAccounts(universities, keys)) {
            genesisbuilder = genesisbuilder.addTransaction(transaction.build());
        }

        logger.info("Add initial funding for each university");
        for (Transaction transaction : initialFunding(universities)) {
            genesisbuilder = genesisbuilder.addTransaction(transaction.build());
        }

        return genesisbuilder.build();
    }

    public static void writeGenesisToFiles(BlockOuterClass.Block genesis, String[] paths) {
        for (String path: paths)
            writeGenesisToFile(genesis, path);
    }

    public static void writeGenesisToFile(BlockOuterClass.Block genesis, String path) {
        try (FileOutputStream file = new FileOutputStream(path)) {
            file.write(JsonFormat.printer().print(genesis).getBytes());
            file.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveKey(KeyPair keyPair, String path) {
        try (FileOutputStream filePub = new FileOutputStream(path+"/node.pub")) {
            try (FileOutputStream filePriv = new FileOutputStream(path+"/node.priv")) {
                filePub.write(bytesToHex(keyPair.getPublic().getEncoded()).getBytes());
                filePub.flush();
                filePriv.write(bytesToHex(keyPair.getPrivate().getEncoded()).getBytes());
                filePriv.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static List<Transaction> initialFunding(List<University> universities) {
        List<Transaction> transactions = new ArrayList<>();
        for (University university : universities) {
            String domain = ChainEntitiesUtils.getUniversityDomain(university);
            logger.info("Initial funding of domain={}", domain);

            transactions.add(Transaction.builder(null)
                    .createAsset(Consts.WILD_ASSET_NAME, domain, 0)
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
                transactions.add(Transaction.builder(getAccountId(getUniversityAccountName(university),getUniversityDomain(university)))
                        .addAssetQuantity(assetId, new BigDecimal(speciality.getQuantity()))
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

    private static List<Transaction> getAccounts(List<University> universities, Map<String, KeyPair> universitiesKeys) {
        List<Transaction> transactions = new ArrayList<>();
        for (University university : universities) {
            transactions.add(Transaction.builder(null)
                    .createAccount(ChainEntitiesUtils.getUniversityAccountName(university), ChainEntitiesUtils
                        .getUniversityDomain(university), universitiesKeys.get(university.getName()).getPublic())
                    .build());
        }
        return transactions;
    }
}
