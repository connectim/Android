package connect.utils;

import android.text.TextUtils;

import com.google.protobuf.ByteString;

import org.junit.Test;

import java.security.SecureRandom;

import connect.utils.cryption.DecryptionUtil;
import connect.utils.cryption.EncryptionUtil;
import connect.utils.cryption.SupportKeyUril;
import connect.utils.log.LogManager;
import connect.wallet.jni.AllNativeMethod;
import protos.Connect;

import static org.junit.Assert.assertTrue;

/**
 * Created by Administrator on 2017/8/2 0002.
 */

public class SupportKeyUrilTest {

    private String Tag = "_SupportKeyUrilTest";

    @Test
    public void hmacSHA512() throws Exception {
        String hmac = SupportKeyUril.hmacSHA512("12345678910",SupportKeyUril.SaltHMAC);
        assertTrue(!TextUtils.isEmpty(hmac));
    }

    @Test
    public void createTalkKey() throws Exception {
        String priKey = "L12LREW9xUHDADSi37RckeML7wVX17FWLaRmtfWgdRVHs3SM8cEY";
        String address = "18ZWCA1Ujv6GL3NQe5GXZX4era5BugKyuZ";
        String pass = "123456";
        String takeStr = SupportKeyUril.createTalkKey(priKey, address, pass);
        String value = SupportKeyUril.decodeTalkKey(takeStr, pass);
        if(!TextUtils.isEmpty(value) && priKey.equals(value)){
            assertTrue(true);
        }else {
            assertTrue(false);
        }
    }

    @Test
    public void encodePri() throws Exception {
        String priKey = "L12LREW9xUHDADSi37RckeML7wVX17FWLaRmtfWgdRVHs3SM8cEY";
        String salt = StringUtil.bytesToHexString(SecureRandom.getSeed(64));
        String pass = "123456";
        String encryStr = SupportKeyUril.decryptionPri(priKey,salt,pass);
        String value = SupportKeyUril.decryptionPri(encryStr,salt,pass);
        if(value.equals(priKey)){
            assertTrue(true);
        }else{
            assertTrue(false);
        }
    }

    @Test
    public void encodeAESGCMStructData() throws Exception {
        String value = "1234";
        String priKey = "L12LREW9xUHDADSi37RckeML7wVX17FWLaRmtfWgdRVHs3SM8cEY";
        Connect.GcmData gcmData = EncryptionUtil.encodeAESGCMStructData(EncryptionUtil.ExtendedECDH.EMPTY, priKey, ByteString.copyFrom(value.getBytes()));
        Connect.StructData structData = DecryptionUtil.decodeAESGCMStructData(EncryptionUtil.ExtendedECDH.EMPTY, priKey, gcmData);
        String valueDe = new String(structData.getPlainData().toByteArray());
        if(value.equals(valueDe)){
            assertTrue(true);
        }else{
            assertTrue(false);
        }
    }

    @Test
    public void getRawECDHKeyTest() throws Exception {
        String priKey = "KzLR7jeCtWBkU8GvwwRALjo83kM7xMNwbwg9UjzEDdJPZt1H5rfN";
        String pubKey = "030f6cacf80bc14afaaf91381a059a3f2d06ec0143ae3135746354a7e9a546aee3";
        byte[] ecdhBytes = AllNativeMethod.cdxtalkgetRawECDHkey(priKey, pubKey);

        StringBuffer buffer=new StringBuffer();
        for (int i = 0; i < StringUtil.hexStringToBytes(pubKey).length; i++) {
            buffer.append(StringUtil.hexStringToBytes(pubKey)[i]);
        }
        LogManager.getLogger().d(Tag, buffer.toString());

        // 8f0c0b8d81815a8da8e1f66bbe20878621f5769377acbb785cbbf19ada96451e
        // 48e0ba26484069cf748ec8ae6a4c77edc0c10dd5754041c7746380cdfd991ddb

        LogManager.getLogger().d(Tag, "ECDH:" + StringUtil.bytesToHexString(ecdhBytes));
    }
}
