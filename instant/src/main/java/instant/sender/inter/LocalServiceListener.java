package instant.sender.inter;

import java.nio.ByteBuffer;

/**
 * App 部跟LocalService之间的通信
 * Created by Administrator on 2017/9/22.
 */

public interface LocalServiceListener {

    void messageSend(byte[] ack, ByteBuffer byteBuffer);

    void exitAccount();
}
