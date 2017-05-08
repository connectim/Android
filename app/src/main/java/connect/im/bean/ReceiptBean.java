package connect.im.bean;

import java.nio.ByteBuffer;

import connect.im.inter.InterParse;
import protos.Connect;

/**
 * Created by pujin on 2017/4/18.
 */
public class ReceiptBean extends InterParse{

    public ReceiptBean(byte ackByte, ByteBuffer byteBuffer) {
        super(ackByte, byteBuffer);
    }

    @Override
    public void msgParse() throws Exception {
        messageReceipt(byteBuffer);
    }

    private void messageReceipt(ByteBuffer buffer) throws Exception {
        Connect.StructData structData = imTransferToStructData(buffer);
        Connect.Ack ack = Connect.Ack.parseFrom(structData.getPlainData());

        receiptMsg(ack.getMsgId(), 1);
    }
}
