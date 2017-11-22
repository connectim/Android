package connect.instant.receiver;

import java.util.ArrayList;
import java.util.List;

import connect.activity.base.BaseApplication;
import connect.activity.chat.bean.ApplyGroupBean;
import connect.activity.chat.bean.RecExtBean;
import connect.activity.home.bean.ConversationAction;
import connect.activity.login.bean.UserBean;
import connect.database.SharedPreferenceUtil;
import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.DaoHelper.MessageHelper;
import connect.database.green.DaoHelper.ParamManager;
import connect.database.green.DaoHelper.SubscribeConversationHelper;
import connect.database.green.DaoHelper.SubscribeDetailHelper;
import connect.database.green.bean.SubscribeDetailEntity;
import connect.instant.model.CRobotChat;
import connect.instant.model.CSubscriberChat;
import connect.ui.activity.R;
import connect.utils.NotificationBar;
import connect.utils.StringUtil;
import instant.bean.ChatMsgEntity;
import instant.parser.inter.RobotListener;
import instant.sender.model.RobotChat;
import protos.Connect;

/**
 * Created by Administrator on 2017/10/10.
 */

public class RobotReceiver implements RobotListener {

    private static String TAG = "_RobotReceiver";

    public static RobotReceiver receiver = getInstance();

    private synchronized static RobotReceiver getInstance() {
        if (receiver == null) {
            receiver = new RobotReceiver();
        }
        return receiver;
    }


    @Override
    public void textMessage(Connect.TextMessage textMessage) {
        ChatMsgEntity chatMsgEntity = RobotChat.getInstance().txtMsg(textMessage.getContent());
        dealRobotMessage(chatMsgEntity);
    }

    @Override
    public void voiceMessage(Connect.VoiceMessage voiceMessage) {
        ChatMsgEntity chatMsgEntity = RobotChat.getInstance().voiceMsg(voiceMessage.getUrl(), voiceMessage.getTimeLength());
        dealRobotMessage(chatMsgEntity);
    }

    @Override
    public void photoMessage(Connect.PhotoMessage photoMessage) {
        ChatMsgEntity chatMsgEntity = RobotChat.getInstance().photoMsg(photoMessage.getThum(), photoMessage.getUrl(), photoMessage.getSize(),
                photoMessage.getImageWidth(), photoMessage.getImageHeight());
        dealRobotMessage(chatMsgEntity);
    }

    @Override
    public void translationMessage(Connect.SystemTransferPackage transferPackage) {
        ChatMsgEntity chatMsgEntity = RobotChat.getInstance().transferMsg(0, transferPackage.getTxid(), transferPackage.getAmount(), transferPackage.getTips());
        dealRobotMessage(chatMsgEntity);
    }

    @Override
    public void systemRedPackageMessage(Connect.SystemRedPackage redPackage) {
        ChatMsgEntity chatMsgEntity = RobotChat.getInstance().luckPacketMsg(0, redPackage.getHashId(), redPackage.getAmount(), redPackage.getTips());
        dealRobotMessage(chatMsgEntity);
    }

    @Override
    public void reviewedMessage(Connect.Reviewed reviewed) {
        ChatMsgEntity chatMsgEntity = RobotChat.getInstance().groupReviewMsg(reviewed);

        String msgid = chatMsgEntity.getMessage_id();
        String groupApplyKey = reviewed.getIdentifier() + reviewed.getUserInfo().getPubKey();
        ApplyGroupBean applyGroupBean = ParamManager.getInstance().loadGroupApply(groupApplyKey);
        if (applyGroupBean == null) {//new apply
            ParamManager.getInstance().updateGroupApply(groupApplyKey, reviewed.getTips(), reviewed.getSource(), 0, msgid);
        } else {//repeat apply, remove the last
            MessageHelper.getInstance().deleteMsgByid(applyGroupBean.getMsgid());
            ParamManager.getInstance().updateGroupApply(groupApplyKey, reviewed.getTips(), reviewed.getSource(), -1, msgid);
        }
        dealRobotMessage(chatMsgEntity);
    }

    @Override
    public void announcementMessage(Connect.Announcement announcement) {
        ChatMsgEntity chatMsgEntity = RobotChat.getInstance().systemAdNotice(announcement);
        dealRobotMessage(chatMsgEntity);
    }

    @Override
    public void systemRedpackgeNoticeMessage(Connect.SystemRedpackgeNotice packgeNotice) {
        String mypubkey = SharedPreferenceUtil.getInstance().getUser().getPubKey();
        Connect.UserInfo userInfo = packgeNotice.getReceiver();
        String receiverName = userInfo.getPubKey().equals(mypubkey) ?
                BaseApplication.getInstance().getBaseContext().getString(R.string.Chat_You) : userInfo.getUsername();

        String outerNotice = BaseApplication.getInstance().getBaseContext().getString(R.string.Chat_opened_Lucky_Packet_of,
                receiverName, BaseApplication.getInstance().getBaseContext().getString(R.string.Chat_You));

        ChatMsgEntity chatMsgEntity = RobotChat.getInstance().noticeMsg(3, outerNotice, packgeNotice.getHashid());
        dealRobotMessage(chatMsgEntity);
    }

    @Override
    public void reviewedResponseMessage(Connect.ReviewedResponse reviewedResponse) {
        String notice = "";
        if (reviewedResponse.getSuccess()) {
            notice = BaseApplication.getInstance().getBaseContext().getString(R.string.Link_You_apply_to_join_has_passed, reviewedResponse.getName());
        } else {
            notice = BaseApplication.getInstance().getBaseContext().getString(R.string.Link_You_apply_to_join_rejected, reviewedResponse.getName());
        }
        ChatMsgEntity chatMsgEntity = RobotChat.getInstance().noticeMsg(0, notice, "");
        ParamManager.getInstance().updateGroupApplyMember(reviewedResponse.getIdentifier(), reviewedResponse.getSuccess() ? 1 : 2);
        dealRobotMessage(chatMsgEntity);
    }

