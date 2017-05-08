package connect.ui.activity.chat.model.content;

import com.google.gson.Gson;

import connect.db.green.bean.ContactEntity;
import connect.im.bean.MsgType;
import connect.im.bean.SocketACK;
import connect.im.model.ChatSendManager;
import connect.ui.activity.R;
import connect.ui.activity.chat.bean.AdBean;
import connect.ui.activity.chat.bean.GatherBean;
import connect.ui.activity.chat.bean.MsgDefinBean;
import connect.ui.activity.chat.bean.MsgSender;
import connect.ui.activity.chat.bean.RoMsgEntity;
import connect.ui.activity.chat.bean.TransferExt;
import connect.ui.activity.chat.bean.WebsiteExt1Bean;
import connect.ui.activity.locmap.bean.GeoAddressBean;
import connect.ui.base.BaseApplication;
import connect.utils.TimeUtil;
import protos.Connect;

/**
 * robot message
 * Created by pujin on 2017/1/19.
 */
public class RobotChat extends BaseChat {

    private static RobotChat robotChat;

    public RobotChat() {
    }

    public static RobotChat getInstance() {
        if (robotChat == null) {
            robotChat = new RobotChat();
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

    @Override
    public RoMsgEntity txtMsg(String string) {
        RoMsgEntity bean = createBaseChat(MsgType.Text);
        bean.getMsgDefinBean().setContent(string);
        return bean;
    }

    @Override
    public RoMsgEntity photoMsg(String string, String ext1) {
        RoMsgEntity bean = createBaseChat(MsgType.Photo);
        bean.getMsgDefinBean().setContent(string);
        bean.getMsgDefinBean().setUrl(string);
        bean.getMsgDefinBean().setExt1(ext1);
        return bean;
    }

    @Override
    public RoMsgEntity videoMsg(String string, int length, String ext1) {
        return null;
    }

    @Override
    public RoMsgEntity voiceMsg(String string, int size, String ext1) {
        return null;
    }

    @Override
    public RoMsgEntity transferMsg(String hashid, long amout, String note, int type) {
        RoMsgEntity bean = createBaseChat(MsgType.Transfer);
        bean.getMsgDefinBean().setContent(hashid);

        TransferExt ext = new TransferExt(amout, note, type);
        bean.getMsgDefinBean().setExt1(new Gson().toJson(ext));
        return bean;
    }

    public RoMsgEntity locationMsg(String address, GeoAddressBean location) {
        RoMsgEntity bean = createBaseChat(MsgType.Location);
        bean.getMsgDefinBean().setContent(address);
        bean.getMsgDefinBean().setLocationExt(location);
        return bean;
    }

    @Override
    public RoMsgEntity emotionMsg(String string) {
        return null;
    }

    @Override
    public RoMsgEntity luckPacketMsg(String string, String tips, int type) {
        RoMsgEntity bean = createBaseChat(MsgType.Lucky_Packet);
        bean.getMsgDefinBean().setContent(string);

        TransferExt ext = new TransferExt();
        ext.setNote(tips);
        ext.setType(type);
        bean.getMsgDefinBean().setExt1(new Gson().toJson(ext));
        return bean;
    }

    /**
     * group review
     * @param content
     * @param ext1
     * @return
     */
    public RoMsgEntity groupReviewMsg(String content, String ext1) {
        RoMsgEntity bean = createBaseChat(MsgType.GROUP_REVIEW);
        bean.getMsgDefinBean().setContent(content);
        bean.getMsgDefinBean().setExt1(ext1);
        String connect = BaseApplication.getInstance().getBaseContext().getString(R.string.app_name);
        bean.getMsgDefinBean().setSenderInfoExt(new MsgSender(connect, connect));
        return bean;
    }

    @Override
    public RoMsgEntity noticeMsg(String string) {
        RoMsgEntity bean = createBaseChat(MsgType.NOTICE);
        bean.getMsgDefinBean().setContent(string);
        return bean;
    }

    public RoMsgEntity outerPacketGetNoticfe(Connect.SystemRedpackgeNotice notice) {
        RoMsgEntity bean = createBaseChat(MsgType.OUTERPACKET_GET);

        TransferExt transferExt = new TransferExt(notice.getAmount(), "", 1);
        Connect.UserInfo receiver = notice.getReceiver();
        bean.getMsgDefinBean().setContent(notice.getHashid());
        bean.getMsgDefinBean().setSenderInfoExt(new MsgSender(receiver.getPubKey(), receiver.getUsername(), receiver.getAddress(), receiver.getAvatar()));
        bean.getMsgDefinBean().setExt1(new Gson().toJson(transferExt));
        return bean;
    }

    public RoMsgEntity systemAdNotice(AdBean adBean) {
        RoMsgEntity bean = createBaseChat(MsgType.SYSTEM_AD);
        bean.getMsgDefinBean().setContent(new Gson().toJson(adBean));

        String connect = BaseApplication.getInstance().getBaseContext().getString(R.string.app_name);
        bean.getMsgDefinBean().setSenderInfoExt(new MsgSender(connect, connect));
        return bean;
    }

    @Override
    public Object cardMsg(ContactEntity entity) {
        return null;
    }

    @Override
    public Object destructMsg(long time) {
        return null;
    }

    @Override
    public Object receiptMsg(String string) {
        return null;
    }

    @Override
    public Object paymentMsg(GatherBean bean) {
        return null;
    }

    @Override
    public Object outerWebsiteMsg(String string, WebsiteExt1Bean bean) {
        return null;
    }

    @Override
    public Object encryptChatMsg() {
        return null;
    }

    @Override
    public Object clickReceiveLuckMsg(String string) {
        RoMsgEntity chatBean = createBaseChat(MsgType.NOTICE_CLICKRECEIVEPACKET);
        chatBean.getMsgDefinBean().setContent(string);
        return chatBean;
    }

    @Override
    public RoMsgEntity createBaseChat(MsgType type) {
        MsgDefinBean msgDefinBean = new MsgDefinBean();
        msgDefinBean.setType(type.type);
        msgDefinBean.setSendtime(TimeUtil.getCurrentTimeInLong());
        msgDefinBean.setMessage_id(TimeUtil.timestampToMsgid());
        msgDefinBean.setPublicKey(BaseApplication.getInstance().getString(R.string.app_name));

        RoMsgEntity robotMsg = new RoMsgEntity();
        robotMsg.setMsgDefinBean(msgDefinBean);
        robotMsg.setMsgid(msgDefinBean.getMessage_id());
        robotMsg.setPubkey(BaseApplication.getInstance().getString(R.string.app_name));
        robotMsg.setSendstate(0);
        return robotMsg;
    }

    @Override
    public void sendPushMsg(Object bean) {
        RoMsgEntity roMsgEntity = (RoMsgEntity) bean;
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
