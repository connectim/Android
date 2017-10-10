import android.text.TextUtils;

import com.wallet.BaseWallet;
import com.wallet.bean.EncryptionPinBean;
import com.wallet.currency.BaseCurrency;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Created by Administrator on 2017/9/22 0022.
 */

public class BaseWalletTest {

    private String Tag = "_BaseWalletTest";
    String priKey = "KysBqxeJpYFyo3Ubf1uFkNxeb4SJ8tgCa8okix66phy47ueGvCj5";
    String salt = "7b1f2cdec6580c5faeb06a13037b16befa906b3464c1b5d6b9df210aed1b8b8dbaaaf2efa7b8bbcf56e2bbb83b72e2168d3a9de31de2f0d828e4fd41672287eb";
    String baseSeed = "6aeff57bf6aba1485faeedd267f9163e31a2bdf3c733b542f67bc60c5d2581d657f8b6063d4306c17a5d6fa2babaf4d7e674bd73ecd9da2edefb55fe5a4b967c";
    String currencySeed = "11f0d9a530f3ad17f11e87c164820080cb32d6c7a3f200944fa4e706b03e0a5bed5244e99afbbd0e2cbfd41a81c816c16b4e2090f13b2af6f61fa8bf3d691197";
    String address = "1CbvxESdeXkcjQQUfhf79LnnUvMV688WSz";

    @Test
    public void getWordsFromVale(){
        BaseWallet baseWallet = new BaseWallet();
        // bip39 native方法不能通过
        String wordsPriKey = baseWallet.getWordsFromVale(priKey);
        String wordsSeed = baseWallet.getWordsFromVale(currencySeed);
        if(!TextUtils.isEmpty(wordsPriKey) && !TextUtils.isEmpty(wordsSeed)){
            assertTrue(true);
        }else{
            assertTrue(false);
        }
    }

    @Test
    public void encryptionPin(){
        BaseWallet baseWallet = new BaseWallet();
        EncryptionPinBean encryptionPinBean = baseWallet.encryptionPinDefault(BaseCurrency.CATEGORY_BASESEED ,baseSeed, "1234");
        String value = baseWallet.decryptionPinDefault(BaseCurrency.CATEGORY_BASESEED ,encryptionPinBean.getPayload(), "1234");
        if(baseSeed.equals(value)){
            assertTrue(true);
        }else{
            assertTrue(false);
        }
    }

}
