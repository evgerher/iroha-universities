package com.iroha.utils;

import com.iroha.IrohaConfig;
import jp.co.soramitsu.iroha.java.IrohaAPI;

public class IrohaApiSingletone {
    private static IrohaAPI api;
    private IrohaApiSingletone(){
        api = new IrohaAPI(IrohaConfig.host,IrohaConfig.port);
    }

    public static IrohaAPI getIrohaApiInstance(){
        if(api == null){
            new IrohaApiSingletone();
            return api;
        }else {
            return api;
        }
    }
}
