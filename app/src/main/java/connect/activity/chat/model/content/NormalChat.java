package connect.activity.chat.model.content;

import connect.activity.chat.bean.MsgExtEntity;
import connect.im.bean.MsgType;
import protos.Connect;

/**
 * public methods to extract
 * Created by gtq on 2016/12/19.
 */
public abstract class NormalChat extends BaseChat {

    @Override
    public void updateRoomMsg(String draft, String showText, long msgtime) {
        super.updateRoomMsg(draft, showText, msgtime);
    }

    @Override
    public MsgExtEntity txtMsg(String string) {
        MsgExtEntity msgExtEntity = (MsgExtEntity) createBaseChat(MsgType.Text);
        Connect.TextMessage.Builder builder = Connect.TextMessage.newBuilder()
                .setContent(string);

        if (chatType() == 0) {
            long destructtime = destructReceipt();
            if (destructtime > 0) {
                builder.setSnapTime(destructtime);
            }
        }
        msgExtEntity.setContents(builder.build().toByteArray());
        return msgExtEntity;
    }

    @Override
    public MsgExtEntity photoMsg(String thum, String url, String filesize, int width, int height) {
        MsgExtEntity msgExtEntity = (MsgExtEntity) createBaseChat(MsgType.Photo);
        Connect.PhotoMessage.Builder builder = Connect.PhotoMessage.newBuilder()
                .setThum(thum)
                .setUrl(url)
                .setSize(filesize)
                .setImageWidth(width)
                .setImageHeight(height);

        if (chatType() == 0) {
            long destructtime = destructReceipt();
            if (destructtime > 0) {
                builder.setSnapTime(destructtime);
            }
        }
        msgExtEntity.setContents(builder.build().toByteArray());
        return msgExtEntity;
    }

    @Override
    public MsgExtEntity voiceMsg(String string, int length) {
        MsgExtEntity msgExtEntity = (MsgExtEntity) createBaseChat(MsgType.Voice);
        Connect.VoiceMessage.Builder builder = Connect.VoiceMessage.newBuilder()
                .setUrl(string)
                .setTimeLength(length);

        if (chatType() == 0) {
            long destructtime = destructReceipt();
            if (destructtime > 0) {
                builder.setSnapTime(destructtime);
            }
        }
        msgExtEntity.setContents(builder.build().toByteArray());
        return msgExtEntity;
    }

    @Override
    public MsgExtEntity videoMsg(String thum, String url, int length, int filesize, int width, int height) {
        MsgExtEntity msgExtEntity = (MsgExtEntity) createBaseChat(MsgType.Video);
        Connect.VideoMessage.Builder builder = Connect.VideoMessage.newBuilder()
                .setCover(thum)
                .setUrl(url)
                .setTimeLength(length)
                .setSize(filesize)
                .setImageWidth(width)
                .setImageHeight(height);

        if (chatType() == 0) {
            long destructtime = destructReceipt();
            if (destructtime > 0) {
                builder.setSnapTime(destructtime);
            }
        }
        msgExtEntity.setContents(builder.build().toByteArray());
        return msgExtEntity;
    }

    @Override
    public MsgExtEntity emotionMsg(String string) {
        MsgExtEntity msgExtEntity = (MsgExtEntity) createBaseChat(MsgType.Emotion);
        Connect.EmotionMessage.Builder builder = Connect.EmotionMessage.newBuilder()
                .setContent(string);

        if (chatType() == 0) {
            long destructtime = destructReceipt();
            if (destructtime > 0) {
                builder.setSnapTime(destructtime);
            }
        }
        msgExtEntity.setContents(builder.build().toByteArray());
        return msgExtEntity;
    }

    @Override
    public MsgExtEntity cardMsg(String pubkey, String name, String avatar) {
        MsgExtEntity msgExtEntity = (MsgExtEntity) createBaseChat(MsgType.Name_Card);
        Connect.CardMessage.Builder builder = Connect.CardMessage.newBuilder()
                .setUid(pubkey)
                .setUsername(name)
                .setAvatar(avatar);

        msgExtEntity.setContents(builder.build().toByteArray());
        return msgExtEntity;
    }

    @Override
    public MsgExtEntity destructMsg(int time) {
        MsgExtEntity msgExtEntity = (MsgExtEntity) createBaseChat(MsgType.Self_destruct_Notice);
        Connect.DestructMessage.Builder builder = Connect.DestructMessage.newBuilder()
                .setTime(time <= 0 ? -1 : time);

        msgExtEntity.setContents(builder.build().toByteArray());
        return msgExtEntity;
    }

