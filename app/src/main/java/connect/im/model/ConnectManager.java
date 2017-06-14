package connect.im.model;

import android.os.Message;
import android.os.RemoteException;
import android.text.TextUtils;

import org.apache.mina.transport.socket.nio.NioSocketConnector;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import connect.db.MemoryDataManager;
import connect.db.SharedPreferenceUtil;
import connect.im.IMessage;
import connect.ui.service.bean.ServiceAck;
import connect.utils.TimeUtil;
import connect.utils.log.LogManager;
import connect.utils.okhttp.HttpRequest;

/**
 * Created by gtq on 2016/11/21.
 */
public class ConnectManager {

    private String Tag = "ConnectManager";
    private boolean selectorRun = true;
    private Selector selector;
    private SocketChannel socketChannel;
    private IMessage iMessage;

    private static ConnectManager connectManager;

    public static ConnectManager getInstance() {
        if (connectManager == null) {
            synchronized (ConnectManager.class) {
                if (connectManager == null) {
                    connectManager = new ConnectManager();
                }
            }
        }
        return connectManager;
    }

    /**
     * connect service runnable
     */
    private class ConnectRunnable implements Runnable {

        private String host;
        private int port;

        public ConnectRunnable(String host, int port) {
            this.host = host;
            this.port = port;
            selectorRun = true;
        }

