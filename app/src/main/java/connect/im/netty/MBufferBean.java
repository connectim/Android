package connect.im.netty;

/**
 * Created by Administrator on 2017/6/21.
 */

public class MBufferBean {
    private byte[] ack;
    private byte[] message;

    public byte[] getAck() {
        return ack;
    }

    public void setAck(byte[] ack) {
        this.ack = ack;
    }

    public byte[] getMessage() {
        return message;
    }

    public void setMessage(byte[] message) {
        this.message = message;
    }
}
