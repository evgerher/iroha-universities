package com.iroha.utils;

import com.google.protobuf.util.JsonFormat;
import iroha.protocol.BlockOuterClass;

import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyPair;

import static com.iroha.utils.ChainEntitiesUtils.bytesToHex;

public class GenesisGeneratorUtils {
    public static void writeGenesisToFiles(BlockOuterClass.Block genesis, String[] paths) {
        for (String path : paths)
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
        try (FileOutputStream filePub = new FileOutputStream(path + "/node.pub")) {
            try (FileOutputStream filePriv = new FileOutputStream(path + "/node.priv")) {
                filePub.write(bytesToHex(keyPair.getPublic().getEncoded()).getBytes());
                filePub.flush();
                filePriv.write(bytesToHex(keyPair.getPrivate().getEncoded()).getBytes());
                filePriv.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
