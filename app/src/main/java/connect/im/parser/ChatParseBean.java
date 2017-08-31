package connect.im.parser;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import java.nio.ByteBuffer;

import connect.activity.base.BaseApplication;
import connect.activity.chat.bean.MsgExtEntity;
import connect.activity.chat.bean.RecExtBean;
import connect.activity.chat.model.content.FriendChat;
import connect.activity.chat.model.content.GroupChat;
import connect.activity.chat.model.content.NormalChat;
import connect.activity.home.bean.HttpRecBean;
import connect.database.MemoryDataManager;
import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.DaoHelper.ConversionSettingHelper;
import connect.database.green.DaoHelper.MessageHelper;
import connect.database.green.DaoHelper.ParamHelper;
import connect.database.green.bean.ContactEntity;
import connect.database.green.bean.GroupEntity;
import connect.database.green.bean.ParamEntity;
import connect.im.bean.MsgType;
import connect.im.bean.UserCookie;
import connect.im.inter.InterParse;
import connect.im.model.FailMsgsManager;
import connect.ui.activity.R;
import connect.utils.StringUtil;
import connect.utils.TimeUtil;
import connect.utils.cryption.DecryptionUtil;
import connect.utils.cryption.SupportKeyUril;
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
    public synchronized void msgParse() throws Exception {
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
    }

    public synchronized void singleChat(Connect.MessagePost msgpost) throws Exception {
        Connect.MessageData messageData = msgpost.getMsgData();
        Connect.ChatSession chatSession = messageData.getChatSession();
        Connect.ChatMessage chatMessage = messageData.getChatMsg();

        String friendPubKey = msgpost.getPubKey();
        String priKey = null;
        String pubkey = null;

        ContactEntity friendEntity = ContactHelper.getInstance().loadFriendEntity(chatMessage.getFrom());
        if (friendEntity == null) {
            requestFriendsByVersion();
        }

        SupportKeyUril.EcdhExts ecdhExts = SupportKeyUril.EcdhExts.EMPTY;
        if (TextUtils.isEmpty(chatSession.getPubKey())) {//old protocol
            priKey = MemoryDataManager.getInstance().getPriKey();
            pubkey = friendPubKey;
        } else if (null == chatSession.getVer() || chatSession.getVer().size() == 0) {//half random
            priKey = MemoryDataManager.getInstance().getPriKey();

            ByteString fromSalt = chatSession.getSalt();
            pubkey = chatSession.getPubKey();
            ecdhExts = SupportKeyUril.EcdhExts.OTHER;
            ecdhExts.setBytes(fromSalt.toByteArray());
        } else {//both random
            ByteString fromSalt = chatSession.getSalt();
            ByteString toSalt = chatSession.getVer();

            ParamEntity toSaltEntity = ParamHelper.getInstance().likeParamEntity(StringUtil.bytesToHexString(toSalt.toByteArray()));
            if (toSaltEntity == null) {
                return;
            }

            UserCookie toCookie = new Gson().fromJson(toSaltEntity.getValue(), UserCookie.class);
            priKey = toCookie.getPriKey();
            pubkey = chatSession.getPubKey();
            ecdhExts = SupportKeyUril.EcdhExts.OTHER;
            ecdhExts.setBytes(SupportKeyUril.xor(fromSalt.toByteArray(), toSalt.toByteArray()));
        }

        byte[] contents = DecryptionUtil.decodeAESGCM(ecdhExts, priKey, pubkey, messageData.getChatMsg().getCipherData());
        if (contents.length < 3) {
            ContactEntity contactEntity = ContactHelper.getInstance().loadFriendEntity(chatMessage.getFrom());
            if (contactEntity != null) {
                NormalChat normalChat = new FriendChat(contactEntity);
                String showTxt = BaseApplication.getInstance().getString(R.string.Chat_Notice_New_Message);
                MsgExtEntity msgExtEntity = normalChat.noticeMsg(showTxt);

                FriendChat friendChat = new FriendChat(friendEntity);
                friendChat.updateRoomMsg(null, showTxt, chatMessage.getMsgTime(), -1, 1, false);

                MessageHelper.getInstance().insertMsgExtEntity(msgExtEntity);
                RecExtBean.getInstance().sendEvent(RecExtBean.ExtType.MESSAGE_RECEIVE, normalChat.chatKey(), msgExtEntity);
            }
        } else {
            MsgExtEntity msgExtEntity = MessageHelper.getInstance().insertMessageEntity(chatMessage.getMsgId(), chatMessage.getFrom(),
                    chatMessage.getChatType().getNumber(), chatMessage.getMsgType(), chatMessage.getFrom(),
                    chatMessage.getTo(), contents, chatMessage.getMsgTime(), 1);

            MsgType msgType = MsgType.toMsgType(chatMessage.getMsgType());
            switch (msgType) {
                case Self_destruct_Notice:
                    MessageHelper.getInstance().insertMessageEntity(msgExtEntity);

                    Connect.DestructMessage destructMessage = Connect.DestructMessage.parseFrom(contents);
                    ConversionSettingHelper.getInstance().updateBurnTime(pubkey, destructMessage.getTime());
                    break;
                case Self_destruct_Receipt:
                    Connect.ReadReceiptMessage readReceiptMessage = Connect.ReadReceiptMessage.parseFrom(contents);
                    MessageHelper.getInstance().updateBurnMsg(readReceiptMessage.getMessageId(), TimeUtil.getCurrentTimeInLong());
                    break;
                default:
                    MessageHelper.getInstance().insertMessageEntity(msgExtEntity);

                    FriendChat friendChat = new FriendChat(friendEntity);
                    friendChat.updateRoomMsg(null, msgExtEntity.showContent(), chatMessage.getMsgTime(), -1, 1, false);
                    break;
            }

            RecExtBean.getInstance().sendEvent(RecExtBean.ExtType.MESSAGE_RECEIVE, msgExtEntity.getMessage_from(), msgExtEntity);
            pushNoticeMsg(msgExtEntity.getMessage_from(), Connect.ChatType.PRIVATE_VALUE, msgExtEntity.showContent());
        }
    }

    /**
     * group chat
     *
     * @param msgpost
     */
    protected synchronized void groupChat(Connect.MessagePost msgpost) {
        Connect.MessageData messageData = msgpost.getMsgData();
        Connect.ChatMessage chatMessage = messageData.getChatMsg();

        String groupIdentify = chatMessage.getTo();
        GroupEntity groupEntity = ContactHelper.getInstance().loadGroupEntity(groupIdentify);
        Connect.GcmData gcmData = chatMessage.getCipherData();

        if (groupEntity == null || TextUtils.isEmpty(groupEntity.getEcdh_key())) {//group backup
            FailMsgsManager.getInstance().insertReceiveMsg(groupIdentify, chatMessage.getMsgId(), msgpost);
            HttpRecBean.sendHttpRecMsg(HttpRecBean.HttpRecType.GroupInfo, groupIdentify);
        } else {
            byte[] contents = DecryptionUtil.decodeAESGCM(SupportKeyUril.EcdhExts.NONE, StringUtil.hexStringToBytes(groupEntity.getEcdh_key()), gcmData);
            if (contents.length < 3) {
                HttpRecBean.sendHttpRecMsg(HttpRecBean.HttpRecType.GroupInfo, groupIdentify);
            } else {
                MsgExtEntity msgExtEntity = MessageHelper.getInstance().insertMessageEntity(chatMessage.getMsgId(), groupIdentify,
                        chatMessage.getChatType().getNumber(), chatMessage.getMsgType(), chatMessage.getFrom(),
                        chatMessage.getTo(), contents, chatMessage.getMsgTime(), 1);
                MessageHelper.getInstance().insertMessageEntity(msgExtEntity);

                GroupChat groupChat = new GroupChat(groupEntity);
                groupChat.updateRoomMsg(null, msgExtEntity.showContent(), chatMessage.getMsgTime(), -1, 1, false);

                RecExtBean.getInstance().sendEvent(RecExtBean.ExtType.MESSAGE_RECEIVE, groupIdentify, msgExtEntity);

                String content = msgExtEntity.showContent();
                String myaddress = MemoryDataManager.getInstance().getAddress();
                if (chatMessage.getMsgType() == MsgType.Text.type) {
                    try {
                        Connect.TextMessage textMessage = Connect.TextMessage.parseFrom(contents);
                        if (textMessage.getAtAddressesList().lastIndexOf(myaddress) != -1) {
                            content = BaseApplication.getInstance().getBaseContext().getString(R.string.Chat_Someone_note_me);
                        }
                    } catch (InvalidProtocolBufferException e) {
                        e.printStackTrace();
                    }
                }
                pushNoticeMsg(groupIdentify, Connect.ChatType.GROUPCHAT_VALUE, content);
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
        String prikey = MemoryDataManager.getInstance().getPriKey();
        Connect.GcmData gcmData = msgpost.getMsgData().getChatMsg().getCipherData();
        Connect.StructData structData = DecryptionUtil.decodeAESGCMStructData(SupportKeyUril.EcdhExts.EMPTY,
                prikey, msgpost.getPubKey(), gcmData);

        Connect.CreateGroupMessage groupMessage = Connect.CreateGroupMessage.parseFrom(structData.getPlainData());
        HttpRecBean.sendHttpRecMsg(HttpRecBean.HttpRecType.GroupInfo, groupMessage.getIdentifier());
    }
}
