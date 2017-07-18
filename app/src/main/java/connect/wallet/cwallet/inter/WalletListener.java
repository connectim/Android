package connect.wallet.cwallet.inter;

/**
 * 提供给外部的
 * Created by Administrator on 2017/7/18.
 */
public interface WalletListener<T> {

    enum WalletError {
        DBError,//数据异常
        NETError,//网络异常
        ACCOUNTError,//账户异常
    }

    void success(T t);

    void fail(WalletError error);
}
