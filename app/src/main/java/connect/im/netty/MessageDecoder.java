package connect.im.netty;

import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import connect.im.bean.Session;
import connect.utils.StringUtil;
import connect.utils.cryption.DecryptionUtil;
import connect.utils.cryption.EncryptionUtil;
import connect.utils.cryption.SupportKeyUril;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import protos.Connect;

/**
 * Created by Administrator on 2017/6/21.
 */

public class MessageDecoder extends ByteToMessageDecoder{

    /**
     * Message length
     */
    private static final int MSG_BODY_LENGTH = 4;
    /**
     * Message header length
     */
    private static final int MSG_HEADER_LENGTH = 13;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        //message header
        if (in.readableBytes() < MSG_HEADER_LENGTH) {
            return;
        }
        //big data
        if (in.readableBytes() > 40960) {
            return;
        }

        in.markReaderIndex();

        byte[] ackArr = new byte[2];
        in.readByte();//version
        ackArr[0] = in.readByte();//ack[0]
        int length = in.readInt();
        ackArr[1] = in.readByte();//ack[1]
        byte[] randoms = new byte[MSG_BODY_LENGTH];//random: 4 bit
        in.readBytes(randoms);
        byte[] ext = new byte[2];//ext: 2 bit
        in.readBytes(ext);

        if (in.readableBytes() < length) {
            in.resetReaderIndex();
            return;
        }
        if (assembleHeader(ackArr[0], length, ackArr[1], randoms, ext)) {
            byte[] msgdata = new byte[length];//message
            in.readBytes(msgdata);

            /*if (ackArr[0] == (byte) 5 && ackArr[1] == (byte) 1) {
                Connect.StructData imTransferToStructData = imTransferToStructData(ByteBuffer.wrap(msgdata));
                Connect.MessagePost messagePost = Connect.MessagePost.parseFrom(imTransferToStructData.getPlainData());
                Connect.MessageData messageData = messagePost.getMsgData();
                String id = messageData.getMsgId();
            }*/

            BufferBean bufferBean = new BufferBean();
            bufferBean.setAck(ackArr);
            bufferBean.setMessage(msgdata);
            out.add(bufferBean);
        }
    }

    /**
     * IMTransferData To StructData
     *
     * @param buffer
     * @return
     * @throws Exception
     */
    protected synchronized Connect.StructData imTransferToStructData(ByteBuffer buffer) throws Exception {
        Connect.IMTransferData imTransferData = Connect.IMTransferData.parseFrom(buffer.array());
        if (!SupportKeyUril.verifySign(imTransferData.getSign(), imTransferData.getCipherData().toByteArray())) {
            throw new Exception("Validation fails");
        }

        byte[] bytes = DecryptionUtil.decodeAESGCM(EncryptionUtil.ExtendedECDH.NONE,
                Session.getInstance().getUserCookie("TEMPCOOKIE").getSalt(), imTransferData.getCipherData());
        return Connect.StructData.parseFrom(bytes);
    }


    public boolean assembleHeader(byte ack1, int length, byte ack2, byte[] randoms, byte[] ext) {
        ByteBuffer header = ByteBuffer.allocate(MSG_HEADER_LENGTH);
        header.put(ack1);
        header.put(ByteBuffer.allocate(MSG_BODY_LENGTH).putInt(length).array());
        header.put(ack2);
        header.put(randoms);
        header.put(StringUtil.MSG_HEADER_EXI);

        try {
            byte[] nExt = StringUtil.byteTomd5(header.array());
            if (ext[0] == nExt[0] && ext[1] == nExt[1]) {
                return true;
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }
}
