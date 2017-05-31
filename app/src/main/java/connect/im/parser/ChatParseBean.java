package connect.im.parser;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.protobuf.ByteString;

import java.nio.ByteBuffer;

import connect.db.MemoryDataManager;
import connect.db.SharedPreferenceUtil;
import connect.db.green.DaoHelper.ContactHelper;
import connect.db.green.DaoHelper.ConversionSettingHelper;
import connect.db.green.DaoHelper.MessageHelper;
import connect.db.green.DaoHelper.ParamHelper;
import connect.db.green.bean.ContactEntity;
import connect.db.green.bean.GroupEntity;
import connect.db.green.bean.ParamEntity;
import connect.im.bean.UserCookie;
import connect.im.inter.InterParse;
import connect.im.model.FailMsgsManager;
import connect.ui.activity.R;
import connect.ui.activity.chat.bean.MsgDefinBean;
import connect.ui.activity.chat.bean.MsgEntity;
import connect.ui.activity.chat.bean.RecExtBean;
import connect.ui.activity.chat.model.ChatMsgUtil;
import connect.ui.activity.chat.model.content.FriendChat;
import connect.ui.activity.chat.model.content.NormalChat;
import connect.ui.activity.home.bean.HttpRecBean;
import connect.ui.base.BaseApplication;
import connect.utils.StringUtil;
import connect.utils.TimeUtil;
import connect.utils.cryption.DecryptionUtil;
import connect.utils.cryption.SupportKeyUril;
import connect.utils.okhttp.adapter.MsgDefTypeAdapter;
import protos.Connect;

/**
 * Created by pujin on 2017/4/19.
 */

public class ChatParseBean extends InterParse {

    private String Tag = "ChatParseBean";

    private byte ackByte;
    private Connect.MessagePost messagePost;

    public ChatParseBean(byte ackByte, Connect.MessagePost messagePost) {
        super(ackByte, ByteBuffer.wrap(messagePost.toByteArray()));
        this.ackByte = ackByte;
        this.messagePost = messagePost;
    }

    @Override
    public void msgParse() throws Exception {
        switch (ackByte) {
            case 0x01://private chat
            case 0x02://burn chat
                singleChat(messagePost);
                break;
            case 0x03://invite to join in group
                inviteJoinGroup(messagePost);
                break;
            case 0x04://group chat
                groupChat(messagePost);
                break;
        }

        String msgid = messagePost.getMsgData().getMsgId();
        sendBackAck(msgid);
    }

    public void singleChat(Connect.MessagePost msgpost) {
        String friendPubKey = msgpost.getPubKey();
        String priKey = null;
        String pubkey = null;

        ContactEntity friendEntity = ContactHelper.getInstance().loadFriendEntity(friendPubKey);
        if (friendEntity == null) {
            requestFriendsByVersion();
        }

        Connect.MessageData messageData = msgpost.getMsgData();
        Connect.GcmData gcmData = messageData.getCipherData();

        SupportKeyUril.EcdhExts ecdhExts = SupportKeyUril.EcdhExts.EMPTY;
        if (TextUtils.isEmpty(messageData.getChatPubKey())) {//old protocol
            priKey = MemoryDataManager.getInstance().getPriKey();
            pubkey = friendPubKey;
        } else if (null == messageData.getVer() || messageData.getVer().size() == 0) {//half random
            priKey = MemoryDataManager.getInstance().getPriKey();

            ByteString fromSalt = messageData.getSalt();
            pubkey = messageData.getChatPubKey();
            ecdhExts = SupportKeyUril.EcdhExts.OTHER;
            ecdhExts.setBytes(fromSalt.toByteArray());
        } else {//both random
            ByteString fromSalt = messageData.getSalt();
            ByteString toSalt = messageData.getVer();

            ParamEntity toSaltEntity = ParamHelper.getInstance().likeParamEntity(StringUtil.bytesToHexString(toSalt.toByteArray()));
            if (toSaltEntity == null) {
                msgParseException(friendPubKey);
                return;
            }

            UserCookie toCookie = new Gson().fromJson(toSaltEntity.getValue(), UserCookie.class);
            priKey = toCookie.getPriKey();
            pubkey = messageData.getChatPubKey();
            ecdhExts = SupportKeyUril.EcdhExts.OTHER;
            ecdhExts.setBytes(SupportKeyUril.xor(fromSalt.toByteArray(), toSalt.toByteArray(), 64));
        }

        byte[] contents = DecryptionUtil.decodeAESGCM(ecdhExts, priKey, pubkey, gcmData);
        if (contents.length > 10) {
            parseToGsonMsg(msgpost.getPubKey(), 0, contents);
        } else {
            msgParseException(friendPubKey);
        }
    }

