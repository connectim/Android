package instant.parser.inter;

import protos.Connect;

/**
 * Created by Administrator on 2017/10/9.
 */
public interface RobotListener {

    void textMessage(Connect.TextMessage textMessage);

    void warehouseMessage(int wareType, byte[] message);

    void voiceMessage(Connect.VoiceMessage voiceMessage);

    void photoMessage(Connect.PhotoMessage photoMessage);

    void translationMessage(Connect.SystemTransferPackage transferPackage);

    void systemRedPackageMessage(Connect.SystemRedPackage redPackage);

    void announcementMessage(Connect.Announcement announcement);

    void systemRedpackgeNoticeMessage(Connect.SystemRedpackgeNotice packgeNotice);

    void reviewedResponseMessage(Connect.ReviewedResponse reviewedResponse);

    void updateMobileBindMessage(Connect.UpdateMobileBind mobileBind);

    void removeGroupMessage(Connect.RemoveGroup removeGroup);

    void addressNotifyMessage(Connect.AddressNotify addressNotify);

    void auditMessage(Connect.ExamineMessage  examineMessage);
}
