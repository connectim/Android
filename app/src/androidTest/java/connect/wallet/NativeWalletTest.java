package connect.wallet;

import org.junit.Test;

import connect.wallet.cwallet.NativeWallet;
import connect.wallet.cwallet.account.CoinAccount;
import connect.wallet.cwallet.bean.CurrencyEnum;
import connect.wallet.cwallet.currency.BaseCurrency;

import static org.junit.Assert.assertTrue;

/**
 * Created by Administrator on 2017/8/3 0003.
 */

public class NativeWalletTest {

    private String Tag = "_NativeWalletTest";

    @Test
    public void initAccount() throws Exception {
        CoinAccount coinAccount = NativeWallet.getInstance().initAccount(CurrencyEnum.BTC);
        assertTrue(coinAccount instanceof CoinAccount);
    }

    @Test
    public void initCurrency() throws Exception {
        BaseCurrency baseCurrency = NativeWallet.getInstance().initCurrency(CurrencyEnum.BTC);
        assertTrue(baseCurrency instanceof BaseCurrency);
    }

}