        @Override
        public void run() {
            try {
                socketChannel = SocketChannel.open();
                socketChannel.configureBlocking(false);
                selector = Selector.open();

                if (socketChannel.connect(new InetSocketAddress(host, port))) {
                    socketChannel.register(selector, SelectionKey.OP_READ);

                    iMessage.connectMessage(ServiceAck.HAND_SHAKE.getAck(),new byte[0]);
                } else {
                    socketChannel.register(selector, SelectionKey.OP_CONNECT);
                }

                while (selectorRun && selector != null) {
                    selector.select();
                    Set<SelectionKey> setKeys = selector.selectedKeys();
                    if (setKeys == null) continue;

                    Iterator<SelectionKey> selectionKeys = selector.selectedKeys().iterator();
                    SelectionKey selectionKey = null;
                    while (selectionKeys.hasNext()) {
                        selectionKey = selectionKeys.next();
                        selectionKeys.remove();

                        if (selectionKey.isConnectable()) {
                            if (socketChannel.finishConnect()) {
                                selectionKey.interestOps(SelectionKey.OP_READ);

                                iMessage.connectMessage(ServiceAck.HAND_SHAKE.getAck(),new byte[0]);
                            } else {//An error occurred; unregister the channel.
                                selectionKey.cancel();
                                reconDelay();
                            }
                        } else if (selectionKey.isReadable()) {
                            socketChannel = (SocketChannel) selectionKey.channel();
                            ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                            int byteLength = socketChannel.read(byteBuffer);
                            if (byteLength < 0) {//Channel has been disconnected
                                LogManager.getLogger().d(Tag, "channel disconnect");
                                socketChannel.close();
                                cancelTimer();
                                reconDelay();
                            } else if (byteLength == 0) {
                                LogManager.getLogger().d(Tag, "empty message");
                            } else {//Will receive the message in the queue
                                LogManager.getLogger().d(Tag, "receive new message");
                                lastReceiverTime = TimeUtil.getCurrentTimeInLong();

                                byteBuffer.flip();
                                byte[] byteArr = new byte[byteBuffer.limit()];
                                byteBuffer.get(byteArr);
                                iMessage.connectMessage(ServiceAck.MESSAGE.getAck(), byteArr);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                LogManager.getLogger().d(Tag, "error message :" + e.getMessage());
                reconDelay();
            } finally {
                stopConnect();
            }
        }
    }

    public synchronized boolean avaliableConnect() {
        if (!HttpRequest.isConnectNet()) {
            stopConnect();
        }
        return avaliableChannel();
    }

    /**
     * Detection channel status
     *
     * @return
     */
    public synchronized boolean avaliableChannel() {
        try {
            if (socketChannel == null || selector == null) return false;
            if (socketChannel.isConnected() && socketChannel.isOpen()
                    && selector.isOpen()) {
                LogManager.getLogger().d(Tag, "SOCKET connect normal");
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public void connectServer() {
        LogManager.getLogger().d(Tag, "connectServer conect service...");
        boolean reliNet = HttpRequest.isConnectNet();
        if (!reliNet) {
            return;
        }
        if (avaliableChannel()) {
            stopConnect();
        }

        String address = "sandbox.connect.im";
        int port = 19090;
        try {
            iMessage.connectMessage(ServiceAck.CONNCET_REFRESH.getAck(), new byte[0]);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        Thread connectThread = new Thread(new ConnectRunnable(address, port));
        connectThread.start();
    }

    public synchronized void sendToBytes(ByteBuffer byteBuffer) {
        WriteRunnable writeRunnable = new WriteRunnable(byteBuffer);
        threadPoolExecutor.execute(writeRunnable);
    }

    private static final int coreSize = 3;
    private static final int maxSize = 6;
    private static final int aliveSize = 1;

    private static BlockingQueue<Runnable> linkedBlockingQueue = new LinkedBlockingQueue<>();
    private static ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(coreSize, maxSize, aliveSize, TimeUnit.DAYS, linkedBlockingQueue);

    class WriteRunnable implements Runnable {

        ByteBuffer byteBuffer;
        WriteRunnable(ByteBuffer byteBuffer) {
            this.byteBuffer = byteBuffer;
        }

        @Override
        public void run() {
            boolean avaliableConnc = true;
            try {
                SocketChannel channel = getSocketChannel();
                avaliableConnc = avaliableConnect();
                if (avaliableConnc) {
                    while (byteBuffer.hasRemaining()) {
                        channel.write(byteBuffer);
                    }
                    LogManager.getLogger().d(Tag, "send message success");
                }
            } catch (IOException e) {
                e.printStackTrace();
                avaliableConnc = false;
            }

            if (!avaliableConnc) {
                reconDelay();
            }
        }
    }

    /**
     * The user logged out/account no longer detect abnormal initiate reconnection
     */
    public void exitConnect() {
        cancelTimer();
        stopConnect();
        reconHandler.removeMessages(TAG_CONNECT);
    }

    /**
     * Close the service
     */
    public void stopConnect() {
        LogManager.getLogger().d(Tag, "connectServer stopConnect()...close service");
        selectorRun = false;
        if (selector != null) {
            try {
                selector.close();
                selector = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (socketChannel != null) {
            try {
                if (socketChannel.socket() != null) {
                    socketChannel.socket().shutdownInput();
                    socketChannel.socket().shutdownOutput();
                }

                socketChannel.close();
                socketChannel = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * connect success
     */
    public void connectSuccess() {
        reconHandler.removeMessages(TAG_CONNECT);
        resetFibonacci();
        launHeartBit();
    }

    /** Heart rate */
    private final long HEART_FREQUENCY = 30 * 1000;
    /** Recently received a message of time */
    private long lastReceiverTime;
    /** The heartbeat polling timer */
    private Timer timer;
    private TimerTask timerTask;
    /** The message exchange Even the Fibonacci sequence */
    private long[] reconFibonacci = new long[]{1000, 1000};

    /**
     * After the connection is successful The reset sequence
     */
    public void resetFibonacci() {
        reconFibonacci[0] = 1000;
        reconFibonacci[1] = 1000;
    }

    private final int TAG_CONNECT = 100;

    private android.os.Handler reconHandler = new android.os.Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            connectServer();
        }
    };

    public synchronized void reconDelay() {
        LogManager.getLogger().d(Tag, "connectServer reconDelay()...");
        if (avaliableConnect()) return;
        if (!isCanConnect()) return;

        if (!reconHandler.hasMessages(TAG_CONNECT)) {
            long count = reconFibonacci[0] + reconFibonacci[1];
            reconFibonacci[0] = reconFibonacci[1];
            reconFibonacci[1] = count;

            LogManager.getLogger().d(Tag, "connectServer reconDelay()..." + count / 1000 + "s reconnect");
            Message msg = Message.obtain(reconHandler, TAG_CONNECT);
            reconHandler.sendMessageDelayed(msg, count);
        }
    }

    public void launHeartBit() {
        cancelTimer();

        timer = new Timer();
        timerTask = new TimerTask() {

            @Override
            public void run() {
                long curtime = TimeUtil.getCurrentTimeInLong();
                if (curtime < lastReceiverTime + HEART_FREQUENCY * 2) {
                    try {
                        iMessage.connectMessage(ServiceAck.HEART_BEAT.getAck(),new byte[0]);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                } else {
                    stopConnect();
                    reconDelay();
                }
            }
        };
        timer.schedule(timerTask, HEART_FREQUENCY, HEART_FREQUENCY);
    }

    public void cancelTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }

        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
    }

    public SocketChannel getSocketChannel() {
        return socketChannel;
    }

    public boolean isCanConnect() {
        return !TextUtils.isEmpty(MemoryDataManager.getInstance().getPriKey());
    }

    public void setiMessage(IMessage iMessage) {
        this.iMessage = iMessage;
    }
}
