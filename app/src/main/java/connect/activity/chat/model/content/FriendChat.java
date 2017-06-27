package connect.activity.chat.model.content;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.protobuf.ByteString;

import connect.database.MemoryDataManager;
import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.DaoHelper.ParamHelper;
import connect.database.green.bean.ContactEntity;
import connect.database.green.bean.ParamEntity;
import connect.im.bean.MsgType;
import connect.im.bean.Session;
import connect.im.bean.SocketACK;
import connect.im.bean.UserCookie;
import connect.im.model.ChatSendManager;
import connect.activity.chat.bean.ExtBean;
import connect.activity.chat.bean.MsgDefinBean;
import connect.activity.chat.bean.MsgEntity;
import connect.activity.chat.bean.MsgSender;
import connect.activity.chat.bean.RoomSession;
import connect.utils.TimeUtil;
import connect.utils.cryption.EncryptionUtil;
import connect.utils.cryption.SupportKeyUril;
import connect.wallet.jni.AllNativeMethod;
import protos.Connect;

/**
 * friend chat
 * Created by gtq on 2016/12/19.
 */
public class FriendChat extends NormalChat {

    private String Tag = "FriendChat";
    private ContactEntity contactEntity = null;

    /**  user Cookie */
    private UserCookie userCookie = null;
    /** friend Cookie */
    private UserCookie friendCookie = null;

    public enum EncryType {
        NORMAL,
        HALF,
        BOTH,
    }

    private EncryType encryType = EncryType.BOTH;

    public FriendChat(ContactEntity entity) {
        contactEntity = entity;

        ContactEntity dbEntity = ContactHelper.getInstance().loadFriendEntity(entity.getPub_key());
        isStranger = (dbEntity == null);
        if (dbEntity != null) {
            contactEntity = dbEntity;
        }
    }

    @Override
    public MsgEntity createBaseChat(MsgType type) {
        MsgDefinBean msgDefinBean = new MsgDefinBean();
        msgDefinBean.setType(type.type);
        msgDefinBean.setUser_name(contactEntity.getUsername());
        msgDefinBean.setSendtime(TimeUtil.getCurrentTimeInLong());
        msgDefinBean.setMessage_id(TimeUtil.timestampToMsgid());
        msgDefinBean.setPublicKey(contactEntity.getPub_key());
        msgDefinBean.setUser_id(address());
        msgDefinBean.setSenderInfoExt(new MsgSender(MemoryDataManager.getInstance().getPubKey(),
                MemoryDataManager.getInstance().getName(),
                MemoryDataManager.getInstance().getAddress(),
                MemoryDataManager.getInstance().getAvatar()));

        long burntime = RoomSession.getInstance().getBurntime();
        if (burntime > 0) {
            ExtBean extBean = new ExtBean();
            extBean.setLuck_delete(burntime);
            msgDefinBean.setExt(new Gson().toJson(extBean));
        }
        MsgEntity chatBean = new MsgEntity();
        chatBean.setMsgDefinBean(msgDefinBean);
        chatBean.setPubkey(contactEntity.getPub_key());
        chatBean.setRecAddress(address());
        chatBean.setSendstate(0);
        return chatBean;
    }

    @Override
    public void sendPushMsg(Object bean) {
        MsgDefinBean definBean = ((MsgEntity) bean).getMsgDefinBean();
        String msgStr = new Gson().toJson(definBean);

        String priKey = null;
        byte[] randomSalt = null;
        String friendKey = null;

        loadUserCookie();
        loadFriendCookie(definBean.getPublicKey());
        SupportKeyUril.EcdhExts ecdhExts = null;
        Connect.MessageData.Builder builder = Connect.MessageData.newBuilder();

        switch (encryType) {
            case NORMAL:
                priKey = MemoryDataManager.getInstance().getPriKey();
                friendKey = definBean.getPublicKey();
                ecdhExts = SupportKeyUril.EcdhExts.EMPTY;
                break;
            case HALF:
                priKey = userCookie.getPriKey();
                randomSalt = userCookie.getSalt();

                friendKey = definBean.getPublicKey();
                ecdhExts = SupportKeyUril.EcdhExts.OTHER;
                ecdhExts.setBytes(randomSalt);
                builder.setSalt(ByteString.copyFrom(randomSalt)).setChatPubKey(userCookie.getPubKey());
                break;
            case BOTH:
                priKey = userCookie.getPriKey();
                randomSalt = userCookie.getSalt();

                friendKey = friendCookie.getPubKey();
                byte[] friendSalt = friendCookie.getSalt();
                ecdhExts = SupportKeyUril.EcdhExts.OTHER;
                ecdhExts.setBytes(SupportKeyUril.xor(randomSalt, friendSalt, 64));
                builder.setSalt(ByteString.copyFrom(randomSalt)).setChatPubKey(userCookie.getPubKey()).
                        setVer(ByteString.copyFrom(friendCookie.getSalt()));
                break;
        }

        Connect.GcmData gcmData = EncryptionUtil.encodeAESGCM(ecdhExts, priKey, friendKey, msgStr.getBytes());
        builder.setCipherData(gcmData).
                setMsgId(definBean.getMessage_id()).
                setTyp(definBean.getType()).
                setReceiverAddress(((MsgEntity) bean).getRecAddress());

        Connect.MessageData messageData = builder.build();
        ChatSendManager.getInstance().sendChatAckMsg(SocketACK.SINGLE_CHAT, definBean.getPublicKey(), messageData);
    }

    @Override
    public String headImg() {
        return contactEntity.getAvatar();
    }

    @Override
    public String nickName() {
        return TextUtils.isEmpty(contactEntity.getRemark()) ? contactEntity.getUsername() : contactEntity.getRemark();
    }

    @Override
    public String address() {
        String address = contactEntity.getAddress();
        if (TextUtils.isEmpty(address)) {
            if (TextUtils.isEmpty(roomKey())) {
                address = "";
            } else {
                address = AllNativeMethod.cdGetBTCAddrFromPubKey(roomKey());
            }
        }
        return address;
    }

    @Override
    public String roomKey() {
        return contactEntity.getPub_key();
    }

    @Override
    public int roomType() {
        return 0;
    }

    public void setFriendEntity(ContactEntity friendEntity) {
        this.contactEntity = friendEntity;
    }

    public void setEncryType(EncryType encryType) {
        this.encryType = encryType;
    }

    public void setFriendCookie(UserCookie friendCookie) {
        this.friendCookie = friendCookie;
    }

    private void loadUserCookie() {
        String pubkey = MemoryDataManager.getInstance().getPubKey();
        userCookie = Session.getInstance().getUserCookie(pubkey);
        if (userCookie == null) {
            String cookieKey = "COOKIE:" + pubkey;
            ParamEntity paramEntity = ParamHelper.getInstance().likeParamEntityDESC(cookieKey);//local cookie
            if (paramEntity != null) {
                userCookie = new Gson().fromJson(paramEntity.getValue(), UserCookie.class);
            }
        }

        if (userCookie == null) {
            encryType = EncryType.NORMAL;
        }
    }

    public void loadFriendCookie(String pubkey) {
        friendCookie = Session.getInstance().getUserCookie(pubkey);
        if (friendCookie == null) {
            String cookieFriend = "COOKIE:" + pubkey;
            ParamEntity friendEntity = ParamHelper.getInstance().likeParamEntityDESC(cookieFriend);
            if (friendEntity != null) {
                friendCookie = new Gson().fromJson(friendEntity.getValue(), UserCookie.class);
            }
        }

        if (friendCookie == null) {
            encryType = EncryType.NORMAL;
        }
    }
}
