package connect.ui.activity.chat.model.content;

import com.google.gson.Gson;

import connect.im.bean.MsgType;
import connect.im.bean.SocketACK;
import connect.im.model.ChatSendManager;
import connect.ui.activity.R;
import connect.ui.activity.chat.bean.AdBean;
import connect.ui.activity.chat.bean.MsgDefinBean;
import connect.ui.activity.chat.bean.MsgEntity;
import connect.ui.activity.chat.bean.MsgSender;
import connect.ui.activity.chat.bean.TransferExt;
import connect.ui.base.BaseApplication;
import connect.utils.TimeUtil;
import protos.Connect;

/**
 * robot message
 * Created by pujin on 2017/1/19.
 */
public class RobotChat extends NormalChat {

    private static RobotChat robotChat;

    public RobotChat() {
    }

    public static RobotChat getInstance() {
        if (robotChat == null) {
            synchronized (RobotChat.class) {
                if (robotChat == null) {
                    robotChat = new RobotChat();
                }
            }
        }
        return robotChat;
    }

    @Override
    public String headImg() {
        return BaseApplication.getInstance().getString(R.string.app_name);
    }

    @Override
    public String nickName() {
        return BaseApplication.getInstance().getString(R.string.app_name);
    }

    @Override
    public String address() {
        return BaseApplication.getInstance().getString(R.string.app_name);
    }

    /**
     * group review
     * @param content
     * @param ext1
     * @return
     */
    public MsgEntity groupReviewMsg(String content, String ext1) {
        MsgEntity bean = createBaseChat(MsgType.GROUP_REVIEW);
        bean.getMsgDefinBean().setContent(content);
        bean.getMsgDefinBean().setExt1(ext1);
        String connect = BaseApplication.getInstance().getBaseContext().getString(R.string.app_name);
        bean.getMsgDefinBean().setSenderInfoExt(new MsgSender(connect, connect));
        return bean;
    }

    public MsgEntity outerPacketGetNoticfe(Connect.SystemRedpackgeNotice notice) {
        MsgEntity bean = createBaseChat(MsgType.OUTERPACKET_GET);

        TransferExt transferExt = new TransferExt(notice.getAmount(), "", 1);
        Connect.UserInfo receiver = notice.getReceiver();
        bean.getMsgDefinBean().setContent(notice.getHashid());
        bean.getMsgDefinBean().setSenderInfoExt(new MsgSender(receiver.getPubKey(), receiver.getUsername(), receiver.getAddress(), receiver.getAvatar()));
        bean.getMsgDefinBean().setExt1(new Gson().toJson(transferExt));
        return bean;
    }

    public MsgEntity systemAdNotice(AdBean adBean) {
        MsgEntity bean = createBaseChat(MsgType.SYSTEM_AD);
        bean.getMsgDefinBean().setContent(new Gson().toJson(adBean));

        String connect = BaseApplication.getInstance().getBaseContext().getString(R.string.app_name);
        bean.getMsgDefinBean().setSenderInfoExt(new MsgSender(connect, connect));
        return bean;
    }

    @Override
    public MsgEntity createBaseChat(MsgType type) {
        MsgDefinBean msgDefinBean = new MsgDefinBean();
        msgDefinBean.setType(type.type);
        msgDefinBean.setSendtime(TimeUtil.getCurrentTimeInLong());
        msgDefinBean.setMessage_id(TimeUtil.timestampToMsgid());
        msgDefinBean.setPublicKey(BaseApplication.getInstance().getString(R.string.app_name));

        MsgEntity robotMsg = new MsgEntity();
        robotMsg.setMsgDefinBean(msgDefinBean);
        robotMsg.setPubkey(BaseApplication.getInstance().getString(R.string.app_name));
        robotMsg.setSendstate(0);
        return robotMsg;
    }

    @Override
    public void sendPushMsg(Object bean) {
        MsgEntity roMsgEntity = (MsgEntity) bean;
        MsgDefinBean definBean = roMsgEntity.getMsgDefinBean();
        Connect.MSMessage.Builder builder = Connect.MSMessage.newBuilder();
        int type = roMsgEntity.getMsgDefinBean().getType();
        builder.setMsgId(definBean.getMessage_id());
        builder.setCategory(type);
        switch (type) {
            case 1:
                Connect.TextMessage textMessage = Connect.TextMessage.newBuilder()
                        .setContent(definBean.getContent()).build();
                builder.setBody(textMessage.toByteString());
                break;
            case 2:
                Connect.Voice voice = Connect.Voice.newBuilder()
                        .setDuration(definBean.getSize())
                        .setUrl(definBean.getContent()).build();
                builder.setBody(voice.toByteString());
                break;
            case 3:
                Connect.Image image = Connect.Image.newBuilder()
                        .setUrl(definBean.getContent()).build();
                builder.setBody(image.toByteString());
                break;
            case 17:
                Connect.Image location = Connect.Image.newBuilder()
                        .setUrl(definBean.getContent()).build();
                builder.setBody(location.toByteString());
                break;
            default:
                return;
        }
        ChatSendManager.getInstance().sendRobotAckMsg(SocketACK.ROBOT_CHAT, definBean.getPublicKey(), builder.build());
    }

    @Override
    public String roomKey() {
        return BaseApplication.getInstance().getString(R.string.app_name);
    }

    @Override
    public int roomType() {
        return 2;
    }
}
