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

    @Test
    public void ecdhTest() {

    }

    @Test
    public void cdGetPubKeyFromPrivKeyTest() {
        String privateKey = "KzLR7jeCtWBkU8GvwwRALjo83kM7xMNwbwg9UjzEDdJPZt1H5rfN";
        String publicKey = AllNativeMethod.cdGetPubKeyFromPrivKey(privateKey);
        LogManager.getLogger().d(Tag, "publicKey: " + publicKey + "   - length: " + publicKey.length());
    }
}
