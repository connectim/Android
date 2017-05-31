package connect.ui.service.bean;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by pujin on 2017/5/26.
 */

public enum ServiceAck {
    BIND_SUCCESS(1),
    HAND_SHAKE(2),
    CONNECT_START(3),
    CONNECT_SUCCESS(4),
    MESSAGE(5),
    HEART_BEAT(6),
    EXIT_ACCOUNT(7),
    STOP_CONNECT(8),
    CONNCET_REFRESH(9);

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
