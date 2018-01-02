package instant.parser;

import java.nio.ByteBuffer;

import instant.parser.localreceiver.ExceptionLocalReceiver;

/**
 * Created by pujin on 2017/4/18.
 */

public class ExceptionParser extends InterParse {

    public ExceptionParser(byte ackByte, ByteBuffer byteBuffer) {
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
       ExceptionLocalReceiver.localReceiver.exitAccount();
   }
}
