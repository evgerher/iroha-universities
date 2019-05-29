package com.iroha.utils;

import iroha.protocol.TransactionOuterClass;
import jp.co.soramitsu.iroha.java.Transaction;
import jp.co.soramitsu.iroha.java.Utils;

import java.security.KeyPair;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class IrohaUtils {
    public static Iterable<TransactionOuterClass.Transaction> createBatch(Iterable<TransactionOuterClass.Transaction> list,
                                                                           TransactionOuterClass.Transaction.Payload.BatchMeta.BatchType batchType,
                                                                           Map<TransactionOuterClass.Transaction,KeyPair> keys) {
        final Iterable<String> batchHashes = Utils.getProtoBatchHashesHex(list);

        return StreamSupport.stream(list.spliterator(), false)
                .map(tx -> Transaction
                        .parseFrom(tx)
                        .makeMutable()
                        .setBatchMeta(batchType, batchHashes)
                        .sign(keys.get(tx))
                        .build()
                )
                .collect(Collectors.toList());
    }
}
