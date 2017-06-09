package connect.ui.service;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

import java.nio.ByteBuffer;

import connect.im.IMessage;
import connect.im.model.ConnectManager;
import connect.ui.service.bean.ServiceAck;

public class PushService extends Service {

    private PushService service;
    private IMessage localBinder;
    private PushBinder pushBinder;
    private PushConnect pushConnect;
    private ConnectManager connectManager;

    @Override
    public IBinder onBind(Intent intent) {
        return pushBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        service=this;
        connectManager = ConnectManager.getInstance();
        if (pushBinder == null) {
            pushBinder = new PushBinder();
        }
        if (pushConnect == null) {
            pushConnect = new PushConnect();
        }
    }

    public static void startService(Context context) {
        Intent intent = new Intent(context, PushService.class);
        context.startService(intent);
    }

    class PushConnect implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            localBinder = IMessage.Stub.asInterface(service);

            connectManager.setiMessage(localBinder);
            connectManager.connectServer();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            if (pushConnect != null) {
                Intent intent = new Intent(service, SocketService.class);
                service.startService(intent);
                service.bindService(intent, pushConnect, Service.BIND_IMPORTANT);
            }
        }
    }

    class PushBinder extends IMessage.Stub {

        @Override
        public void connectMessage(int type, byte[] message) throws RemoteException {
            ServiceAck serviceAck=ServiceAck.valueOf(type);

            switch (serviceAck) {
                case BIND_SUCCESS:
                    Intent intent = new Intent(service, SocketService.class);
                    bindService(intent, pushConnect, Service.BIND_IMPORTANT);
                    break;
                case MESSAGE:
                    ByteBuffer byteBuffer = ByteBuffer.wrap(message);
                    connectManager.sendToBytes(byteBuffer);
                    break;
                case CONNECT_START:
                    connectManager.connectServer();
                    break;
                case CONNECT_SUCCESS:
                    connectManager.connectSuccess();
                    break;
                case EXIT_ACCOUNT:
                    connectManager.exitConnect();
                    break;
                case STOP_CONNECT:
                    connectManager.stopConnect();
                    break;
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbindService(pushConnect);
    }
}