    /**
     * group chat
     *
     * @param msgpost
     */
    protected void groupChat(Connect.MessagePost msgpost) {
        String pubkey = msgpost.getMsgData().getReceiverAddress();
        GroupEntity groupEntity = ContactHelper.getInstance().loadGroupEntity(pubkey);
        Connect.GcmData gcmData = msgpost.getMsgData().getCipherData();

        if (groupEntity == null || TextUtils.isEmpty(groupEntity.getEcdh_key())) {//group backup
            FailMsgsManager.getInstance().insertReceiveMsg(pubkey, msgpost.getMsgData().getMsgId(), gcmData);
            HttpRecBean.sendHttpRecMsg(HttpRecBean.HttpRecType.GroupInfo, pubkey);
        } else {
            byte[] contents = DecryptionUtil.decodeAESGCM(SupportKeyUril.EcdhExts.NONE, StringUtil.hexStringToBytes(groupEntity.getEcdh_key()), gcmData);
            if (contents.length < 10) {
                HttpRecBean.sendHttpRecMsg(HttpRecBean.HttpRecType.GroupInfo, pubkey);
            } else {
                parseToGsonMsg(pubkey, 1, contents);
            }
        }
    }

    /**
     * invite to join group
     *
     * @param msgpost
     * @throws Exception
     */
    protected void inviteJoinGroup(Connect.MessagePost msgpost) throws Exception {
        Connect.GcmData gcmData = msgpost.getMsgData().getCipherData();
        Connect.StructData structData = DecryptionUtil.decodeAESGCMStructData(SupportKeyUril.EcdhExts.EMPTY, MemoryDataManager.getInstance().getPriKey(),
                msgpost.getPubKey(), gcmData);

        Connect.CreateGroupMessage groupMessage = Connect.CreateGroupMessage.parseFrom(structData.getPlainData());
        HttpRecBean.sendHttpRecMsg(HttpRecBean.HttpRecType.GroupInfo, groupMessage.getIdentifier());
    }

    /**
     * MsgDefinBean
     *
     * @param pubkey
     * @param contents
     */
    protected void parseToGsonMsg(String pubkey, int roomtype, byte[] contents) {
        String content = new String(contents);
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(MsgDefinBean.class, new MsgDefTypeAdapter());
        MsgDefinBean definBean = gsonBuilder.create().fromJson(content, MsgDefinBean.class);

        switch (definBean.getType()) {
            case 11://burn state
                ConversionSettingHelper.getInstance().updateBurnTime(pubkey, Long.parseLong(definBean.getContent()));
                break;
            case 12://friend read the burn
                MessageHelper.getInstance().updateBurnMsg(definBean.getContent(), TimeUtil.getCurrentTimeInLong());
                break;
            default:
                MessageHelper.getInstance().insertFromMsg(pubkey, definBean);
                break;
        }
        broadMsg(pubkey, roomtype, definBean);
    }

    protected void msgParseException(String pubkey) {
        NormalChat normalChat = ChatMsgUtil.loadBaseChat(pubkey);
        if (normalChat != null) {
            String showTxt = BaseApplication.getInstance().getString(R.string.Chat_Notice_New_Message);
            MsgEntity msgEntity = normalChat.noticeMsg(showTxt);
            normalChat.updateRoomMsg(null, showTxt, msgEntity.getMsgDefinBean().getSendtime(),-1,true);
            MessageHelper.getInstance().insertFromMsg(normalChat.roomKey(), msgEntity.getMsgDefinBean());
            RecExtBean.sendRecExtMsg(RecExtBean.ExtType.MESSAGE_RECEIVE, normalChat.roomKey(), msgEntity);
        }
    }

    /**
     * broad message
     */
    protected void broadMsg(String pubkey, int type, MsgDefinBean definBean) {
        MsgEntity msgEntity = new MsgEntity();
        msgEntity.setMsgDefinBean(definBean);
        msgEntity.setSendstate(0);
        msgEntity.setPubkey(pubkey);
        RecExtBean.sendRecExtMsg(RecExtBean.ExtType.MESSAGE_RECEIVE,pubkey,msgEntity);
        pushNoticeMsg(pubkey, type, msgEntity);
    }
}
