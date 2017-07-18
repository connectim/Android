package connect.wallet.cwallet.account;

import java.util.List;

import connect.wallet.cwallet.currency.BaseCurrency;

/**
 * BTC 币种账户
 * Created by Administrator on 2017/7/18.
 */

public class BtcCoinAccount implements CoinAccount {

    @Override
    public void balance() {

    }

    @Override
    public void hideAddress(String address) {

    }

    @Override
    public List<Object> addressList() {
        return null;
    }

    @Override
    public void transfer(BaseCurrency baseCurrency, double amount, List<String> fromAddress, List<String> toAddress) {

    }
}
