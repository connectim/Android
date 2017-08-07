package connect.wallet;

import org.junit.Test;

import java.security.SecureRandom;

import connect.utils.StringUtil;
import connect.utils.cryption.SupportKeyUril;
import connect.wallet.cwallet.NativeWallet;
import connect.wallet.cwallet.account.CoinAccount;
import connect.wallet.cwallet.bean.CurrencyEnum;
import connect.wallet.cwallet.currency.BaseCurrency;

import static org.junit.Assert.assertTrue;

/**
 * Created by Administrator on 2017/8/3 0003.
 */

public class BtcCurrencyTest {

    private String Tag = "_BtcCurrencyTest";

    @Test
    public void getCurrencyData() throws Exception {
        BaseCurrency baseCurrency = NativeWallet.getInstance().initCurrency(CurrencyEnum.BTC);
        baseCurrency.getCurrencyData();
    }

    @Test
    public void createAddress() throws Exception {
        /*salt = StringUtil.bytesToHexString(SecureRandom.getSeed(64));
        String currencySeed = SupportKeyUril.xor(value, salt);*/
        BaseCurrency baseCurrency = NativeWallet.getInstance().initCurrency(CurrencyEnum.BTC);
        String currencySeed = StringUtil.bytesToHexString(SecureRandom.getSeed(64));
        String address = baseCurrency.createAddress(currencySeed);
        assertTrue(SupportKeyUril.checkAddress(address));
    }


}
