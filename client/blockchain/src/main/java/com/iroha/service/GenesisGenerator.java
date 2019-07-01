package com.iroha.service;

import iroha.protocol.BlockOuterClass;

public interface GenesisGenerator {
    BlockOuterClass.Block getGenesisBlock();
}
