package connect.ui.activity.chat.model.content;

import android.text.TextUtils;

import com.google.gson.Gson;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import connect.db.green.DaoHelper.ConversionHelper;
import connect.db.green.DaoHelper.MessageHelper;
import connect.db.green.bean.ContactEntity;
import connect.db.green.bean.ConversionEntity;
import connect.db.green.bean.MessageEntity;
import connect.im.bean.MsgType;
import connect.ui.activity.chat.bean.BaseEntity;
import connect.ui.activity.chat.bean.GatherBean;
import connect.ui.activity.chat.bean.MessageExtEntity;
import connect.ui.activity.chat.bean.MsgDefinBean;
import connect.ui.activity.chat.bean.MsgEntity;
import connect.ui.activity.chat.bean.WebsiteExt1Bean;
import connect.ui.activity.home.bean.MsgFragmReceiver;
import connect.ui.activity.locmap.bean.GeoAddressBean;
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

    public synchronized void updateRoomMsg(String draft, String showText, long msgtime) {
        updateRoomMsg(draft, showText, msgtime, -1);
    }

    public void updateRoomMsg(String draft, String showText, long msgtime, int at) {
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
        roomEntity.setDraft(draft);
        roomEntity.setType(roomType());
        roomEntity.setContent(showText);
        roomEntity.setLast_time(msgtime);
        roomEntity.setStranger(isStranger ? 1 : 0);
        roomEntity.setUnread_count(0);
        if (at == 0 || at == 1) {
            roomEntity.setNotice(at);
        }

        ConversionHelper.getInstance().insertRoomEntity(roomEntity);
        MsgFragmReceiver.refreshRoom(MsgFragmReceiver.FragRecType.ALL);
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

    public List<BaseEntity> loadChatEntities() {
        MsgDefinBean definBean = null;
        MsgEntity chatBean = null;
        List<BaseEntity> localBeans = new ArrayList();
        List<MessageExtEntity> detailEntities = MessageHelper.getInstance().loadFirstMsgEntities(roomKey());
        byte[] localHashKeys = SupportKeyUril.localHashKey().getBytes();
        for (MessageExtEntity detailEntity : detailEntities) {
            try {
                Connect.GcmData gcmData = null;
                gcmData = Connect.GcmData.parseFrom(StringUtil.hexStringToBytes(detailEntity.getContent()));
                byte[] contents = DecryptionUtil.decodeAESGCM(SupportKeyUril.EcdhExts.NONE, localHashKeys, gcmData);
                definBean = new Gson().fromJson(new String(contents), MsgDefinBean.class);

                chatBean = new MsgEntity();
                chatBean.setPubkey(roomKey());
                chatBean.setMsgDefinBean(definBean);
                chatBean.setMsgid(definBean.getMessage_id());
                chatBean.setSendstate(detailEntity.getSend_status());
                chatBean.setRecAddress(address());
                chatBean.setHashid(detailEntity.getHashid());
                chatBean.setTransStatus(detailEntity.getTransStatus());
                chatBean.setPayCount(detailEntity.getPayCount());
                chatBean.setCrowdCount(detailEntity.getCrowdCount());
                chatBean.setReadstate(detailEntity.getState());

                long burnstart = (null == detailEntity.getSnap_time()) ? 0 : detailEntity.getSnap_time();
                chatBean.setBurnstarttime(burnstart);

                localBeans.add(chatBean);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return localBeans;
    }

    public List<BaseEntity> loadMoreEntities(long firstmsgtime) {
        MsgDefinBean definBean = null;
        MsgEntity chatBean = null;
        List<BaseEntity> localBeans = new ArrayList();
        List<MessageExtEntity> detailEntities = MessageHelper.getInstance().loadMoreMsgEntities(roomKey(), firstmsgtime);
        byte[] localHashKeys = SupportKeyUril.localHashKey().getBytes();
        for (MessageExtEntity detailEntity : detailEntities) {
            try {
                Connect.GcmData gcmData = null;
                gcmData = Connect.GcmData.parseFrom(StringUtil.hexStringToBytes(detailEntity.getContent()));
                byte[] contents = DecryptionUtil.decodeAESGCM(SupportKeyUril.EcdhExts.NONE, localHashKeys, gcmData);
                definBean = new Gson().fromJson(new String(contents), MsgDefinBean.class);

                chatBean = new MsgEntity();
                chatBean.setPubkey(roomKey());
                chatBean.setMsgDefinBean(definBean);
                chatBean.setSendstate(detailEntity.getSend_status());
                chatBean.setRecAddress(address());

                long burnstart = (null == detailEntity.getSnap_time()) ? 0 : detailEntity.getSnap_time();
                chatBean.setBurnstarttime(burnstart);
                chatBean.setHashid(detailEntity.getHashid());
                chatBean.setTransStatus(detailEntity.getTransStatus());
                chatBean.setPayCount(detailEntity.getPayCount());
                chatBean.setCrowdCount(detailEntity.getCrowdCount());

                localBeans.add(chatBean);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return localBeans;
    }

    /**
     * Msgid
     *
     * @param msgid
     * @return
     */
    public BaseEntity loadEntityByMsgid(String msgid) throws Exception {
        MessageEntity messageEntity = MessageHelper.getInstance().loadMsgByMsgid(msgid);
        if (messageEntity == null) {
            return null;
        }

        byte[] localHashKeys = SupportKeyUril.localHashKey().getBytes();
        Connect.GcmData gcmData = Connect.GcmData.parseFrom(StringUtil.hexStringToBytes(messageEntity.getContent()));
        byte[] contents = DecryptionUtil.decodeAESGCM(SupportKeyUril.EcdhExts.NONE, localHashKeys, gcmData);
        MsgDefinBean definBean = new Gson().fromJson(new String(contents), MsgDefinBean.class);

        MsgEntity chatBean = new MsgEntity();
        chatBean.setPubkey(roomKey());
        chatBean.setMsgDefinBean(definBean);
        chatBean.setSendstate(messageEntity.getSend_status());
        chatBean.setRecAddress(address());

        long burnstart = (null == messageEntity.getSnap_time()) ? 0 : messageEntity.getSnap_time();
        chatBean.setBurnstarttime(burnstart);
        return chatBean;
    }
}
