package connect.im.model;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import connect.db.MemoryDataManager;
import connect.im.inter.InterParse;
import connect.im.parser.CommandBean;
import connect.im.parser.ExceptionBean;
import connect.im.parser.MsgParseBean;
import connect.im.parser.ReceiptBean;
import connect.im.parser.ShakeHandBean;
import connect.ui.activity.login.StartActivity;
import connect.ui.base.BaseApplication;
import connect.ui.service.bean.PushMessage;
import connect.ui.service.bean.ServiceAck;
import connect.utils.log.LogManager;
import connect.utils.system.SystemUtil;

/**
 * Created by gtq on 2016/11/30.
 */
public class MsgRecManager {

    private String Tag = "MsgRecManager";
    private static MsgRecManager receiverManager;

    public static MsgRecManager getInstance() {
        if (receiverManager == null) {
            synchronized (MsgRecManager.class) {
                if (receiverManager == null) {
                    receiverManager = new MsgRecManager();
                }
            }
        }
        return receiverManager;
    }

    private static final int coreSize = 3;
    private static final int maxSize = 6;
    private static final int aliveSize = 1;

    private static BlockingQueue<Runnable> linkedBlockingQueue = new LinkedBlockingQueue<>();
    private static ExecutorService threadPoolExecutor = new ThreadPoolExecutor(coreSize, maxSize, aliveSize, TimeUnit.DAYS, linkedBlockingQueue);

    public synchronized void sendMessage(ByteBuffer ack, ByteBuffer body) {
        ReceiveRun receiveRun = new ReceiveRun(ack, body);
        threadPoolExecutor.execute(receiveRun);
    }

    private synchronized void receiveMsgDeal(ByteBuffer ack, ByteBuffer body) throws Exception {
        byte type = ack.get(1);
        byte ext = ack.get(6);

        LogManager.getLogger().i(Tag, "receive order: [" + type + "][" + ext + "]");
        InterParse interParse = null;

        switch (type) {
            case 0x01:
                interParse = new ShakeHandBean(ext, body);
                break;
            case 0x03:
                interParse = new ReceiptBean(ext, body);
                break;
            case 0x04://command order
                interParse = new CommandBean(ext, body);
                break;
            case 0x05://chat order
                interParse = new MsgParseBean(ext, body, 1);
                break;
            case 0x06://Be offline
                interParse = new ExceptionBean(ext, body);
                break;
        }

        if (interParse != null) {
            interParse.msgParse();
        }
    }

    private class ReceiveRun implements Runnable {

        private ByteBuffer ack;
        private ByteBuffer body;

        public ReceiveRun(ByteBuffer ack, ByteBuffer body) {
            this.ack = ack;
            this.body = body;
        }

        @Override
        public void run() {
            if (!isKeyAvaliable()) {
                return;
            }

            try {
                receiveMsgDeal(ack, body);
            } catch (Exception e) {
                e.printStackTrace();
                String errInfo = e.getMessage();
                if (TextUtils.isEmpty(errInfo)) {
                    errInfo = "";
                }
                LogManager.getLogger().d(Tag, "exception order info: [" + ack.get(1) + "][" + ack.get(6) + "]" + errInfo);
            }
        }

        /** private key is available
         *
         * @return
         */
        public synchronized boolean isKeyAvaliable() {
            boolean isAvailable = MemoryDataManager.getInstance().isAvailableKey();
            if (!isAvailable) {
                PushMessage.pushMessage(ServiceAck.EXIT_ACCOUNT, ByteBuffer.allocate(0));//close socket
                if (SystemUtil.isRunBackGround()) {// run in front
                    Context context = BaseApplication.getInstance().getBaseContext();
                    Intent intent = new Intent(context, StartActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//keep a single instance
                    context.startActivity(intent);
                }
            }
            return isAvailable;
        }
    }
}