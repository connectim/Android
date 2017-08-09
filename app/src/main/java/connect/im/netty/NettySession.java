package connect.im.netty;

import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;

/**
 * Created by Administrator on 2017/6/27.
 */

public class NettySession {

    public static NettySession session;

    public static NettySession getInstance() {
        if (session == null) {
            synchronized (NettySession.class) {
                if (session == null) {
                    session = new NettySession();
                }
            }
        }
        return session;
    }

    private EventLoopGroup loopGroup;
    private Channel channel;

    public void setLoopGroup(EventLoopGroup loopGroup) {
        this.loopGroup = loopGroup;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }


    public boolean isWriteAble() {
        boolean able = true;
        if (channel == null || !channel.isOpen() || !channel.isActive() || !channel.isWritable()) {
            able = false;
        }
        return able;
    }

    public void shutDown() {
        if (loopGroup != null) {
            loopGroup.shutdownGracefully();
            loopGroup = null;
        }
    }
}
