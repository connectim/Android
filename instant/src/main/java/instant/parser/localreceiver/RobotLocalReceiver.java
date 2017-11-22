package instant.parser.localreceiver;

import instant.parser.inter.RobotListener;
import protos.Connect;

/**
 * Created by Administrator on 2017/10/9.
 */

public class RobotLocalReceiver implements RobotListener {

    public static RobotLocalReceiver localReceiver = getInstance();

    private synchronized static RobotLocalReceiver getInstance() {
        if (localReceiver == null) {
            localReceiver = new RobotLocalReceiver();
        }
        return localReceiver;
    }

    private RobotListener robotListener = null;

    public void registerRobotListener(RobotListener listener) {
        this.robotListener = listener;
    }

    public RobotListener getRobotListener() {
        if (robotListener == null) {
            throw new RuntimeException("robotListener don't registe");
        }
        return robotListener;
    }

    @Override
    public void textMessage(Connect.TextMessage textMessage) {
        getRobotListener().textMessage(textMessage);
    }

    @Override
    public void voiceMessage(Connect.VoiceMessage voiceMessage) {
        getRobotListener().voiceMessage(voiceMessage);
    }

    @Override
    public void photoMessage(Connect.PhotoMessage photoMessage) {
        getRobotListener().photoMessage(photoMessage);
    }

    @Override
    public void translationMessage(Connect.SystemTransferPackage transferPackage) {
        getRobotListener().translationMessage(transferPackage);
    }

    @Override
    public void systemRedPackageMessage(Connect.SystemRedPackage redPackage) {
        getRobotListener().systemRedPackageMessage(redPackage);
    }

    @Override
    public void reviewedMessage(Connect.Reviewed reviewed) {
        getRobotListener().reviewedMessage(reviewed);
    }

    @Override
    public void announcementMessage(Connect.Announcement announcement) {
        getRobotListener().announcementMessage(announcement);
    }

    @Override
    public void systemRedpackgeNoticeMessage(Connect.SystemRedpackgeNotice packgeNotice) {
        getRobotListener().systemRedpackgeNoticeMessage(packgeNotice);
    }

    @Override
    public void reviewedResponseMessage(Connect.ReviewedResponse reviewedResponse) {
        getRobotListener().reviewedResponseMessage(reviewedResponse);
    }

    @Override
    public void updateMobileBindMessage(Connect.UpdateMobileBind mobileBind) {
        getRobotListener().updateMobileBindMessage(mobileBind);
    }

    @Override
    public void removeGroupMessage(Connect.RemoveGroup removeGroup) {
        getRobotListener().removeGroupMessage(removeGroup);
    }

    @Override
    public void addressNotifyMessage(Connect.AddressNotify addressNotify) {
        getRobotListener().addressNotifyMessage(addressNotify);
    }

    @Override
    public void subscribePull(Connect.RSSPush rssPush) throws Exception {
        getRobotListener().subscribePull(rssPush);
    }
}
