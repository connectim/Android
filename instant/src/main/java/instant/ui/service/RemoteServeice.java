package instant.ui.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

import connect.im.IMessage;
import instant.bean.Session;
import instant.bean.SocketACK;
import instant.bean.UserCookie;
import instant.netty.BufferBean;
import instant.netty.CSslHandler;
import instant.netty.MessageDecoder;
import instant.netty.MessageEncoder;
import instant.netty.NettySession;
import instant.ui.InstantSdk;
import instant.utils.TimeUtil;
import instant.utils.XmlParser;
import instant.utils.log.LogManager;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * Created by puin on 17-9-21.
 */

public class RemoteServeice extends Service {

    private static String TAG = "_RemoteServeice";

    private RemoteServeice service;
    private IMessage localBinder;
    private PushBinder pushBinder;
    private PushConnect pushConnect;

    private RemoteNetWorkBroadcastReceiver broadcastReceiver = new RemoteNetWorkBroadcastReceiver();

    @Override
    public IBinder onBind(Intent intent) {
        return pushBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        service = this;
        if (pushBinder == null) {
            pushBinder = new PushBinder();
        }
        if (pushConnect == null) {
            pushConnect = new PushConnect();
        }

        // 监听网络变换
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(broadcastReceiver, filter);
    }

    public static void startService(Context context) {
        Intent intent = new Intent(context, RemoteServeice.class);
        context.startService(intent);
    }

    class PushConnect implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LogManager.getLogger().d(TAG, "PushConnect :onServiceConnected");

            localBinder = IMessage.Stub.asInterface(service);

            socketAddress = XmlParser.getInstance().socketAddress();
            socketPort = XmlParser.getInstance().socketPort();
            canReConnect = true;
            connectService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            LogManager.getLogger().d(TAG, "PushConnect :onServiceDisconnected");

            if (service == null) {
                service = RemoteServeice.this;
            }

