package com.iroha;

import static com.iroha.utils.ChainEntitiesUtils.Consts.UNIVERSITIES_DOMAIN;
import static com.iroha.utils.ChainEntitiesUtils.Consts.WILD_ASSET_NAME;

import com.iroha.dao.MongoDBConnector;
import com.iroha.model.university.University;
import com.iroha.utils.ChainEntitiesUtils;
import com.iroha.utils.IrohaApiSingletone;
import iroha.protocol.TransactionOuterClass;

import java.security.KeyPair;
import java.time.Instant;
import java.util.Arrays;

import jp.co.soramitsu.iroha.java.IrohaAPI;
import jp.co.soramitsu.iroha.java.Query;
import jp.co.soramitsu.iroha.java.Transaction;
import jp.co.soramitsu.iroha.java.TransactionStatusObserver;
import jp.co.soramitsu.iroha.java.Utils;
import jp.co.soramitsu.iroha.java.subscription.WaitForTerminalStatus;

import lombok.val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestMain {
  private static final Logger logger = LoggerFactory.getLogger(TestMain.class);
  private static WaitForTerminalStatus waiter = new WaitForTerminalStatus();

  public static void main(String[] args) throws Exception {
    MongoDBConnector conn = new MongoDBConnector();
    University uni = conn.getUniversity("kai");
    KeyPair key = conn.getUniversityKeys("kai");
    uni.setPeerKey(key);

    IrohaAPI api = IrohaApiSingletone.getIrohaApiInstance();

    val observer = TransactionStatusObserver.builder()
        // executed when stateless or stateful validation is failed
        .onTransactionFailed(t -> logger.info(String.format(
            "transaction %s failed with msg: %s",
            t.getTxHash(),
            t.getErrOrCmdName()

        )))
        // executed when got any exception in handlers or grpc
        .onError(e -> logger.info("Failed with exception: " + e))
        // executed when we receive "committed" status
        .onTransactionCommitted((t) -> logger.info("Committed :) {}", t.getErrOrCmdNameBytes()))
        // executed when transfer is complete (failed or succeed) and observable is closed
        .onComplete(() -> logger.info("Complete0"))
        .onTransactionSent(() -> logger.info("sent"))
        .onNotReceived(e -> logger.info("not received with: "+ e))
        .onEnoughSignaturesCollected(t -> logger.info("sigs collected: "+ t))
        .onMstExpired(t -> logger.info("mst expired: "+ t))
        .onMstPending(t -> logger.info("pending: "+ t))
        .onStatelessValidationSuccess(t -> logger.info("sls val: "+ t))
        .onUnrecognizedStatus(t -> logger.info("unrecognized"+t))
        .onStatefulValidationSuccess(t -> logger.info("slf val: "+ t))
        .onUnrecognizedStatus(t -> logger.info("HZ"))
        .onRejected(t -> logger.info("rejected" + t))
        .build();

    String accountId = ChainEntitiesUtils.getAccountId(ChainEntitiesUtils.getUniversityAccountName(uni), ChainEntitiesUtils.getUniversityDomain(uni));
    TransactionOuterClass.Transaction t1 = Transaction.builder(accountId, Instant.now())
        .addAssetQuantity("bskaidomain#kaidomain", "10")
        .sign(uni.getPeerKey())
        .build();
    TransactionOuterClass.Transaction t2 = Transaction.builder(accountId, Instant.now())
        .addAssetQuantity("bskaidomain#kaidomain", "5")
        .sign(uni.getPeerKey())
        .build();

//    Applicant applicant = conn.getApplicant("abcaaabaafdadeecbcfbccecaadeba");
//    val t11 = createTransactionFromUniversity()
    // Sizes of batch_meta and provided transactions are different

//    TransactionOuterClass.Transaction tr2 = Transaction.builder(accountId)
//        .setBatchMeta(BatchType.ATOMIC, Arrays.asList(t1.getReducedHashHex(), t2.getReducedHashHex()))
//        .createAsset(ChainEntitiesUtils.getAssetName("asset", ChainEntitiesUtils.getUniversityDomain(uni)), ChainEntitiesUtils.getUniversityDomain(uni), 0)
//        .createAsset(ChainEntitiesUtils.getAssetName("tesseract", ChainEntitiesUtils.getUniversityDomain(uni)), ChainEntitiesUtils.getUniversityDomain(uni), 0)
//        .sign(uni.getPeerKey())
//        .build();


//    val  tx = Transaction.builder(accountId)
//        .addAssetQuantity(ChainEntitiesUtils.getAssetId("tesseract", ChainEntitiesUtils.getUniversityDomain(uni)), "10")
//        .sign(uni.getPeerKey())
//        .build();
    val q = Query.builder(accountId, 1)
        .getAccountAssets(accountId)
        .buildSigned(uni.getPeerKey());

    val assets1 = api.query(q).getAccountAssetsResponse().getAccountAssetsList();

    val txs = Utils.createTxAtomicBatch(Arrays.asList(t1, t2), uni.getPeerKey());
    api.transactionListSync(txs);


    waiter.subscribe(api, Utils.hash(t1)).subscribe(observer);
    waiter.subscribe(api, Utils.hash(t2)).subscribe(observer);

    Thread.sleep(20000);

    val qq = Query.builder(accountId, 1)
        .getAccountAssets(accountId)
        .buildSigned(uni.getPeerKey());

    val assets2 = api.query(qq).getAccountAssetsResponse().getAccountAssetsList();

//
////    api.transaction(t1.makeMutable().sign(uni.getPeerKey()).build()).subscribe(observer);
////    api.transaction(t2.makeMutable().sign(uni.getPeerKey()).build()).subscribe(observer);
//    api.transaction(tx).subscribe(observer);
//
    Thread.sleep(60000);
  }
}
