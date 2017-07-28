package connect.wallet.cwallet.inter;

/**
 * Provided to external
 * Created by Administrator on 2017/7/18.
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
