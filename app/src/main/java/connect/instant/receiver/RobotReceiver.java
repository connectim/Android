package connect.instant.receiver;

import connect.activity.base.BaseApplication;
import connect.activity.chat.bean.RecExtBean;
import connect.activity.login.bean.UserBean;
import connect.database.SharedPreferenceUtil;
import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.DaoHelper.MessageHelper;
import connect.database.green.DaoHelper.ParamManager;
import connect.instant.model.CRobotChat;
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
    public void warehouseMessage(int wareType, byte[] message) {
        ChatMsgEntity chatMsgEntity = RobotChat.getInstance().wareHouseMsg(wareType, StringUtil.bytesToHexString(message));
        dealRobotMessage(chatMsgEntity);
    }

    @Override
    public void auditMessage(Connect.ExamineMessage examineMessage) {
        ChatMsgEntity chatMsgEntity = RobotChat.getInstance().noticeMsg(5, examineMessage.getBody(), "");
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
    public void announcementMessage(Connect.Announcement announcement) {
        ChatMsgEntity chatMsgEntity = RobotChat.getInstance().systemAdNotice(announcement);
        dealRobotMessage(chatMsgEntity);
    }

    @Override
    public void systemRedpackgeNoticeMessage(Connect.SystemRedpackgeNotice packgeNotice) {
        String mypubkey = SharedPreferenceUtil.getInstance().getUser().getUid();
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
        String mypublickey = SharedPreferenceUtil.getInstance().getUser().getUid();
        chatMsgEntity.setMessage_from(BaseApplication.getInstance().getString(R.string.app_name));
        chatMsgEntity.setMessage_to(mypublickey);

        MessageHelper.getInstance().insertMsgExtEntity(chatMsgEntity);

        String robotname = BaseApplication.getInstance().getString(R.string.app_name);
        String content = chatMsgEntity.showContent();
        CRobotChat.getInstance().updateRoomMsg(null, content, chatMsgEntity.getCreatetime(), 0, 1);
        NotificationBar.notificationBar.noticeBarMsg(robotname, 2, robotname, content);
    }
}
