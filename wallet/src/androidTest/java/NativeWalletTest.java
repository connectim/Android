import com.wallet.NativeWallet;
import com.wallet.bean.CurrencyBean;
import com.wallet.bean.CurrencyEnum;
import com.wallet.currency.BaseCurrency;
import com.wallet.inter.WalletListener;
import com.wallet.utils.WalletUtil;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class NativeWalletTest {

    private String Tag = "_NativeWalletTest";
    String priKey = "KysBqxeJpYFyo3Ubf1uFkNxeb4SJ8tgCa8okix66phy47ueGvCj5";
    String salt = "7b1f2cdec6580c5faeb06a13037b16befa906b3464c1b5d6b9df210aed1b8b8dbaaaf2efa7b8bbcf56e2bbb83b72e2168d3a9de31de2f0d828e4fd41672287eb";
    String baseSeed = "6aeff57bf6aba1485faeedd267f9163e31a2bdf3c733b542f67bc60c5d2581d657f8b6063d4306c17a5d6fa2babaf4d7e674bd73ecd9da2edefb55fe5a4b967c";
    String currencySeed = "11f0d9a530f3ad17f11e87c164820080cb32d6c7a3f200944fa4e706b03e0a5bed5244e99afbbd0e2cbfd41a81c816c16b4e2090f13b2af6f61fa8bf3d691197";
    String address = "1CbvxESdeXkcjQQUfhf79LnnUvMV688WSz";

    @Test
    public void createCurrencyBaseSeed(){
        NativeWallet.getInstance().createCurrency(CurrencyEnum.BTC, BaseCurrency.CATEGORY_BASESEED, baseSeed, new WalletListener<CurrencyBean>() {
            @Override
            public void success(CurrencyBean currencyBean) {
                if(currencyBean.getCurrencySeed().equals(WalletUtil.xor(currencyBean.getBaseSeed(), currencyBean.getSalt()))){
                    assertTrue(true);
                }else{
                    assertTrue(false);
                }
            }

            @Override
            public void fail(WalletError error) {
                assertTrue(false);
            }
        });
    }

    @Test
    public void createCurrencySeed(){
        NativeWallet.getInstance().createCurrency(CurrencyEnum.BTC, BaseCurrency.CATEGORY_CURRENCY, currencySeed, new WalletListener<CurrencyBean>() {
            @Override
            public void success(CurrencyBean currencyBean) {
                if(address.equals(currencyBean.getMasterAddress())){
                    assertTrue(true);
                }else{
                    assertTrue(false);
                }
            }

            @Override
            public void fail(WalletError error) {
                assertTrue(false);
            }
        });
    }

    @Test
    public void createCurrencyPriKey(){
        NativeWallet.getInstance().createCurrency(CurrencyEnum.BTC, BaseCurrency.CATEGORY_PRIKEY, priKey, new WalletListener<CurrencyBean>() {
            @Override
            public void success(CurrencyBean currencyBean) {
                if(address.equals(currencyBean.getMasterAddress())){
                    assertTrue(true);
                }else{
                    assertTrue(false);
                }
            }

            @Override
            public void fail(WalletError error) {
                assertTrue(false);
            }
        });
    }
   // addCurrencyAddress
    @Test
    public void addCurrencyAddress(){
        NativeWallet.getInstance().addCurrencyAddress(CurrencyEnum.BTC, baseSeed, salt, 0, new WalletListener<CurrencyBean>() {
            @Override
            public void success(CurrencyBean currencyBean) {
                if(address.equals(currencyBean.getMasterAddress())){
                    assertTrue(true);
                }else{
                    assertTrue(false);
                }
            }

            @Override
            public void fail(WalletError error) {
                assertTrue(false);
            }
        });
    }

    @Test
    public void getAddressPriKey(){
        ArrayList<Integer> list = new ArrayList<>();
        list.add(0);
        NativeWallet.getInstance().getPriKeyFromAddressIndex(CurrencyEnum.BTC, baseSeed, salt, list, new WalletListener<List<String>>() {
            @Override
            public void success(List<String> listPri) {
                if(priKey.equals(listPri.get(0))){
                    assertTrue(true);
                }else{
                    assertTrue(false);
                }
            }

            @Override
            public void fail(WalletError error) {
                assertTrue(false);
            }
        });
    }



}
