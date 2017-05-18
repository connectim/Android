package connect.utils.scan;

import android.app.Activity;
import android.net.Uri;

import connect.db.MemoryDataManager;
import connect.db.SharedPreferenceUtil;
import connect.db.green.DaoHelper.ContactHelper;
import connect.db.green.bean.ContactEntity;
import connect.im.msgdeal.SendMsgUtil;
import connect.ui.activity.R;
import connect.ui.activity.chat.exts.ApplyJoinGroupActivity;
import connect.ui.activity.chat.exts.TransferToActivity;
import connect.ui.activity.contact.FriendInfoActivity;
import connect.ui.activity.contact.StrangerInfoActivity;
import connect.ui.activity.contact.bean.MsgSendBean;
import connect.ui.activity.contact.bean.SourceType;
import connect.ui.activity.home.bean.MsgNoticeBean;
import connect.utils.ActivityUtil;
import connect.utils.ToastEUtil;
import connect.utils.ToastUtil;

/**
 * Web Url parsing
 */

public class ResolveUrlUtil {

    public static final String TYPE_WEB_FRIEND = "friend";
    public static final String TYPE_WEB_PAY = "pay";
    public static final String TYPE_WEB_TRANSFER = "transfer";
    public static final String TYPE_WEB_PACKET = "packet";
    public static final String TYPE_WEB_GROUNP = "group";
    public static final String TYPE_OPEN_SCAN = "scan";
    public static final String TYPE_OPEN_WEB= "web";
    public String Web_Url = "connectim://.*";
    private Activity activity;

    public ResolveUrlUtil(Activity activity) {
        this.activity = activity;
    }

    /**
     * Check whether open for a Web App
     * @param url
     */
    public void checkAppOpen(String url) {
        Uri uri = Uri.parse(url);
        String address = uri.getQueryParameter("address");
        String amount = uri.getQueryParameter("amount");
        String token = uri.getQueryParameter("token");

        ScanResultBean resultBean = new ScanResultBean();
        resultBean.setType(uri.getHost());
        resultBean.setAddress(address == null ? "" : address);
        resultBean.setAmount(amount == null ? "" : amount);
        resultBean.setToken(token == null ? "" : token);
        resultBean.setTip(TYPE_OPEN_WEB);
        dealResult(resultBean,false);
    }

    /**
     * According to the type of processing links to content
     * @param resultBean
     * @param isCloseScan
     */
    public void dealResult(ScanResultBean resultBean,boolean isCloseScan){
        switch (resultBean.getType()){
            case TYPE_WEB_FRIEND:
                if (!resultBean.getAddress().equals(MemoryDataManager.getInstance().getAddress())) {
                    ContactEntity friendEntity = ContactHelper.getInstance().loadFriendEntity(resultBean.getAddress());
                    if (friendEntity != null) {
                        FriendInfoActivity.startActivity(activity, resultBean.getAddress());
                    } else {
                        StrangerInfoActivity.startActivity(activity, resultBean.getAddress(), SourceType.QECODE);
                    }
                    if(isCloseScan){
                        ActivityUtil.goBack(activity);
                    }
                }
                break;
            case TYPE_WEB_PAY:
                Double amount = null;
                if (resultBean.getAmount() != null)
                    amount = Double.valueOf(resultBean.getAmount());
                if (!resultBean.getAddress().equals(MemoryDataManager.getInstance().getAddress())) {
                    TransferToActivity.startActivity(activity, resultBean.getAddress(), amount);
                    if(isCloseScan){
                        ActivityUtil.goBack(activity);
                    }
                } else {
                    ToastEUtil.makeText(activity, R.string.Wallet_Could_not_get_himself_sent_money_transfer).show();
                }
                break;
            case TYPE_WEB_TRANSFER:
                MsgSendBean transferBean = new MsgSendBean();
                transferBean.setType(MsgSendBean.SendType.TypeOutTransfer);
                transferBean.setTips(resultBean.getTip());
                SendMsgUtil.outerTransfer(resultBean.getToken(), transferBean);
                break;
            case TYPE_WEB_PACKET:
                MsgSendBean packetBean = new MsgSendBean();
                packetBean.setType(MsgSendBean.SendType.TypeOutPacket);
                packetBean.setTips(resultBean.getTip());
                SendMsgUtil.outerRedPacket(resultBean.getToken(), packetBean);
                break;
            case TYPE_WEB_GROUNP:
                ApplyJoinGroupActivity.startActivity(activity, ApplyJoinGroupActivity.EApplyGroup.TOKEN, resultBean.getToken());
                if(isCloseScan){
                    ActivityUtil.goBack(activity);
                }
                break;
            default:
                break;
        }
    }

    /**
     * Accept external red envelopes, external transfer error notification
     * @param notice
     * @param isCloseScan
     */
    public void showMsgTip(MsgNoticeBean notice,String tips, boolean isCloseScan){
        Object[] objs = null;
        if (notice.object != null) {
            objs = (Object[]) notice.object;
        }
        MsgSendBean msgSendBean = (MsgSendBean) objs[0];
        if (msgSendBean == null || !tips.equals(msgSendBean.getTips()))
            return;
        switch (notice.ntEnum) {
            case MSG_SEND_SUCCESS:
                if(isCloseScan){
                    ActivityUtil.goBack(activity);
                }
                break;
            case MSG_SEND_FAIL:
                int errorNum = (int) objs[1];
                if(msgSendBean.getType() == MsgSendBean.SendType.TypeOutPacket){
                    switch (errorNum){
                        case 1:
                            ToastUtil.getInstance().showToast(R.string.Chat_Failed_to_get_redpact);
                            break;
                        case 2:
                            ToastUtil.getInstance().showToast(R.string.Wallet_You_already_open_this_luckypacket);
                            break;
                    }
                }else if(msgSendBean.getType() == MsgSendBean.SendType.TypeOutTransfer){
                    switch (errorNum){
                        case 1:
                            ToastUtil.getInstance().showToast(R.string.Wallet_Transfer_Failed);
                            break;
                        case 2:
                            ToastUtil.getInstance().showToast(R.string.Wallet_Transfer_Failed);
                            break;
                    }
                }
                break;
            default:
                break;
        }
    }

}