            if (pushConnect == null) {
                Intent intent = new Intent(service, RemoteServeice.class);
                service.startService(intent);
                service.bindService(intent, pushConnect, Service.BIND_IMPORTANT);
            }
        }
    }

    class PushBinder extends IMessage.Stub {

        @Override
        public void serviceBind() throws RemoteException {
            LogManager.getLogger().d(TAG, "serviceBind");

            Intent intent = new Intent(service, SenderService.class);
            bindService(intent, pushConnect, Service.BIND_IMPORTANT);
        }

        @Override
        public void connectStart() throws RemoteException {
            LogManager.getLogger().d(TAG, "connectStart");

            connectService();
        }

        @Override
        public void connectMessage(byte[] ack, byte[] message) throws RemoteException {
            if (!NettySession.getInstance().isWriteAble()) {
                reconDelay();
            } else {
                BufferBean bufferBean = new BufferBean(ack, message);
                NettySession.getInstance().getChannel().writeAndFlush(bufferBean).addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        if (!future.isSuccess()) {
                            reconDelay();
                        }
                    }
                });

                if (SocketACK.PULL_OFFLINE.equals(ack)) {
                    connectSuccess();
                }
            }
        }

        @Override
        public void heartBeat() throws RemoteException {

        }

        @Override
        public void connectStop() throws RemoteException {

        }

        @Override
        public void connectExit() throws RemoteException {
            stopConnect();
            if (localBinder != null) {
                localBinder.connectExit();
                localBinder = null;
            }
            if (pushConnect != null) {
                unbindService(pushConnect);
                pushConnect = null;
            }
            stopSelf();
        }
    }

    private final int TAG_CONNECT = 100;
    /** The message exchange Even the Fibonacci sequence */
    private long[] reconFibonacci = new long[]{1000, 1000};

    private Handler reconHandler = new Handler(Looper.myLooper()) {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case TAG_CONNECT:
                    if (isConnectNet()) {
                        connectService();
                    }
                    break;
            }
        }
    };

    public void resetFibonacci() {
        reconFibonacci[0] = 1000;
        reconFibonacci[1] = 1000;
    }


    /**
     * connect success
     */
    public void connectSuccess() {
        canReConnect = true;
        resetFibonacci();
        reconHandler.removeMessages(TAG_CONNECT);
    }

    public void reconDelay() {
        LogManager.getLogger().d(TAG, "connectServer reconDelay()...");
        if (canReConnect && !reconHandler.hasMessages(TAG_CONNECT)) {
            if (reconFibonacci[0] + reconFibonacci[1] > 34000) {//10 time repeat
                resetFibonacci();
            }

            long count = reconFibonacci[0] + reconFibonacci[1];
            reconFibonacci[0] = reconFibonacci[1];
            reconFibonacci[1] = count;

            LogManager.getLogger().d(TAG, "connectServer reconDelay()..." + count / 1000 + "s reconnect");
            Message msg = Message.obtain(reconHandler, TAG_CONNECT);
            reconHandler.sendMessageDelayed(msg, count);
        }
    }

    private ConnectRunable connectRunable;
    private static ExecutorService threadPoolExecutor = Executors.newSingleThreadExecutor();

    private final static int CONNECT_TIMEOUT = 30000;
    private final static int READERIDLE_TIME = 10;
    private final static int WRITERIDLE_TIME = 0;

    private static boolean canReConnect = true;
    private String socketAddress;
    private int socketPort;

    public void connectService() {
        LogManager.getLogger().d(TAG, "connectService");
        NettySession.getInstance().shutDown();

        connectRunable = new ConnectRunable();
        threadPoolExecutor.submit(connectRunable);
    }

    private class ConnectRunable implements Runnable {

        @Override
        public void run() {
            EventLoopGroup loopGroup = new NioEventLoopGroup();

            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(loopGroup);//java.lang.OutOfMemoryError: Could not allocate JNI Env
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
            bootstrap.option(ChannelOption.TCP_NODELAY, true);//TCP
            bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, CONNECT_TIMEOUT);

            bootstrap.handler(new ChannelInitializer<SocketChannel>() {

                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    SSLContext clientContext = SSLContext.getDefault();
//                    io.netty.handler.ssl.SslContext clientContext = SslContextFactory.getServerContext();
                    SSLEngine engine = clientContext.createSSLEngine();
                    engine.setUseClientMode(true);
                    engine.setNeedClientAuth(false);
                    ch.pipeline().addLast(new CSslHandler(engine));

                    ch.pipeline().addLast(new IdleStateHandler(READERIDLE_TIME, WRITERIDLE_TIME, 0, TimeUnit.SECONDS));
                    ch.pipeline().addLast(new MessageEncoder());
                    ch.pipeline().addLast(new MessageDecoder());
                    ch.pipeline().addLast(new ConnectHandlerAdapter());
                }
            });

            LogManager.getLogger().d(TAG, "connectService() info ==> ip:" + socketAddress + ";  port:" + socketPort);
            ChannelFuture future = bootstrap.connect(socketAddress, socketPort);
            try {
                future.addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        if (future.isSuccess()) {
                            LogManager.getLogger().d(TAG, "connectService() isSuccess ==> ");
                            localBinder.connectStart();
                        } else if (!future.isSuccess()) {
                            LogManager.getLogger().d(TAG, "connectService() isFail ==> ");
                            reconDelay();
                        }
                    }
                }).sync();

                Channel channel = future.channel();
                NettySession.getInstance().setLoopGroup(loopGroup);
                NettySession.getInstance().setChannel(channel);

                channel.closeFuture().sync();
            } catch (Exception e) {
                e.printStackTrace();
                LogManager.getLogger().d(TAG, "connectService() Exception ==> " + e.getMessage());
                reconDelay();
            } finally {
                LogManager.getLogger().d(TAG, "connectService() finally ==> ");
                NettySession.getInstance().shutDown();
                reconDelay();
            }
        }
    }

    public void stopConnect() {
        LogManager.getLogger().d(TAG, "stopConnect() ==> ");
        canReConnect = false;
        NettySession.getInstance().shutDown();
    }

    /** Recently received a message of time */
    private long lastReceiverTime = TimeUtil.getCurrentTimeInLong();
    /** Heart rate */
    private final static long HEART_FREQUENCY = (READERIDLE_TIME + WRITERIDLE_TIME + 5) * 1000;

    @ChannelHandler.Sharable
    class ConnectHandlerAdapter extends ChannelHandlerAdapter {

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            super.channelRead(ctx, msg);
            lastReceiverTime = TimeUtil.getCurrentTimeInLong();
            BufferBean bufferBean = (BufferBean) msg;
            LogManager.getLogger().d(TAG, "channelRead() ==> ack:[" +
                    bufferBean.getAck()[0] + "][" + bufferBean.getAck()[1] + "]");

            localBinder.connectMessage(bufferBean.getAck(), bufferBean.getMessage());
        }

        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            super.userEventTriggered(ctx, evt);
            if (IdleStateEvent.class.isAssignableFrom(evt.getClass())) {
                IdleStateEvent event = (IdleStateEvent) evt;
                if (event.state() == IdleState.READER_IDLE || event.state() == IdleState.WRITER_IDLE ||
                        event.state() == IdleState.ALL_IDLE) {
                    long curtime = TimeUtil.getCurrentTimeInLong();
                    if (curtime < lastReceiverTime + HEART_FREQUENCY) {
                        LogManager.getLogger().d(TAG, "userEventTriggered() ==> " + (curtime - lastReceiverTime));
                        try {
                            localBinder.heartBeat();
                        } catch (RemoteException e) {
                            e.printStackTrace();
                            reconDelay();
                        }
                    } else {//connect timeout
                        ctx.close();
                        LogManager.getLogger().d(TAG, "userEventTriggered() ==> connect timeout" + (curtime - lastReceiverTime));
                        reconDelay();
                    }
                }
            }
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            super.channelActive(ctx);
            LogManager.getLogger().d(TAG, "channelActive() == ");
            //localBinder.connectMessage(ServiceAck.HAND_SHAKE.getAck(), new byte[0], new byte[0]);
        }

        /**
         * When the TCP connection is broken, it will be called back
         *
         * @param ctx
         * @throws Exception
         */
        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            super.channelInactive(ctx);
            ctx.close();
            LogManager.getLogger().d(TAG, "channelInactive() ==> connection is broken");

            reconDelay();
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            super.exceptionCaught(ctx, cause);
            ctx.close();
            LogManager.getLogger().d(TAG, "exceptionCaught() ==");

            reconDelay();
        }
    }

    /**
     * network environment
     *
     * @return
     */
    public boolean isConnectNet() {
        Context context = InstantSdk.getInstance().getBaseContext();
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        boolean isWifiConn = networkInfo.isConnected();

        networkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        boolean isMobileConn;
        if (null != networkInfo) {
            isMobileConn = networkInfo.isConnected();
        } else {
            isMobileConn = false;
        }
        return isWifiConn || isMobileConn;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }

    private class RemoteNetWorkBroadcastReceiver extends BroadcastReceiver {

        /** The default for repeated connection broadcast time */
        private static final int TIME_REPEART = 5000;
        /** The last received time */
        private long lastReceiveTime = 0;

        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                if (context == null) {
                    context = InstantSdk.getInstance().getBaseContext();
                }
                ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo.State wifiState = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
                NetworkInfo.State mobileState = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();

                if ((wifiState != null && NetworkInfo.State.CONNECTED == wifiState) ||
                        (mobileState != null && NetworkInfo.State.CONNECTED == mobileState)) {//Network connection is successful
                    LogManager.getLogger().d(TAG, "NetBroadcastReceiver onReceive()...Switch to the network environment");

                    if (isCanConnect() && (TimeUtil.getCurrentTimeInLong() - lastReceiveTime > TIME_REPEART)) {
                        lastReceiveTime = TimeUtil.getCurrentTimeInLong();
                        connectSuccess();
                        reconDelay();
                    }
                } else {
                    LogManager.getLogger().d(TAG, "NetBroadcastReceiver onReceive()...Network disconnection");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public boolean isCanConnect() {
            UserCookie userCookie = Session.getInstance().getConnectCookie();
            return userCookie != null;
        }
    }
}
