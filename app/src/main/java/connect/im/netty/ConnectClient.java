package connect.im.netty;

import connect.im.IMessage;
import connect.ui.service.bean.PushMessage;
import connect.ui.service.bean.ServiceAck;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * Created by Administrator on 2017/6/21.
 */

public class ConnectClient {

    private IMessage iMessage;
    private Channel channel;

    public void connect(String host, int port) {
        EventLoopGroup loopGroup = new NioEventLoopGroup();
        ConnectChannelHandler channelHandler = new ConnectChannelHandler();

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(loopGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.SO_TIMEOUT, 5000)
                .handler(channelHandler);

        ChannelFuture future = bootstrap.connect(host, port);
        try {
            future.sync();
            future.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (future.isSuccess()) {
                        iMessage.connectMessage(ServiceAck.HAND_SHAKE.getAck(), new byte[0], new byte[0]);
                    }
                }
            });
            channel = future.channel();
            channel.closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            loopGroup.shutdownGracefully();
        }
    }

    public void writeBytes(MBufferBean bufferBean) {
        channel.writeAndFlush(bufferBean);
    }

    public void setiMessage(IMessage iMessage) {
        this.iMessage = iMessage;
    }

    class ConnectChannelHandler extends ChannelInitializer {

        @Override
        protected void initChannel(Channel ch) throws Exception {
            ch.pipeline().addLast(new MessageEncoder());
            ch.pipeline().addLast(new MessageDecoder());
            ch.pipeline().addLast(new ConnectHandlerAdapter());
        }
    }

    class ConnectHandlerAdapter extends ChannelHandlerAdapter {

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            super.channelRead(ctx, msg);
            MBufferBean bufferBean = (MBufferBean) msg;
            iMessage.connectMessage(ServiceAck.MESSAGE.getAck(), bufferBean.getAck(), bufferBean.getMessage());
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            super.exceptionCaught(ctx, cause);
        }
    }
}
