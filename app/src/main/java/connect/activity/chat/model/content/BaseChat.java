package connect.activity.chat.model.content;
import android.text.TextUtils;
import java.io.Serializable;
import java.util.List;
import connect.activity.chat.bean.GatherBean;
import connect.activity.chat.bean.GeoAddressBean;
import connect.activity.chat.bean.MsgExtEntity;
import connect.activity.chat.bean.WebsiteExt1Bean;
import connect.activity.home.bean.MsgFragmReceiver;
import connect.database.green.DaoHelper.ConversionHelper;
import connect.database.green.DaoHelper.MessageHelper;
import connect.database.green.bean.ContactEntity;
import connect.database.green.bean.ConversionEntity;
import connect.database.green.bean.MessageEntity;
import connect.im.bean.MsgType;
import connect.utils.StringUtil;
import connect.utils.cryption.DecryptionUtil;
import connect.utils.cryption.SupportKeyUril;
import protos.Connect;

/**
 * Created by pujin on 2017/1/19.
 */

public abstract class BaseChat<T> implements Serializable {
    private static String Tag = "BaseChat";

    protected boolean isStranger = false;

    public abstract String headImg();

    public abstract String nickName();

    public abstract String address();

    public abstract T txtMsg(String string);

    public abstract T photoMsg(String string, String ext1);

    public abstract T videoMsg(String string, int length, String ext1);

    public abstract T emotionMsg(String string);

    public abstract T voiceMsg(String string, int size, String ext1);

    public abstract T transferMsg(String hashid, long amout, String note, int type);

    public abstract T locationMsg(String address, GeoAddressBean location);

    public abstract T luckPacketMsg(String string, String tips, int type);

    public abstract T noticeMsg(String string);

    public abstract T cardMsg(ContactEntity entity);

    public abstract T destructMsg(long time);

    public abstract T receiptMsg(String string);

    public abstract T paymentMsg(GatherBean bean);

    public abstract T outerWebsiteMsg(String string, WebsiteExt1Bean bean);

    public abstract T encryptChatMsg();

    public abstract T clickReceiveLuckMsg(String string);

    public abstract T createBaseChat(MsgType type);

    public void updateRoomMsg(String draft, String showText, long msgtime) {
        updateRoomMsg(draft, showText, msgtime, -1);
    }

    public void updateRoomMsg(String draft, String showText, long msgtime, int at) {
        updateRoomMsg(draft, showText, msgtime, at, false);
    }

    public void updateRoomMsg(String draft, String showText, long msgtime, int at, boolean newmsg) {
        updateRoomMsg(draft,showText,msgtime,at,newmsg,true);
    }

    public void updateRoomMsg(String draft, String showText, long msgtime, int at, boolean newmsg,boolean broad) {
        if (TextUtils.isEmpty(roomKey())) {
            return;
        }

        ConversionEntity roomEntity = ConversionHelper.getInstance().loadRoomEnitity(roomKey());
        if (roomEntity == null) {
            roomEntity = new ConversionEntity();
            roomEntity.setIdentifier(roomKey());
        }

        roomEntity.setName(nickName());
        roomEntity.setAvatar(headImg());
        roomEntity.setType(roomType());
        if (!TextUtils.isEmpty(showText)) {
            roomEntity.setContent(showText);
        }
        if (msgtime != 0) {
            roomEntity.setLast_time(msgtime);
        }
        roomEntity.setStranger(isStranger ? 1 : 0);

        int unread = (null == roomEntity.getUnread_count()) ? 0 : roomEntity.getUnread_count();
        roomEntity.setUnread_count(newmsg ? ++unread : 0);
        if (draft != null) {
            roomEntity.setDraft(draft);
        }
        if (at == 0 || at == 1) {
            roomEntity.setNotice(at);
        }

        ConversionHelper.getInstance().insertRoomEntity(roomEntity);
        if (broad) {
            MsgFragmReceiver.refreshRoom();
        }
    }

    public abstract void sendPushMsg(T bean);

    /** Roomkey */
    public abstract String roomKey();

    /** RoomType */
    public abstract int roomType();

    public boolean isStranger() {
        return isStranger;
    }

    public void setStranger(boolean stranger) {
        isStranger = stranger;
    }

    public List<MsgExtEntity> loadMoreEntities(long firstmsgtime) {
        List<MsgExtEntity> detailEntities = MessageHelper.getInstance().loadMoreMsgEntities(roomKey(), firstmsgtime);
        byte[] localHashKeys = SupportKeyUril.localHashKey().getBytes();
        for (MsgExtEntity detailEntity : detailEntities) {
            try {
                Connect.GcmData gcmData = Connect.GcmData.parseFrom(StringUtil.hexStringToBytes(detailEntity.getContent()));
                byte[] contents = DecryptionUtil.decodeAESGCM(SupportKeyUril.EcdhExts.NONE, localHashKeys, gcmData);
                detailEntity.setContents(contents);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return detailEntities;
    }

    /**
     * Msgid
     *
     * @param msgid
     * @return
     */
    public MsgExtEntity loadEntityByMsgid(String msgid) throws Exception {
        MessageEntity messageEntity = MessageHelper.getInstance().loadMsgByMsgid(msgid);
        if (messageEntity == null) {
            return null;
        }

        byte[] localHashKeys = SupportKeyUril.localHashKey().getBytes();
        Connect.GcmData gcmData = Connect.GcmData.parseFrom(StringUtil.hexStringToBytes(messageEntity.getContent()));
        byte[] contents = DecryptionUtil.decodeAESGCM(SupportKeyUril.EcdhExts.NONE, localHashKeys, gcmData);

        MsgExtEntity msgExtEntity = messageEntity.transToExtEntity();
        msgExtEntity.setContents(contents);
        return msgExtEntity;
    }
}
