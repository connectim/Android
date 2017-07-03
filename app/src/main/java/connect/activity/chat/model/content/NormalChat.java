package connect.activity.chat.model.content;

import com.google.gson.Gson;

import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.bean.ContactEntity;
import connect.database.green.bean.GroupEntity;
import connect.im.bean.MsgType;
import connect.ui.activity.R;
import connect.activity.chat.bean.CardExt1Bean;
import connect.activity.chat.bean.GatherBean;
import connect.activity.chat.bean.GroupExt1Bean;
import connect.activity.chat.bean.MsgEntity;
import connect.activity.chat.bean.TransferExt;
import connect.activity.chat.bean.WebsiteExt1Bean;
import connect.activity.chat.bean.GeoAddressBean;
import connect.activity.base.BaseApplication;

/**
 * public methods to extract
 * Created by gtq on 2016/12/19.
 */
public abstract class NormalChat extends BaseChat {

    public static NormalChat loadBaseChat(String pubkey) {
        NormalChat normalChat = null;

        if ((BaseApplication.getInstance().getString(R.string.app_name)).equals(pubkey)) {
            normalChat = RobotChat.getInstance();
        } else {
            GroupEntity groupEntity = ContactHelper.getInstance().loadGroupEntity(pubkey);
            if (groupEntity != null) {
                normalChat = new GroupChat(groupEntity);
            } else {
                ContactEntity friendEntity = ContactHelper.getInstance().loadFriendEntity(pubkey);
                if (friendEntity != null) {
                    normalChat = new FriendChat(friendEntity);
                }
            }
        }
        return normalChat;
    }

    @Override
    public void updateRoomMsg(String draft, String showText, long msgtime) {
        super.updateRoomMsg(draft, showText, msgtime);
    }

    public MsgEntity txtMsg(String string) {
        MsgEntity chatBean = (MsgEntity) createBaseChat(MsgType.Text);
        chatBean.getMsgDefinBean().setContent(string);
        return chatBean;
    }

    public MsgEntity photoMsg(String string, String ext1) {
        MsgEntity chatBean = (MsgEntity) createBaseChat(MsgType.Photo);
        chatBean.getMsgDefinBean().setContent(string);
        chatBean.getMsgDefinBean().setUrl(string);
        chatBean.getMsgDefinBean().setExt1(ext1);
        return chatBean;
    }

    public MsgEntity videoMsg(String string, int length, String ext1) {
        MsgEntity chatBean = (MsgEntity) createBaseChat(MsgType.Video);
        chatBean.getMsgDefinBean().setContent(string);
        chatBean.getMsgDefinBean().setUrl(string);
        chatBean.getMsgDefinBean().setSize(length);
        chatBean.getMsgDefinBean().setExt1(ext1);
        return chatBean;
    }

    public MsgEntity voiceMsg(String string, int size, String ext1) {
        MsgEntity chatBean = (MsgEntity) createBaseChat(MsgType.Voice);
        chatBean.getMsgDefinBean().setContent(string);
        chatBean.getMsgDefinBean().setSize(size);
        chatBean.getMsgDefinBean().setExt1(ext1);
        return chatBean;
    }

    public MsgEntity emotionMsg(String string) {
        MsgEntity chatBean = (MsgEntity) createBaseChat(MsgType.Emotion);
        chatBean.getMsgDefinBean().setContent(string);
        return chatBean;
    }

    public MsgEntity cardMsg(ContactEntity entity) {
        CardExt1Bean ext1Bean = new CardExt1Bean();
        ext1Bean.setAvatar(entity.getAvatar());
        ext1Bean.setAddress(entity.getAddress());
        ext1Bean.setPub_key(entity.getPub_key());
        ext1Bean.setUsername(entity.getUsername());

        MsgEntity chatBean = (MsgEntity) createBaseChat(MsgType.Name_Card);
        chatBean.getMsgDefinBean().setExt1(new Gson().toJson(ext1Bean));
        return chatBean;
    }

    public MsgEntity destructMsg(long time) {
        MsgEntity chatBean = (MsgEntity) createBaseChat(MsgType.Self_destruct_Notice);
        chatBean.getMsgDefinBean().setContent(String.valueOf(time));
        return chatBean;
    }

