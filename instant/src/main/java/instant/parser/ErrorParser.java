package instant.parser;

import java.nio.ByteBuffer;

import instant.parser.localreceiver.ExceptionLocalReceiver;

/**
 * Created by Administrator on 2017/11/8.
 */
public class ErrorParser extends InterParse {

    public ErrorParser(byte ackByte, ByteBuffer byteBuffer) {
        super(ackByte, byteBuffer);
    }

    @Override
    public void msgParse() throws Exception {
        switch (ackByte) {
            case 0x00:
                ExceptionLocalReceiver.localReceiver.remoteLogin("");
                break;
        }
    }
}