    @Override
    public void updateMobileBindMessage(Connect.UpdateMobileBind mobileBind) {
        String content = BaseApplication.getInstance().getBaseContext().getString(R.string.Chat_Your_Connect_ID_will_no_longer_be_linked_with_mobile_number,
                mobileBind.getUsername());
        ChatMsgEntity chatMsgEntity = RobotChat.getInstance().txtMsg(content);

        UserBean userBean = SharedPreferenceUtil.getInstance().getUser();
        userBean.setPhone("");
        SharedPreferenceUtil.getInstance().putUser(userBean);
        dealRobotMessage(chatMsgEntity);
    }

    @Override
    public void removeGroupMessage(Connect.RemoveGroup removeGroup) {
        ChatMsgEntity chatMsgEntity = RobotChat.getInstance().noticeMsg(0,
                BaseApplication.getInstance().getBaseContext().getString(R.string.Chat_Group_has_been_disbanded,
                        removeGroup.getName()), "");

        ContactHelper.getInstance().removeGroupInfos(removeGroup.getGroupId());
        RecExtBean.getInstance().sendEvent(RecExtBean.ExtType.GROUP_REMOVE, removeGroup.getGroupId());
        dealRobotMessage(chatMsgEntity);
    }

    @Override
    public void addressNotifyMessage(Connect.AddressNotify addressNotify) {

    }

    public void dealRobotMessage(ChatMsgEntity chatMsgEntity) {
        chatMsgEntity.setSend_status(1);
        String mypublickey = SharedPreferenceUtil.getInstance().getUser().getPubKey();
        chatMsgEntity.setMessage_from(BaseApplication.getInstance().getString(R.string.app_name));
        chatMsgEntity.setMessage_to(mypublickey);

        MessageHelper.getInstance().insertMsgExtEntity(chatMsgEntity);

        String robotname = BaseApplication.getInstance().getString(R.string.app_name);
        String content = chatMsgEntity.showContent();
        CRobotChat.getInstance().updateRoomMsg(null, content, chatMsgEntity.getCreatetime(), -1, 1);
        NotificationBar.notificationBar.noticeBarMsg(robotname, 2, content);
    }

    @Override
    public void subscribePull(Connect.RSSPush rssPush) throws Exception {
        List<SubscribeDetailEntity> chatMsgEntities = new ArrayList<>();
        switch (rssPush.getCategory()) {
            case 1://rss
                Connect.RSSMessage lastRssMessage = null;
                Connect.RSSMessageList rssMessageList = Connect.RSSMessageList.parseFrom(rssPush.getData());
                for (Connect.RSSMessage rssMessage : rssMessageList.getMessagesList()) {
                    SubscribeDetailEntity detailEntity = new SubscribeDetailEntity();
                    detailEntity.setMessageId(rssMessage.getId());
                    detailEntity.setRssId(rssPush.getRssId());
                    detailEntity.setCategory(1);
                    detailEntity.setContent(StringUtil.bytesToHexString(rssMessage.toByteArray()));
                    chatMsgEntities.add(detailEntity);

                    lastRssMessage = rssMessage;
                }
                if (lastRssMessage != null) {
                    SubscribeConversationHelper.subscribeConversationHelper.updataConversationEntity(
                            rssPush.getRssId(),
                            lastRssMessage.getTitle(),
                            lastRssMessage.getTime(),
                            1);

                    CSubscriberChat.cSubscriberChat.updateConversationListEntity(
                            rssPush.getRssId(),
                            "",
                            "",
                            lastRssMessage.getTitle(),
                            lastRssMessage.getTime(),
                            1);
                    CSubscriberChat.cSubscriberChat.updateRoomMsg("", lastRssMessage.getTitle(), lastRssMessage.getTime(), -1, 1);
                }
                break;
            case 2://article
                Connect.Article lastArtcle = null;
                Connect.ArticleList articleList = Connect.ArticleList.parseFrom(rssPush.getData());
                for (Connect.Article article : articleList.getMessagesList()) {
                    SubscribeDetailEntity detailEntity = new SubscribeDetailEntity();
                    detailEntity.setMessageId(article.getId());
                    detailEntity.setRssId(rssPush.getRssId());
                    detailEntity.setCategory(2);
                    detailEntity.setContent(StringUtil.bytesToHexString(article.toByteArray()));
                    chatMsgEntities.add(detailEntity);

                    lastArtcle = article;
                }
                if (lastArtcle != null) {
                    SubscribeConversationHelper.subscribeConversationHelper.updataConversationEntity(
                            rssPush.getRssId(),
                            lastArtcle.getTitle(),
                            lastArtcle.getTime(),
                            1);

                    CSubscriberChat.cSubscriberChat.updateConversationListEntity(
                            rssPush.getRssId(),
                            "",
                            "",
                            lastArtcle.getTitle(),
                            lastArtcle.getTime(),
                            1);
                    CSubscriberChat.cSubscriberChat.updateRoomMsg("", lastArtcle.getTitle(), lastArtcle.getTime(), -1, 1);
                }
                break;
        }

        SubscribeDetailHelper.subscribeDetailHelper.insertSubscribeEntities(chatMsgEntities);
        ConversationAction.conversationAction.sendEventDelay(ConversationAction.ConverType.LOAD_UNREAD);
    }
}
