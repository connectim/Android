package connect.utils;

import org.junit.Test;

import protos.Connect;

import static org.junit.Assert.*;

/**
 * Created by Administrator on 2017/7/10.
 */
public class ProtoBufUtilTest {

    @Test
    public void checkProtoBufTest() throws Exception {
        Connect.VersionResponse response=Connect.VersionResponse.newBuilder()
                .setVersion("1")
                .setUpgradeUrl("123").build();

        assertTrue(ProtoBufUtil.getInstance().checkProtoBuf(response));
    }
}