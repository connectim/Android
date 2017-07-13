package connect.utils;

import org.junit.Test;

import java.security.SecureRandom;

import connect.utils.log.LogManager;
import connect.wallet.jni.AllNativeMethod;

/**
 * Created by Administrator on 2017/7/13.
 */

public class JniNativeTest {

    private String Tag = "_JniNativeTest";

    @Test
    public void walletEncryptTest() {
        String seed = AllNativeMethod.cdGetHash256(StringUtil.bytesToHexString(SecureRandom.getSeed(64)));
        LogManager.getLogger().d(Tag, seed);
        String aa = AllNativeMethod.connectWalletKeyEncrypt(seed, "bit1234", 17, 2);
        String bb = AllNativeMethod.connectWalletKeyDecrypt(aa, "bit1234", 2);
        LogManager.getLogger().d(Tag, bb);
    }
}
