package com.wallet.inter;

/**
 * The purse the callback
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
