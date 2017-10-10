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
