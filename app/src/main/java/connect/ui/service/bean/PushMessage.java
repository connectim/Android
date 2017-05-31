package connect.ui.service.bean;

import org.greenrobot.eventbus.EventBus;

import java.io.Serializable;
import java.nio.ByteBuffer;

/**
 * Created by pujin on 2017/5/26.
 */

public class PushMessage implements Serializable{

    private ServiceAck serviceAck;
    private ByteBuffer byteBuffer;

    public PushMessage(ServiceAck serviceAck,ByteBuffer byteBuffer) {
        this.serviceAck=serviceAck;
        this.byteBuffer = byteBuffer;
    }

    public ServiceAck getServiceAck() {
        return serviceAck;
    }

    public ByteBuffer getByteBuffer() {
        return byteBuffer;
    }

    public static void pushMessage(ServiceAck serviceAck,ByteBuffer byteBuffer){
        EventBus.getDefault().post(new PushMessage(serviceAck,byteBuffer));
    }
}
