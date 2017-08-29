package connect.utils;

import org.junit.Test;

import connect.utils.log.LogManager;
import protos.Connect;
import wallet_gateway.WalletOuterClass;

/**
 * Created by Administrator on 2017/7/10.
 */
public class ProtoBufUtilTest {

    private String Tag = "_ProtoBufUtilTest";

    @Test
    public void checkProtoBufTest() throws Exception {
        WalletOuterClass.OriginalTransactionResponse response = WalletOuterClass.OriginalTransactionResponse.newBuilder()
                .setCode(120)
                .setMessage("123456").build();

        if (ProtoBufUtil.getInstance().checkProtoBuf(response)) {
            LogManager.getLogger().d(Tag, "checkProtoBufTest: true");
        }
    }

    @Test
    public void timeLengthTest() {
        Connect.DestructMessage destructMessage = Connect.DestructMessage.newBuilder()
                .setTime(0).build();
        int length = destructMessage.toByteArray().length;
        LogManager.getLogger().d(Tag, "checkProtoBufTest: " + length);
    }
}