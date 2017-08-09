package connect.wallet;

import org.junit.Test;

import java.security.SecureRandom;

import connect.utils.StringUtil;
import connect.utils.cryption.SupportKeyUril;
import connect.wallet.cwallet.NativeWallet;
import connect.wallet.cwallet.bean.CurrencyEnum;
import connect.wallet.cwallet.currency.BaseCurrency;
import connect.wallet.cwallet.currency.BtcCurrency;

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
        BaseCurrency baseCurrency = NativeWallet.getInstance().initCurrency(CurrencyEnum.BTC);
        String currencySeed = StringUtil.bytesToHexString(SecureRandom.getSeed(64));
        String address = baseCurrency.createAddress(currencySeed);
        assertTrue(SupportKeyUril.checkAddress(address));
    }

    @Test
    public void createPriKey() throws Exception {
        BaseCurrency baseCurrency = NativeWallet.getInstance().initCurrency(CurrencyEnum.BTC);
        String baseSeed = StringUtil.bytesToHexString(SecureRandom.getSeed(64));
        String salt = StringUtil.bytesToHexString(SecureRandom.getSeed(64));
        String priKey = baseCurrency.createPriKey(baseSeed, salt, 0);
        assertTrue(SupportKeyUril.checkPrikey(priKey));
    }

    @Test
    public void doubleToLongCurrency() throws Exception {
        BaseCurrency baseCurrency = NativeWallet.getInstance().initCurrency(CurrencyEnum.BTC);
        Long longValue = baseCurrency.doubleToLongCurrency(0.0001);
        assertTrue(longValue == 10000);
    }

    @Test
    public void longToDoubleCurrency() throws Exception {
        BaseCurrency baseCurrency = NativeWallet.getInstance().initCurrency(CurrencyEnum.BTC);
        String value = baseCurrency.longToDoubleCurrency(10000);
        assertTrue(Double.valueOf(value) == 0.0001);
    }

    @Test
    public void isHaveDustWithAmount() throws Exception {
        assertTrue(BtcCurrency.isHaveDustWithAmount(100));
    }

    @Test
    public void getAutoFeeWithUnspentLength() throws Exception {
        long value = BtcCurrency.getAutoFeeWithUnspentLength(true, 1, 1);
        assertTrue(value > 0);
    }

}
