package connect.im.netty;

/**
 * Created by Administrator on 2017/6/21.
 */

public class BufferBean {

    private byte[] ack;
    private byte[] message;

    public BufferBean() {
    }

    public BufferBean(byte[] ack, byte[] message) {
        this.ack = ack;
        this.message = message;
    }

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
