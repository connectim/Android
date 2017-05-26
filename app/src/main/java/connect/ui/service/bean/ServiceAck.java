package connect.ui.service.bean;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by pujin on 2017/5/26.
 */

public enum ServiceAck {
    HAND_SHAKE(1),
    CONNECT_START(2),
    CONNECT_SUCCESS(3),
    MESSAGE(4),
    HEART_BEAT(5),
    EXIT_ACCOUNT(6),
    STOP_CONNECT(7),
    CONNCET_REFRESH(8);

    int ack;
    ServiceAck(int ack){
        this.ack=ack;
    }

    public int getAck() {
        return ack;
    }

    private static Map<Integer,ServiceAck> serviceAckMap=new HashMap<>();

    static {
        ServiceAck[] serviceAcks = ServiceAck.values();
        for (ServiceAck serviceAck : serviceAcks) {
            serviceAckMap.put(serviceAck.ack, serviceAck);
        }
    }

    public static ServiceAck valueOf(int ack){
        return serviceAckMap.get(ack);
    }
}
