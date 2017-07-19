package connect.wallet.cwallet.account;

import com.google.protobuf.GeneratedMessageV3;

import java.util.List;

import connect.wallet.cwallet.currency.BaseCurrency;
import connect.wallet.cwallet.inter.WalletListener;

/**
 * Created by Administrator on 2017/7/18.
 */

public interface CoinAccount {

    /**
     * 余额
     */
    public void balance();

    /**
     * 隐藏地址
     */
    public void hideAddress(String address);

    /**
     * 获取地址列表
     */
    public List<Object> addressList();

    /**
     * 转账
     */
    public void transfer(String url, GeneratedMessageV3 body, WalletListener listener);
}
