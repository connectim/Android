package connect.im.bean;

import java.nio.ByteBuffer;

import connect.im.inter.InterParse;
import connect.ui.activity.home.bean.HomeAction;

/**
 * Created by pujin on 2017/4/18.
 */

public class ExceptionBean extends InterParse{

    public ExceptionBean(byte ackByte, ByteBuffer byteBuffer) {
        super(ackByte, byteBuffer);
    }

    @Override
    public void msgParse() throws Exception {
        switch (ackByte) {
            case 0x00:
                crowdedOffline();
                break;
        }
    }

    /**
     * Be offline
     */
   private void crowdedOffline() {
        HomeAction.sendTypeMsg(HomeAction.HomeType.EXIT);
    }
}
