package connect.utils;

import android.text.TextUtils;

import org.junit.Test;

import java.security.SecureRandom;

import connect.utils.cryption.EncoPinBean;
import connect.utils.cryption.SupportKeyUril;
import connect.utils.log.LogManager;
import connect.wallet.jni.AllNativeMethod;

import static org.junit.Assert.assertTrue;

/**
 * Created by Administrator on 2017/7/13.
 */

public class JniNativeTest {

    private String Tag = "_JniNativeTest";

    @Test
    public void walletEncryptTest() {
//        String seed = AllNativeMethod.cdGetHash256(StringUtil.bytesToHexString(SecureRandom.getSeed(64)));
//        LogManager.getLogger().d(Tag, seed);
//        String aa = AllNativeMethod.connectWalletKeyEncrypt(seed, "bit1234", 17, 2);
//        String bb = AllNativeMethod.connectWalletKeyDecrypt(aa, "bit1234", 2);
//        LogManager.getLogger().d(Tag, bb);
        String prikey = "L58QekqQYviopx1uLxm6rj5Lpbn6i9meP8895rPcMZvrbjkYwsU1";
        byte[] prikeyBytes = prikey.getBytes();
        String prikeyHex = StringUtil.bytesToHexString(prikeyBytes);
        EncoPinBean pinBean = SupportKeyUril.encoPinDefult(1,prikeyHex, "1234");
        if(!TextUtils.isEmpty(pinBean.getPayload()) && pinBean.getPayload().contains("error")){
            pinBean.setPayload("");
        }
        if(TextUtils.isEmpty(pinBean.getPayload())){
            assertTrue(false);
        }else{
            assertTrue(true);
        }
    }

    @Test
    public void cdGetBIP39WordsFromSeedTest() {
        String seed = AllNativeMethod.cdGetHash256(StringUtil.bytesToHexString(SecureRandom.getSeed(64)));
        LogManager.getLogger().d(Tag, seed);
        String word = AllNativeMethod.cdGetBIP39WordsFromSeed(seed);
        LogManager.getLogger().d(Tag, "word:" + word);
    }

    @Test
    public void cdGetSeedFromBIP39WordsTest() {

    }
}