    public MsgEntity receiptMsg(String string) {
        MsgEntity chatBean = (MsgEntity) createBaseChat(MsgType.Self_destruct_Receipt);
        chatBean.getMsgDefinBean().setContent(string);
        return chatBean;
    }

    public MsgEntity paymentMsg(GatherBean bean) {
        MsgEntity chatBean = (MsgEntity) createBaseChat(MsgType.Request_Payment);
        chatBean.getMsgDefinBean().setContent(bean.getHashid());
        chatBean.getMsgDefinBean().setExt1(new Gson().toJson(bean));
        return chatBean;
    }

    public MsgEntity transferMsg(String hashid, long amout, String note,int type) {
        MsgEntity chatBean = (MsgEntity) createBaseChat(MsgType.Transfer);
        chatBean.getMsgDefinBean().setContent(hashid);

        TransferExt ext = new TransferExt(amout, note, type);
        chatBean.getMsgDefinBean().setExt1(new Gson().toJson(ext));
        return chatBean;
    }

    public MsgEntity locationMsg(String address, GeoAddressBean location) {
        MsgEntity chatBean = (MsgEntity) createBaseChat(MsgType.Location);
        chatBean.getMsgDefinBean().setContent(address);
        chatBean.getMsgDefinBean().setLocationExt(location);
        chatBean.getMsgDefinBean().setImageOriginWidth(location.getImageOriginWidth());
        chatBean.getMsgDefinBean().setImageOriginHeight(location.getImageOriginHeight());
        return chatBean;
    }

    public MsgEntity luckPacketMsg(String string, String tips, int type) {
        MsgEntity chatBean = (MsgEntity) createBaseChat(MsgType.Lucky_Packet);
        chatBean.getMsgDefinBean().setContent(string);

        TransferExt ext = new TransferExt();
        ext.setNote(tips);
        ext.setType(type);
        chatBean.getMsgDefinBean().setExt1(new Gson().toJson(ext));
        return chatBean;
    }

    public MsgEntity noticeMsg(String string) {
        MsgEntity chatBean = (MsgEntity) createBaseChat(MsgType.NOTICE);
        chatBean.getMsgDefinBean().setContent(string);
        return chatBean;
    }

    public MsgEntity joinGroupMsg(GroupExt1Bean ext1Bean) {
        MsgEntity chatBean = (MsgEntity) createBaseChat(MsgType.INVITE_GROUP);
        chatBean.getMsgDefinBean().setExt1(new Gson().toJson(ext1Bean));
        return chatBean;
    }

    public MsgEntity strangerNotice() {
        MsgEntity chatBean = (MsgEntity) createBaseChat(MsgType.NOTICE_STRANGER);
        return chatBean;
    }

    public MsgEntity blackFriendNotice() {
        MsgEntity chatBean = (MsgEntity) createBaseChat(MsgType.NOTICE_BLACK);
        return chatBean;
    }

    public MsgEntity notMemberNotice() {
        MsgEntity chatBean = (MsgEntity) createBaseChat(MsgType.NOTICE_NOTMEMBER);
        return chatBean;
    }

    public MsgEntity outerWebsiteMsg(String string, WebsiteExt1Bean ext1Bean) {
        MsgEntity chatBean = (MsgEntity) createBaseChat(MsgType.OUTER_WEBSITE);
        chatBean.getMsgDefinBean().setContent(string);
        chatBean.getMsgDefinBean().setExt1(new Gson().toJson(ext1Bean));
        return chatBean;
    }

    @Override
    public MsgEntity encryptChatMsg() {
        MsgEntity chatBean = (MsgEntity) createBaseChat(MsgType.NOTICE_ENCRYPTCHAT);
        return chatBean;
    }

    @Override
    public MsgEntity clickReceiveLuckMsg(String string) {
        MsgEntity chatBean = (MsgEntity) createBaseChat(MsgType.NOTICE_CLICKRECEIVEPACKET);
        chatBean.getMsgDefinBean().setContent(string);
        return chatBean;
    }

    public abstract String headImg();

    public abstract String nickName();

    public abstract String address();
}
