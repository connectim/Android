package connect.utils.scan;

import android.app.Activity;
import android.net.Uri;

import connect.activity.chat.exts.TransferToActivity;
import connect.activity.contact.ContactInfoActivity;
import connect.activity.contact.bean.MsgSendBean;
import connect.activity.home.bean.MsgNoticeBean;
import connect.database.SharedPreferenceUtil;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.ToastUtil;
import instant.bean.UserOrderBean;

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
    public static String Web_Url = "connectim://.*";
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
                dealFriend(resultBean, isCloseScan);
                break;
            case TYPE_WEB_PAY:
                dealPay(resultBean, isCloseScan);
                break;
            case TYPE_WEB_TRANSFER:
                dealTransfer(resultBean);
                break;
            case TYPE_WEB_PACKET:
                dealPacket(resultBean);
                break;
            case TYPE_WEB_GROUNP:
                break;
            default:
                break;
        }
    }

    /**
     * Scan user address to enter user details
     * @param resultBean
     * @param isCloseScan
     */
    private void dealFriend(ScanResultBean resultBean, boolean isCloseScan){
        if (!resultBean.getAddress().equals(SharedPreferenceUtil.getInstance().getUser().getUid())) {
            ContactInfoActivity.lunchActivity(activity, resultBean.getAddress());
            if(isCloseScan){
                ActivityUtil.goBack(activity);
            }
        }
    }

    /**
     * Enter the user payment interface
     * @param resultBean
     * @param isCloseScan
     */
    private void dealPay(ScanResultBean resultBean, boolean isCloseScan){
        Double amount = null;
        if (resultBean.getAmount() != null)
            amount = Double.valueOf(resultBean.getAmount());

        TransferToActivity.startActivity(activity, resultBean.getAddress(), amount);
        if(isCloseScan){
            ActivityUtil.goBack(activity);
        }
    }

    /**
     * Receive user external transfer
     * @param resultBean
     */
    private void dealTransfer(ScanResultBean resultBean){
        MsgSendBean transferBean = new MsgSendBean();
        transferBean.setType(MsgSendBean.SendType.TypeOutTransfer);
        transferBean.setTips(resultBean.getTip());

        UserOrderBean userOrderBean = new UserOrderBean();
        userOrderBean.outerTransfer(resultBean.getToken(), transferBean);
    }

    /**
     * Receive user external bonus
     * @param resultBean
     */
    private void dealPacket(ScanResultBean resultBean){
        MsgSendBean packetBean = new MsgSendBean();
        packetBean.setType(MsgSendBean.SendType.TypeOutPacket);
        packetBean.setTips(resultBean.getTip());

        UserOrderBean userOrderBean = new UserOrderBean();
        userOrderBean.outerRedPacket(resultBean.getToken(), packetBean);
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
                dealFailToast(msgSendBean, objs);
                break;
            default:
                break;
        }
    }

    private void dealFailToast(MsgSendBean msgSendBean, Object[] objs){
        int errorNum = (int) objs[1];
        if(msgSendBean.getType() == MsgSendBean.SendType.TypeOutPacket){
            switch (errorNum){
                case 1:
                    ToastUtil.getInstance().showToast(R.string.Chat_Failed_to_get_redpact);
                    break;
                case 2:
                    ToastUtil.getInstance().showToast(R.string.Wallet_You_already_open_this_luckypacket);
                    break;
                case 3:
                    ToastUtil.getInstance().showToast(R.string.Chat_system_luckypackage_have_been_frozen);
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
    }

}
