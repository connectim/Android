package connect.instant.receiver;

import instant.parser.inter.RobotListener;
import protos.Connect;

/**
 * Created by Administrator on 2017/10/10.
 */

public class RobotReceiver implements RobotListener {

    private String Tag = "_RobotReceiver";

    public static RobotReceiver receiver = getInstance();

    private synchronized static RobotReceiver getInstance() {
        if (receiver == null) {
            receiver = new RobotReceiver();
        }
        return receiver;
    }

    @Override
    public void textMessage(Connect.TextMessage textMessage) {

    }

    @Override
    public void voiceMessage(Connect.VoiceMessage voiceMessage) {

    }

    @Override
    public void photoMessage(Connect.PhotoMessage photoMessage) {

    }

    @Override
    public void translationMessage(Connect.SystemTransferPackage transferPackage) {

    }

    @Override
    public void systemRedPackageMessage(Connect.SystemRedPackage redPackage) {

    }

    @Override
    public void reviewedMessage(Connect.Reviewed reviewed) {

    }

    @Override
    public void announcementMessage(Connect.Announcement announcement) {

    }

    @Override
    public void systemRedpackgeNoticeMessage(Connect.SystemRedpackgeNotice packgeNotice) {

    }

    @Override
    public void reviewedResponseMessage(Connect.ReviewedResponse reviewedResponse) {

    }

    @Override
    public void updateMobileBindMessage(Connect.UpdateMobileBind mobileBind) {

    }

    @Override
    public void removeGroupMessage(Connect.RemoveGroup removeGroup) {

    }

    @Override
    public void addressNotifyMessage(Connect.AddressNotify addressNotify) {

    }
}
