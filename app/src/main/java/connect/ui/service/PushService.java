package connect.ui.service;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;

import com.google.gson.Gson;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import connect.im.IMessage;
import connect.im.netty.BufferBean;
import connect.im.netty.MessageDecoder;
import connect.im.netty.MessageEncoder;
import connect.im.netty.NettySession;
import connect.ui.service.bean.ServiceAck;
import connect.ui.service.bean.ServiceInfoBean;
import connect.utils.TimeUtil;
import connect.utils.log.LogManager;
import connect.utils.okhttp.HttpRequest;
import io.netty.bootstrap.Bootstrap;
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

public class PushService extends Service {

    private String Tag = "tag_PushService";
    private PushService service;
    private IMessage localBinder;
    private PushBinder pushBinder;
    private PushConnect pushConnect;

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
    }

    public static void startService(Context context) {
        Intent intent = new Intent(context, PushService.class);
        context.startService(intent);
    }

    class PushConnect implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            try {
                LogManager.getLogger().d(Tag, "onServiceConnected"+TimeUtil.getCurrentTimeInString(TimeUtil.DATE_FORMAT_SECOND));
                localBinder = IMessage.Stub.asInterface(service);
                localBinder.connectMessage(ServiceAck.SERVER_ADDRESS.getAck(), new byte[0], new byte[0]);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            if (service == null) {
                service = PushService.this;
            }

            if (pushConnect == null) {
                Intent intent = new Intent(service, SocketService.class);
                service.startService(intent);
                service.bindService(intent, pushConnect, Service.BIND_IMPORTANT);
            }
        }
    }

    class PushBinder extends IMessage.Stub {

        @Override
        public void connectMessage(int type, byte[] ack, byte[] message) throws RemoteException {
            ByteBuffer byteBuffer;
            BufferBean bufferBean;
            ServiceAck serviceAck = ServiceAck.valueOf(type);

            switch (serviceAck) {
                case BIND_SUCCESS:
                    LogManager.getLogger().d(Tag, "connectMessage :BIND_SUCCESS;"+TimeUtil.getCurrentTimeInString(TimeUtil.DATE_FORMAT_SECOND));
                    Intent intent = new Intent(service, SocketService.class);
                    bindService(intent, pushConnect, Service.BIND_IMPORTANT);
                    break;
                case MESSAGE:
                    bufferBean = new BufferBean(ack, message);
                    writeBytes(bufferBean);
                    break;
                case CONNECT_START:
                    connectService();
                    break;
                case CONNECT_SUCCESS:
                    connectSuccess();
                    break;
                case EXIT_ACCOUNT:
                    stopConnect();
                    localBinder.connectMessage(ServiceAck.EXIT_ACCOUNT.getAck(), new byte[0], new byte[0]);
                    unbindService(pushConnect);
                    pushConnect = null;
                    stopSelf();
                    break;
                case SERVER_ADDRESS:
                    try {
                        byteBuffer = ByteBuffer.wrap(message);
                        String serviceInfo = new String(byteBuffer.array(), "utf-8");
                        ServiceInfoBean serviceInfoBean = new Gson().fromJson(serviceInfo, ServiceInfoBean.class);

                        socketAddress = serviceInfoBean.getServiceAddress();
                        socketPort = serviceInfoBean.getPort();
                        canReConnect = true;
                        connectService();
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /** tag */
    private final int TAG_CONNECT = 100;
    /** The message exchange Even the Fibonacci sequence */
    private long[] reconFibonacci = new long[]{1000, 1000};

    private Handler reconHandler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case TAG_CONNECT:
                    if (HttpRequest.isConnectNet()) {
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
        LogManager.getLogger().d(Tag, "connectServer reconDelay()...");
        if (canReConnect && !reconHandler.hasMessages(TAG_CONNECT)) {
            long count = reconFibonacci[0] + reconFibonacci[1];
            reconFibonacci[0] = reconFibonacci[1];
            reconFibonacci[1] = count;

            LogManager.getLogger().d(Tag, "connectServer reconDelay()..." + count / 1000 + "s reconnect");
            Message msg = Message.obtain(reconHandler, TAG_CONNECT);
            reconHandler.sendMessageDelayed(msg, count);
        }
    }


    private ConnectRunable connectRunable;
    private static ExecutorService threadPoolExecutor = Executors.newSingleThreadExecutor();
    private boolean canReConnect = true;
    private String socketAddress;
    private int socketPort;

    public void connectService() {
        LogManager.getLogger().d(Tag, "connectService:" + TimeUtil.getCurrentTimeInString(TimeUtil.DATE_FORMAT_SECOND));
        if (connectRunable != null) {
            connectRunable.stopRun();
        }
        connectRunable = new ConnectRunable();
        threadPoolExecutor.submit(connectRunable);
    }

    class ConnectRunable implements Runnable {

        @Override
        public void run() {
            EventLoopGroup loopGroup = new NioEventLoopGroup();

            Bootstrap bootstrap = new Bootstrap();
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.option(ChannelOption.SO_BACKLOG, 128);
            bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
            bootstrap.option(ChannelOption.TCP_NODELAY, true);//TCP协议
            bootstrap.group(loopGroup);//java.lang.OutOfMemoryError: Could not allocate JNI Env
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {

                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new IdleStateHandler(20, 20, 0, TimeUnit.SECONDS));
                    ch.pipeline().addLast(new MessageEncoder());
                    ch.pipeline().addLast(new MessageDecoder());
                    ch.pipeline().addLast(new ConnectHandlerAdapter());
                }
            });

            ChannelFuture future = bootstrap.connect(socketAddress, socketPort);
            try {
                future.addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        if (!future.isSuccess()) {
                            reconDelay();
                        }
                    }
                });
                future.sync();
                LogManager.getLogger().d(Tag, "Thread:" + TimeUtil.getCurrentTimeInString(TimeUtil.DATE_FORMAT_SECOND));

                SocketChannel channel = (SocketChannel) future.channel();
                NettySession nettySession = NettySession.getInstance();
                nettySession.setLoopGroup(loopGroup);
                nettySession.setChannel(channel);

                channel.closeFuture().sync();
            } catch (Exception e) {
                e.printStackTrace();
                LogManager.getLogger().d(Tag, "connectService() Exception ==> " + e.getMessage());
                reconDelay();
            } finally {
                NettySession.getInstance().shutDown();
                reconDelay();
            }
        }

        public void stopRun(){
            NettySession.getInstance().shutDown();
        }
    }

    public void stopConnect() {
        LogManager.getLogger().d(Tag, "stopConnect() ==> ");
        canReConnect = false;

        NettySession.getInstance().shutDown();
    }

    public void writeBytes(BufferBean bufferBean) {
        if (!NettySession.getInstance().isWriteAble()) {
            LogManager.getLogger().d(Tag, "writeBytes() channel ==> is null?");
            reconDelay();
        } else {
            LogManager.getLogger().d(Tag, "writeBytes:" + TimeUtil.getCurrentTimeInString(TimeUtil.DATE_FORMAT_SECOND));
            NettySession.getInstance().getChannel().writeAndFlush(bufferBean);
        }
    }

    /** Recently received a message of time */
    private long lastReceiverTime;
    /** Heart rate */
    private final long HEART_FREQUENCY = 20 * 1000;

    @ChannelHandler.Sharable
    class ConnectHandlerAdapter extends ChannelHandlerAdapter {

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            super.channelRead(ctx, msg);
            lastReceiverTime = TimeUtil.getCurrentTimeInLong();
            BufferBean bufferBean = (BufferBean) msg;
            localBinder.connectMessage(ServiceAck.MESSAGE.getAck(), bufferBean.getAck(), bufferBean.getMessage());
        }

        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            super.userEventTriggered(ctx, evt);
            if (IdleStateEvent.class.isAssignableFrom(evt.getClass())) {
                IdleStateEvent event = (IdleStateEvent) evt;
                if (event.state() == IdleState.READER_IDLE || event.state() == IdleState.WRITER_IDLE ||
                        event.state() == IdleState.ALL_IDLE) {
                    long curtime = TimeUtil.getCurrentTimeInLong();
                    if (curtime < lastReceiverTime + 2 * HEART_FREQUENCY) {
                        LogManager.getLogger().d(Tag, "userEventTriggered() ==> send heartbeat");
                        try {
                            localBinder.connectMessage(ServiceAck.HEART_BEAT.getAck(), new byte[0], new byte[0]);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                            reconDelay();
                        }
                    } else {//connect timeout
                        LogManager.getLogger().d(Tag, "userEventTriggered() ==> connect timeout");
                        reconDelay();
                    }
                }
            }
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            super.channelActive(ctx);
            LogManager.getLogger().d(Tag, "channelActive() == ");
            localBinder.connectMessage(ServiceAck.HAND_SHAKE.getAck(), new byte[0], new byte[0]);
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
            LogManager.getLogger().d(Tag, "channelInactive() ==> connection is broken");
            reconDelay();
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            super.exceptionCaught(ctx, cause);
            ctx.close();
            LogManager.getLogger().d(Tag, "exceptionCaught() ==");
            reconDelay();
        }
    }
}
