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
     * Balance
     */
    public void balance();

    /**
     * Hide the address
     */
    public void hideAddress(String address);

    /**
     * Get the address list
     */
    public void requestAddressList(WalletListener listener);

    /**
     * transfer
     */
    public void transfer(String url, GeneratedMessageV3 body, WalletListener listener);
}
