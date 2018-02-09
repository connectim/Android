package instant.sender.model;

import instant.bean.ChatMsgEntity;
import instant.bean.MessageType;
import instant.bean.Session;
import instant.bean.UserCookie;
import protos.Connect;

/**
 * public methods to extract
 * Created by gtq on 2016/12/19.
 */
public abstract class NormalChat extends BaseChat {

    @Override
    public ChatMsgEntity txtMsg(String string) {
        ChatMsgEntity msgExtEntity = (ChatMsgEntity) createBaseChat(MessageType.Text);
        Connect.TextMessage.Builder builder = Connect.TextMessage.newBuilder()
                .setContent(string);

        msgExtEntity.setContents(builder.build().toByteArray());
        return msgExtEntity;
    }

    @Override
    public ChatMsgEntity photoMsg(String thum, String url, String filesize, int width, int height) {
        ChatMsgEntity msgExtEntity = (ChatMsgEntity) createBaseChat(MessageType.Photo);
        Connect.PhotoMessage.Builder builder = Connect.PhotoMessage.newBuilder()
                .setThum(thum)
                .setUrl(url)
                .setSize(filesize)
                .setImageWidth(width)
                .setImageHeight(height);

        msgExtEntity.setContents(builder.build().toByteArray());
        return msgExtEntity;
    }

    @Override
    public ChatMsgEntity voiceMsg(String string, int length) {
        ChatMsgEntity msgExtEntity = (ChatMsgEntity) createBaseChat(MessageType.Voice);
        Connect.VoiceMessage.Builder builder = Connect.VoiceMessage.newBuilder()
                .setUrl(string)
                .setTimeLength(length);

        msgExtEntity.setContents(builder.build().toByteArray());
        return msgExtEntity;
    }

    @Override
    public ChatMsgEntity videoMsg(String thum, String url, int length, int filesize, int width, int height) {
        ChatMsgEntity msgExtEntity = (ChatMsgEntity) createBaseChat(MessageType.Video);
        Connect.VideoMessage.Builder builder = Connect.VideoMessage.newBuilder()
                .setCover(thum)
                .setUrl(url)
                .setTimeLength(length)
                .setSize(filesize)
                .setImageWidth(width)
                .setImageHeight(height);

        msgExtEntity.setContents(builder.build().toByteArray());
        return msgExtEntity;
    }

    @Override
    public ChatMsgEntity emotionMsg(String string) {
        ChatMsgEntity msgExtEntity = (ChatMsgEntity) createBaseChat(MessageType.Emotion);
        Connect.EmotionMessage.Builder builder = Connect.EmotionMessage.newBuilder()
                .setContent(string);

        msgExtEntity.setContents(builder.build().toByteArray());
        return msgExtEntity;
    }

    @Override
    public ChatMsgEntity cardMsg(String pubkey, String name, String avatar) {
        ChatMsgEntity msgExtEntity = (ChatMsgEntity) createBaseChat(MessageType.Name_Card);
        Connect.CardMessage.Builder builder = Connect.CardMessage.newBuilder()
                .setUid(pubkey)
                .setUsername(name)
                .setAvatar(avatar);

        msgExtEntity.setContents(builder.build().toByteArray());
        return msgExtEntity;
    }

    @Override
    public ChatMsgEntity paymentMsg(int paymenttype, String hashid, long amount, int membersize, String tips) {
        ChatMsgEntity msgExtEntity = (ChatMsgEntity) createBaseChat(MessageType.Request_Payment);
        Connect.PaymentMessage.Builder builder = Connect.PaymentMessage.newBuilder()
                .setPaymentType(paymenttype)
                .setHashId(hashid)
                .setAmount(amount)
                .setMemberSize(membersize)
                .setTips(tips);

        msgExtEntity.setContents(builder.build().toByteArray());
        msgExtEntity.setCrowdCount(membersize);
        msgExtEntity.setPayCount(0);
        return msgExtEntity;
    }

    @Override
    public ChatMsgEntity transferMsg(int type, String hashid, long amout, String tips) {
        ChatMsgEntity msgExtEntity = (ChatMsgEntity) createBaseChat(MessageType.Transfer);
        Connect.TransferMessage.Builder builder = Connect.TransferMessage.newBuilder()
                .setTransferType(type)
                .setHashId(hashid)
                .setAmount(amout)
                .setTips(tips);

        msgExtEntity.setContents(builder.build().toByteArray());
        return msgExtEntity;
    }

    @Override
    public ChatMsgEntity locationMsg(float latitude, float longitude, String address, String thum, int width, int height) {
        ChatMsgEntity msgExtEntity = (ChatMsgEntity) createBaseChat(MessageType.Location);
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
    public ChatMsgEntity luckPacketMsg(int type, String hashid, long amount, String tips) {
        ChatMsgEntity msgExtEntity = (ChatMsgEntity) createBaseChat(MessageType.Lucky_Packet);
        Connect.LuckPacketMessage.Builder builder = Connect.LuckPacketMessage.newBuilder()
                .setLuckyType(type)
                .setHashId(hashid)
                .setAmount(amount)
                .setTips(tips);

        msgExtEntity.setContents(builder.build().toByteArray());
        return msgExtEntity;
    }

    @Override
    public ChatMsgEntity noticeMsg(int noticeType, String content, String ext) {
        ChatMsgEntity msgExtEntity = (ChatMsgEntity) createBaseChat(MessageType.NOTICE);
        Connect.NotifyMessage notifyMessage = Connect.NotifyMessage.newBuilder()
                .setNotifyType(noticeType)
                .setContent(content)
                .setExtion(ext)
                .build();

        msgExtEntity.setContents(notifyMessage.toByteArray());
        return msgExtEntity;
    }

    @Override
    public ChatMsgEntity outerWebsiteMsg(String url, String title, String subtitle, String img) {
        ChatMsgEntity msgExtEntity = (ChatMsgEntity) createBaseChat(MessageType.OUTER_WEBSITE);
        Connect.WebsiteMessage.Builder builder = Connect.WebsiteMessage.newBuilder()
                .setUrl(url)
                .setTitle(title)
                .setSubtitle(subtitle)
                .setImg(img);

        msgExtEntity.setContents(builder.build().toByteArray());
        return msgExtEntity;
    }

    public Connect.MessagePost normalChatMessage(Connect.MessageData data) {
        UserCookie connectCookie = Session.getInstance().getConnectCookie();
        String uid = connectCookie.getUid();

        Connect.MessagePost messagePost = Connect.MessagePost.newBuilder()
                .setUid(uid)
                .setMsgData(data)
                .build();
        return messagePost;
    }

    @Override
    public String friendPublicKey() {
        return null;
    }
}
