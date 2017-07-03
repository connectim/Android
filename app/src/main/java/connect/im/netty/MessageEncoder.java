package connect.im.netty;

import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import connect.utils.StringUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * Created by Administrator on 2017/6/21.
 */

public class MessageEncoder extends MessageToByteEncoder<BufferBean>{

    /** version number */
    private static final byte MSG_VERSION = 0x01;
    /** Message length */
    private static final int MSG_BODY_LENGTH = 4;
    /** Message header length */
    private static final int MSG_HEADER_LENGTH = 13;

    @Override
    protected void encode(ChannelHandlerContext ctx, BufferBean msg, ByteBuf out) throws Exception {
        byte[] ackarr=msg.getAck();
        byte[] msgdata=msg.getMessage();
        byte[] randomBytes = SecureRandom.getSeed(4);

        int length = msgdata.length;

        ByteBuffer header = ByteBuffer.allocate(MSG_HEADER_LENGTH);
        header.put(ackarr[0]);
        byte[] lengthArr = ByteBuffer.allocate(MSG_BODY_LENGTH).putInt(length).array();
        header.put(lengthArr);
        header.put(ackarr[1]);
        header.put(randomBytes);
        header.put(StringUtil.MSG_HEADER_EXI);

        try {
            byte[] ext = StringUtil.byteTomd5(header.array());
            byte[] message = new byte[MSG_HEADER_LENGTH + length];

            ByteBuffer buffer = ByteBuffer.wrap(message);
            buffer.put(MSG_VERSION);
            byte[] msgHeader = new byte[10];
            header.position(0);
            header.get(msgHeader);
            buffer.put(msgHeader);
            buffer.put(ext[0]);
            buffer.put(ext[1]);
            buffer.put(msgdata);
            buffer.flip();

            out.writeBytes(buffer);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
}
