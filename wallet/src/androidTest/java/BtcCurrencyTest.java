import android.text.TextUtils;

import com.wallet.bean.CurrencyBean;
import com.wallet.currency.BaseCurrency;
import com.wallet.currency.BtcCurrency;
import com.wallet.utils.WalletUtil;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import connect.wallet.jni.AllNativeMethod;

import static org.junit.Assert.assertTrue;

public class BtcCurrencyTest {

    private String Tag = "_BtcCurrencyTest";
    String priKey = "KysBqxeJpYFyo3Ubf1uFkNxeb4SJ8tgCa8okix66phy47ueGvCj5";
    String salt = "7b1f2cdec6580c5faeb06a13037b16befa906b3464c1b5d6b9df210aed1b8b8dbaaaf2efa7b8bbcf56e2bbb83b72e2168d3a9de31de2f0d828e4fd41672287eb";
    String baseSeed = "6aeff57bf6aba1485faeedd267f9163e31a2bdf3c733b542f67bc60c5d2581d657f8b6063d4306c17a5d6fa2babaf4d7e674bd73ecd9da2edefb55fe5a4b967c";
    String currencySeed = "11f0d9a530f3ad17f11e87c164820080cb32d6c7a3f200944fa4e706b03e0a5bed5244e99afbbd0e2cbfd41a81c816c16b4e2090f13b2af6f61fa8bf3d691197";
    String address = "1CbvxESdeXkcjQQUfhf79LnnUvMV688WSz";

    @Test
    public void createCurrency(){
        BtcCurrency btcCurrency = new BtcCurrency();
        boolean isSuccess = true;
        // 纯私钥创建货币
        CurrencyBean currencyBeanPri = btcCurrency.createCurrency(BaseCurrency.CATEGORY_PRIKEY, priKey);
        if(!currencyBeanPri.getMasterAddress().equals(address)){
            isSuccess = false;
        }
        // 货币种子创建货币
        AllNativeMethod.cdGetPrivKeyFromSeedBIP44(currencySeed,44,0,0,0,0);
        CurrencyBean currencyBeanSeed = btcCurrency.createCurrency(BaseCurrency.CATEGORY_CURRENCY, currencySeed);
        if(!currencyBeanSeed.getMasterAddress().equals(address)){
            isSuccess = false;
        }
        // 原始种子创建货币
        CurrencyBean currencyBeanBase = btcCurrency.createCurrency(BaseCurrency.CATEGORY_BASESEED, baseSeed);
        if(TextUtils.isEmpty(currencyBeanBase.getMasterAddress())
                || !currencyBeanBase.getCurrencySeed().equals(WalletUtil.xor(currencyBeanBase.getBaseSeed(), currencyBeanBase.getSalt()))){
            isSuccess = false;
        }
        assertTrue(isSuccess);
    }


    @Test
    public void addCurrencyAddress(){
        BtcCurrency btcCurrency = new BtcCurrency();
        CurrencyBean currencyBean = btcCurrency.addCurrencyAddress(baseSeed, salt, 0);
        if(address.equals(currencyBean.getMasterAddress())){
            assertTrue(true);
        }else{
            assertTrue(false);
        }
    }
    //getAddressPriKey
    @Test
    public void getAddressPriKey(){
        BtcCurrency btcCurrency = new BtcCurrency();
        ArrayList<Integer> indexList = new ArrayList<>();
        indexList.add(0);
        List<String> listAddress = btcCurrency.getPriKeyFromAddressIndex(baseSeed, salt, indexList);
        if(priKey.equals(listAddress.get(0))){
            assertTrue(true);
        }else{
            assertTrue(false);
        }
    }


}