    @Override
    public MsgExtEntity receiptMsg(String messageid) {
        MsgExtEntity msgExtEntity = (MsgExtEntity) createBaseChat(MsgType.Self_destruct_Receipt);
        Connect.ReadReceiptMessage.Builder builder = Connect.ReadReceiptMessage.newBuilder()
                .setMessageId(messageid);

        msgExtEntity.setContents(builder.build().toByteArray());
        return msgExtEntity;
    }

    @Override
    public MsgExtEntity paymentMsg(int paymenttype,String hashid, long amount, int membersize, String tips) {
        MsgExtEntity msgExtEntity = (MsgExtEntity) createBaseChat(MsgType.Request_Payment);
        Connect.PaymentMessage.Builder builder = Connect.PaymentMessage.newBuilder()
                .setPaymentType(paymenttype)
                .setHashId(hashid)
                .setAmount(amount)
                .setMemberSize(membersize)
                .setTips(tips);

        msgExtEntity.setContents(builder.build().toByteArray());
        return msgExtEntity;
    }

    @Override
    public MsgExtEntity transferMsg(int type, String hashid, long amout, String tips) {
        MsgExtEntity msgExtEntity = (MsgExtEntity) createBaseChat(MsgType.Transfer);
        Connect.TransferMessage.Builder builder = Connect.TransferMessage.newBuilder()
                .setTransferType(type)
                .setHashId(hashid)
                .setAmount(amout)
                .setTips(tips);

        msgExtEntity.setContents(builder.build().toByteArray());
        return msgExtEntity;
    }

    @Override
    public MsgExtEntity locationMsg(float latitude, float longitude, String address, String thum, int width, int height) {
        MsgExtEntity msgExtEntity = (MsgExtEntity) createBaseChat(MsgType.Location);
        Connect.LocationMessage.Builder builder = Connect.LocationMessage.newBuilder()
                .setLatitude(latitude)
                .setLongitude(longitude)
                .setAddress(address)
                .setScreenShot(thum)
                .setImageWidth(width)
                .setImageHeight(height);

        msgExtEntity.setContents(builder.build().toByteArray());
        return msgExtEntity;
    }

    @Override
    public MsgExtEntity luckPacketMsg(int type, String hashid, String tips, long amount) {
        MsgExtEntity msgExtEntity = (MsgExtEntity) createBaseChat(MsgType.Lucky_Packet);
        Connect.LuckPacketMessage.Builder builder = Connect.LuckPacketMessage.newBuilder()
                .setLuckyType(type)
                .setHashId(hashid)
                .setAmount(amount)
                .setTips(hashid);

        msgExtEntity.setContents(builder.build().toByteArray());
        return msgExtEntity;
    }

    @Override
    public MsgExtEntity noticeMsg(String string) {
        MsgExtEntity msgExtEntity = (MsgExtEntity) createBaseChat(MsgType.NOTICE);
        Connect.NotifyMessage.Builder builder = Connect.NotifyMessage.newBuilder()
                .setContent(string);

        msgExtEntity.setContents(builder.build().toByteArray());
        return msgExtEntity;
    }

    @Override
    public MsgExtEntity outerWebsiteMsg(String url, String title, String subtitle, String img) {
        MsgExtEntity msgExtEntity = (MsgExtEntity) createBaseChat(MsgType.OUTER_WEBSITE);
        Connect.WebsiteMessage.Builder builder = Connect.WebsiteMessage.newBuilder()
                .setUrl(url)
                .setTitle(title)
                .setSubtitle(subtitle)
                .setImg(img);

        msgExtEntity.setContents(builder.build().toByteArray());
        return msgExtEntity;
    }

    @Override
    public MsgExtEntity encryptChatMsg() {
        MsgExtEntity msgExtEntity = (MsgExtEntity) createBaseChat(MsgType.NOTICE_ENCRYPTCHAT);
        return msgExtEntity;
    }

    @Override
    public MsgExtEntity clickReceiveLuckMsg(String string) {
        MsgExtEntity msgExtEntity = (MsgExtEntity) createBaseChat(MsgType.NOTICE_CLICKRECEIVEPACKET);
        Connect.NotifyMessage.Builder builder = Connect.NotifyMessage.newBuilder()
                .setContent(string);

        msgExtEntity.setContents(builder.build().toByteArray());
        return msgExtEntity;
    }

    public abstract String headImg();

    public abstract String nickName();

    public abstract String identify();

    public abstract String address();

    public abstract long destructReceipt();

    public abstract Connect.MessageUserInfo senderInfo();
}
