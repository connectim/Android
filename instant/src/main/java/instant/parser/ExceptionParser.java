package instant.parser;

import java.nio.ByteBuffer;

import instant.parser.localreceiver.ExceptionLocalReceiver;
import protos.Connect;

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
                Connect.StructData structData = imTransferToStructData(byteBuffer);
                Connect.QuitMessage quitMessage = Connect.QuitMessage.parseFrom(structData.getPlainData());
                remoteLogin(quitMessage.getDeviceName());
                break;
            case 0x01:
                crowdedOffline();
                break;
        }
    }

    private void remoteLogin(String devicename){
        ExceptionLocalReceiver.localReceiver.remoteLogin(devicename);
    }

    /**
     * Be offline
     */
   private void crowdedOffline() {
       ExceptionLocalReceiver.localReceiver.exitAccount();
   }
}
