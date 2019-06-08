package com.iroha.utils;

import com.google.protobuf.util.JsonFormat;
import iroha.protocol.BlockOuterClass;
import iroha.protocol.Primitive;

import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyPair;
import java.util.Arrays;
import java.util.List;

import static com.iroha.utils.ChainEntitiesUtils.bytesToHex;

public class GenesisGeneratorUtils {

    public static final List<Primitive.RolePermission> universityRolePermissions = Arrays.asList(
            Primitive.RolePermission.can_add_asset_qty,
            Primitive.RolePermission.can_add_domain_asset_qty,
            Primitive.RolePermission.can_create_asset,
            Primitive.RolePermission.can_read_assets,
            Primitive.RolePermission.can_receive,
            Primitive.RolePermission.can_transfer,
            Primitive.RolePermission.can_get_my_acc_ast,
            Primitive.RolePermission.can_get_my_txs,
            Primitive.RolePermission.can_get_all_acc_ast,
            Primitive.RolePermission.can_get_all_acc_detail,
            Primitive.RolePermission.can_create_account
    ) ;
    public static final List<Primitive.RolePermission> applicantRolePermissions = Arrays.asList(
            Primitive.RolePermission.can_receive,
            Primitive.RolePermission.can_transfer,
            Primitive.RolePermission.can_get_my_acc_ast,
            Primitive.RolePermission.can_get_my_txs);



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
