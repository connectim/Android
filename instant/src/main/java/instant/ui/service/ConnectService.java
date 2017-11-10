package instant.ui.service;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;

import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import connect.im.IMessage;
import instant.bean.Session;
import instant.parser.CommandParser;
import instant.parser.ErrorParser;
import instant.parser.ExceptionParser;
import instant.parser.InterParse;
import instant.parser.MessageParser;
import instant.parser.ReceiptParser;
import instant.parser.ShakeHandParser;
import instant.sender.HeartBeatSender;
import instant.sender.ShakeHandSender;
import instant.utils.SharedUtil;
import instant.utils.TimeUtil;
import instant.utils.log.LogManager;

/**
 * Created by puin on 17-10-8.
 */
public class ConnectService extends Service {

    private String Tag = "_ConnectService";

    private ConnectService service;
    protected IMessage pushBinder;
    protected LocalBinder localBinder;
    protected LocalConnect localConnect;

    @Override
    public void onCreate() {
        super.onCreate();
        service = this;
        if (localBinder == null) {
            localBinder = new LocalBinder();
        }
        if (localConnect == null) {
            localConnect = new LocalConnect();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return localBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogManager.getLogger().d(Tag, "onStartCommand");
        intent = new Intent(this, RemoteServeice.class);
        bindService(intent, localConnect, Service.BIND_IMPORTANT);

        try {
            if (pushBinder != null) {
                LogManager.getLogger().d(Tag, "pushBinder != null");
                pushBinder.serviceBind();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    class LocalConnect implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LogManager.getLogger().d(Tag, "onServiceConnected");

            try {
                pushBinder = IMessage.Stub.asInterface(service);
                pushBinder.serviceBind();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            LogManager.getLogger().d(Tag, "onServiceDisconnected");
            if (localConnect != null) {
                Intent intent = new Intent(service, RemoteServeice.class);
                service.startService(intent);
                service.bindService(intent, localConnect, Service.BIND_IMPORTANT);
            }
        }
    }

    private class LocalBinder extends IMessage.Stub {

        ShakeHandSender shakeHand = null;
        HeartBeatSender heartBeat = null;

        @Override
        public void serviceBind() throws RemoteException {

        }

        @Override
        public void connectStart() throws RemoteException {
            LogManager.getLogger().d(Tag, "connectStart: " + TimeUtil.getCurrentTimeInString(TimeUtil.DATE_FORMAT_SECOND));

            if (shakeHand == null) {
                shakeHand = new ShakeHandSender();
            }
            shakeHand.firstLoginShake();
        }

        @Override
        public void connectMessage(byte[] ack, byte[] message) throws RemoteException {
            LogManager.getLogger().d(Tag, "connectMessage ACK :" + ack[0] + ack[1]);

            receiveMessage(ack, message);
        }

        @Override
        public void heartBeat() throws RemoteException {
            if (heartBeat == null) {
                heartBeat = new HeartBeatSender();
            }
            heartBeat.heartBeat();
        }

        @Override
        public void connectStop() throws RemoteException {

        }

        @Override
        public void connectExit() throws RemoteException {
            LogManager.getLogger().d(Tag, "connectExit");

            Session.getInstance().clearUserCookie();
            SharedUtil.getInstance().closeShare();
            if (localBinder != null) {
                unbindService(localConnect);
                localConnect = null;
                stopSelf();
            }
        }
    }

    private static ExecutorService threadPoolExecutor = Executors.newCachedThreadPool();

    public void receiveMessage(byte[] ack, byte[] bytes) {
        ByteBuffer ackBuffer = ByteBuffer.wrap(ack);
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);

        ReceiveRun receiveRun = new ReceiveRun(ackBuffer, byteBuffer);
        threadPoolExecutor.submit(receiveRun);
    }

    private class ReceiveRun implements Runnable {

        private ByteBuffer ack;
        private ByteBuffer body;

        public ReceiveRun(ByteBuffer ack, ByteBuffer body) {
            this.ack = ack;
            this.body = body;
        }

        @Override
        public synchronized void run() {
            try {
                byte type = ack.get(0);
                byte ext = ack.get(1);

                LogManager.getLogger().i(Tag, "receive order: [" + type + "][" + ext + "]");
                InterParse interParse = null;

                switch (type) {
                    case 0x01://shake hand order
                        interParse = new ShakeHandParser(ext, body);
                        break;
                    case 0x03://receive message id
                        interParse = new ReceiptParser(ext, body);
                        break;
                    case 0x04://command order
                        interParse = new CommandParser(ext, body);
                        break;
                    case 0x05://chat order
                        interParse = new MessageParser(ext, body);
                        break;
                    case 0x06://Be offline
                        interParse = new ExceptionParser(ext, body);
                        break;
                    case 0x08://error
                        interParse = new ErrorParser(ext, body);
                        break;
                }

                if (interParse != null) {
                    interParse.msgParse();
                }
            } catch (Exception e) {
                e.printStackTrace();
                String errInfo = e.getMessage();
                if (TextUtils.isEmpty(errInfo)) {
                    errInfo = "";
                }
                LogManager.getLogger().d(Tag, "exception order info: [" + ack.get(0) + "][" + ack.get(1) + "]" + errInfo);
            }
        }
    }
}
