package connect.ui.base.bean;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.io.Serializable;

/**
 * BaseEvent delayed send eventbus message
 * Created by pujin on 2017/3/2.
 */

public abstract class BaseEvent {

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 120:
                    Bundle bundle = msg.getData();
                    Serializable type = bundle.getSerializable("type");
                    Serializable obj = bundle.getSerializable("obj");
                    sendEvent(type, obj);
                    break;
            }
        }
    };

    public abstract void sendEvent(Serializable type, Serializable... objects);

    public void sendEventDelay(Serializable type, Serializable... objects) {
        sendEventDelay(type, 500, objects);
    }

    public void sendEventDelay(Serializable type, long delaytime, Serializable... objects) {

        Message message = new Message();
        message.what = 120;
        Bundle bundle = new Bundle();
        bundle.putSerializable("type", type);
        bundle.putSerializable("obj", objects);
        message.setData(bundle);

        handler.sendMessageDelayed(message, delaytime);
    }
}
