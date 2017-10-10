package com.wallet.inter;

/**
 * 钱包调用回调
 */
public interface WalletListener<T> {

    String success = "success";

    enum WalletError {
        DBError,
        NETError,
        ACCOUNTError,
    }

    void success(T t);

    void fail(WalletError error);
}
