package connect.activity.chat.model.content;

import android.text.TextUtils;

import java.io.Serializable;
import java.util.List;

import connect.activity.chat.bean.MsgExtEntity;
import connect.activity.home.bean.ConversationAction;
import connect.database.green.DaoHelper.ConversionHelper;
import connect.database.green.DaoHelper.MessageHelper;
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

    public abstract T photoMsg(String thum, String url, String filesize, int width, int height);

    public abstract T voiceMsg(String string, int length);

    public abstract T videoMsg(String thum,String url, int length,int filesize,int width,int height);

    public abstract T emotionMsg(String string);

    public abstract T cardMsg(String pubkey, String name, String avatar);

    public abstract T locationMsg(float latitude,float longitude,String address,String thum,int width,int height);

    public abstract T noticeMsg(String string);

    public abstract T destructMsg(int time);

    public abstract T receiptMsg(String messageid);

    /**
     * @param type 0:private  1:group  2:outer
     * @param hashid
     * @param amout
     * @param tips
     * @return
     */
    public abstract T transferMsg(int type, String hashid, long amout, String tips);

    /**
     * @param type 0:inner 1:outer 2:system
     * @param hashid
     * @param tips
     * @param amount
     * @return
     */
    public abstract T luckPacketMsg(int type, String hashid, String tips, long amount);

    /**
     * @param paymenttype 0: private 1:crowding
     * @param hashid
     * @param amount
     * @param membersize
     * @param tips
     * @return
     */
    public abstract T paymentMsg(int paymenttype,String hashid, long amount, int membersize, String tips);

    public abstract T outerWebsiteMsg(String url, String title, String subtitle, String img);

    public abstract T encryptChatMsg();

    public abstract T clickReceiveLuckMsg(String string);

    public abstract T createBaseChat(MsgType type);

    public void updateRoomMsg(String draft, String showText, long msgtime) {
        updateRoomMsg(draft, showText, msgtime, -1);
    }

    public void updateRoomMsg(String draft, String showText, long msgtime, int at) {
        updateRoomMsg(draft, showText, msgtime, at, 0);
    }

    public void updateRoomMsg(String draft, String showText, long msgtime, int at, int newmsg) {
        updateRoomMsg(draft,showText,msgtime,at,newmsg,true);
    }

    public void updateRoomMsg(String draft, String showText, long msgtime, int at, int newmsg, boolean broad) {
        if (TextUtils.isEmpty(chatKey())) {
            return;
        }

        ConversionEntity conversionEntity = ConversionHelper.getInstance().loadRoomEnitity(chatKey());
        if (conversionEntity == null) {
            conversionEntity = new ConversionEntity();
            conversionEntity.setIdentifier(chatKey());
        }

        conversionEntity.setName(nickName());
        conversionEntity.setAvatar(headImg());
        conversionEntity.setType(chatType());
        if (!TextUtils.isEmpty(showText)) {
            conversionEntity.setContent(showText);
        }
        if (msgtime > 0) {
            conversionEntity.setLast_time(msgtime);
        }
        conversionEntity.setStranger(isStranger ? 1 : 0);


        if (newmsg == 0) {
            conversionEntity.setUnread_count(0);
        } else if (newmsg > 0) {
            int unread = (null == conversionEntity.getUnread_count()) ? 1 : 1 + conversionEntity.getUnread_count();
            conversionEntity.setUnread_count(unread);
        }

        if (draft != null) {
            conversionEntity.setDraft(draft);
        }
        if (at >= 0) {
            conversionEntity.setNotice(at);
        }

        ConversionHelper.getInstance().insertRoomEntity(conversionEntity);
        if (broad) {
            ConversationAction.conversationAction.sendEvent();
        }
    }

    public abstract void sendPushMsg(MsgExtEntity msgExtEntity);

    public abstract String chatKey();

    public abstract int chatType();

    public boolean isStranger() {
        return isStranger;
    }

    public void setStranger(boolean stranger) {
        isStranger = stranger;
    }

    public List<MsgExtEntity> loadMoreEntities(long firstmsgtime) {
        List<MsgExtEntity> detailEntities = MessageHelper.getInstance().loadMoreMsgEntities(chatKey(), firstmsgtime);
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
