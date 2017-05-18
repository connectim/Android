package connect.im.model;

import android.os.Message;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import connect.db.MemoryDataManager;
import connect.db.SharedPreferenceUtil;
import connect.im.bean.CommandBean;
import connect.im.bean.ConnectState;
import connect.im.bean.Session;
import connect.im.bean.SocketACK;
import connect.im.bean.UserCookie;
import connect.im.msgdeal.SendMsgUtil;
import connect.utils.ConfigUtil;
import connect.utils.TimeUtil;
import connect.utils.log.LogManager;
import connect.utils.okhttp.HttpRequest;
import protos.Connect;

/**
 * Created by gtq on 2016/11/21.
 */
public class ConnectManager {

    private String Tag = "ConnectManager";
    private boolean selectorRun = true;
    private Selector selector;
    private SocketChannel socketChannel;

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
    private class connectRunnable implements Runnable {

        /** service host */
        private String host;
        /** port */
        private int port;

        private int threadId;

        public connectRunnable(String host, int port) {
            this.host = host;
            this.port = port;
            threadId = (int) (Math.random() * 100);
            selectorRun = true;
        }

        @Override
        public synchronized void run() {
            try {
                socketChannel = SocketChannel.open();
                socketChannel.configureBlocking(false);
                selector = Selector.open();

                if (socketChannel.connect(new InetSocketAddress(host, port))) {
                    socketChannel.register(selector, SelectionKey.OP_READ);

                    Connect.IMRequest firstShake = SendMsgUtil.firstLoginShake();
                    MsgSendManager.getInstance().sendMessage(SocketACK.HAND_SHAKE_FIRST.getOrder(), firstShake.toByteArray());
                } else {
                    socketChannel.register(selector, SelectionKey.OP_CONNECT);
                }

                while (selectorRun && selector != null) {
                    LogManager.getLogger().d(Tag, "selector :threadId " + threadId);
                    selector.select();
                    Set<SelectionKey> setKeys = selector.selectedKeys();
                    if (setKeys == null) continue;
                    Iterator<SelectionKey> selectionKeys = selector.selectedKeys().iterator();
                    SelectionKey selectionKey = null;
                    while (selectionKeys.hasNext()) {
                        selectionKey = selectionKeys.next();
                        selectionKeys.remove();

                        LogManager.getLogger().d(Tag, "selectionKeys : threadId" + threadId);
                        if (selectionKey.isConnectable()) {
                            if (socketChannel.finishConnect()) {
                                selectionKey.interestOps(SelectionKey.OP_READ);

                                Connect.IMRequest firstShake = SendMsgUtil.firstLoginShake();
                                MsgSendManager.getInstance().sendMessage(SocketACK.HAND_SHAKE_FIRST.getOrder(), firstShake.toByteArray());
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
                                MsgByteManager.getInstance().putByteMsg(byteBuffer);
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

    public boolean avaliableConnect() {
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

    public synchronized void connectServer() {
        LogManager.getLogger().d(Tag, "connectServer conect service...");
        boolean reliNet = HttpRequest.isConnectNet();
        if (!reliNet) {
            return;
        }
        if (avaliableChannel()) {
            stopConnect();
        }

        String address = ConfigUtil.getInstance().socketAddress();
        int port = ConfigUtil.getInstance().socketPort();

        ConnectState.getInstance().sendEvent(ConnectState.ConnectType.REFRESH_ING);
        Thread connectThread = new Thread(new connectRunnable(address, port));
        connectThread.start();
    }

    /**
     * The user logged out/account no longer detect abnormal initiate reconnection
     */
    public void exitConnect() {
        canConnect = false;
        cancelTimer();
        stopConnect();
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
        canConnect = true;
        reconHandler.removeMessages(TAG_CONNECT);
        resetFibonacci();
        ConnectManager.getInstance().launHeartBit();
    }

    /** Whether can initiate reconnection */
    private boolean canConnect = true;
    /** Heart rate */
    private final long HEART_FREQUENCY = 10 * 1000;
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
        if (!canConnect) return;

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
                    MsgSendManager.getInstance().sendMessage(SocketACK.HEART_BREAK.getOrder(), new byte[]{});
                    checkUserCookie();
                } else {
                    stopConnect();
                    reconDelay();
                }
            }

            /**
             * check cookie expire time
             */
            public synchronized void checkUserCookie() {
                boolean checkExpire = false;
                UserCookie userCookie = Session.getInstance().getUserCookie(MemoryDataManager.getInstance().getPubKey());
                if (userCookie != null) {
                    long curTime = TimeUtil.getCurrentTimeSecond();
                    checkExpire = curTime >= userCookie.getExpiredTime();
                }
                if (checkExpire) {
                    CommandBean commandBean = new CommandBean((byte) 0x00, null);
                    commandBean.chatCookieInfo(3);
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
        return canConnect;
    }
}
