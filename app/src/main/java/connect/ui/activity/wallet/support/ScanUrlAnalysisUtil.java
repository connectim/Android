package connect.ui.activity.wallet.support;

import android.app.Activity;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.protobuf.InvalidProtocolBufferException;

import java.net.MalformedURLException;
import java.net.URL;

import connect.db.SharedPreferenceUtil;
import connect.db.green.DaoHelper.ContactHelper;
import connect.db.green.bean.ContactEntity;
import connect.im.bean.UserOrderBean;
import connect.ui.activity.R;
import connect.ui.activity.chat.exts.ApplyJoinGroupActivity;
import connect.ui.activity.chat.exts.OuterWebsiteActivity;
import connect.ui.activity.chat.exts.TransferToActivity;
import connect.ui.activity.contact.FriendInfoActivity;
import connect.ui.activity.contact.StrangerInfoActivity;
import connect.ui.activity.contact.bean.MsgSendBean;
import connect.ui.activity.contact.bean.SourceType;
import connect.ui.activity.home.bean.MsgNoticeBean;
import connect.ui.activity.login.bean.UserBean;
import connect.ui.activity.login.bean.WebOpenBean;
import connect.ui.activity.wallet.RequestActivity;
import connect.ui.activity.wallet.TransferAddressActivity;
import connect.ui.activity.wallet.bean.ScanResultBean;
import connect.utils.ActivityUtil;
import connect.utils.RegularUtil;
import connect.utils.ToastEUtil;
import connect.utils.ToastUtil;
import connect.utils.UriUtil;
import connect.utils.cryption.DecryptionUtil;
import connect.utils.cryption.SupportKeyUril;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import protos.Connect;

/**
 * Parsing the external links
 * Created by Administrator on 2017/2/26.
 */

public class ScanUrlAnalysisUtil {

    public static final String TYPE_WEB_FRIEND = "friend";
    public static final String TYPE_WEB_PAY = "pay";
    public static final String TYPE_WEB_TRANSFER = "transfer";
    public static final String TYPE_WEB_PACKET = "packet";
    public static final String TYPE_WEB_GROUNP = "group";
    public static final String TYPE_WEB_GROUP_ = "group:";
    private String Request_Matches = "bitcoin:.*.?amount=.*";
    private String Url_Matches = "(?:(?:(?:[a-z]+:)?//))?(?:localhost|(?:25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9][0-9]|[0-9])(?:\\.(?:25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9][0-9]|[0-9])){3}|(?:(?:[a-z\\u00a1-\\uffff0-9]-*)*[a-z\\u00a1-\\uffff0-9]+)(?:\\.(?:[a-z\\u00a1-\\uffff0-9]-*)*[a-z\\u00a1-\\uffff0-9]+)*(?:\\.(?:[a-z\\u00a1-\\uffff]{2,}))\\.?)(?::\\d{2,5})?(?:[/?#][^\\s\"]*)?";
    private String Connect_Url = ".*.connect.im";
    public String Web_Url = "connectim://.*";
    private Activity activity;

    public ScanUrlAnalysisUtil(Activity activity) {
        this.activity = activity;
    }

    /**
     * web open App
     */
    public void checkWebOpen() {
        String value = SharedPreferenceUtil.getInstance().getStringValue(SharedPreferenceUtil.WEB_OPEN_APP);
        if (!TextUtils.isEmpty(value)) {
            SharedPreferenceUtil.getInstance().remove(SharedPreferenceUtil.WEB_OPEN_APP);
            WebOpenBean webOpenBean = new Gson().fromJson(value, WebOpenBean.class);
            ScanResultBean resultBean = new ScanResultBean();
            resultBean.setAddress(webOpenBean.getAddress());
            resultBean.setAmount(webOpenBean.getAmount());
            resultBean.setToken(webOpenBean.getToken());
            resultBean.setTip("web");
            dealResult(webOpenBean.getType(),resultBean);
        }
    }

    /**
     * open
     * @param url
     */
    public void checkAppOpen(String url) {
        Uri uri = Uri.parse(url);
        WebOpenBean scanResultBean = WebOpenAppData(uri);
        ScanResultBean resultBean = new ScanResultBean();
        resultBean.setAddress(scanResultBean.getAddress());
        resultBean.setAmount(scanResultBean.getAmount());
        resultBean.setToken(scanResultBean.getToken());
        resultBean.setTip("web");
        dealResult(scanResultBean.getType(),resultBean);
    }

    public static WebOpenBean WebOpenAppData(Uri uri){
        WebOpenBean openBean = new WebOpenBean();
        switch (uri.getHost()){
            case ScanUrlAnalysisUtil.TYPE_WEB_FRIEND:
                openBean.setType(uri.getHost());
                openBean.setAddress(uri.getQueryParameter("address"));
                break;
            case ScanUrlAnalysisUtil.TYPE_WEB_PAY:
                openBean.setType(uri.getHost());
                openBean.setAddress(uri.getQueryParameter("address"));
                openBean.setAmount(uri.getQueryParameter("amount"));
                break;
            case ScanUrlAnalysisUtil.TYPE_WEB_TRANSFER:
                openBean.setType(uri.getHost());
                openBean.setToken(uri.getQueryParameter("token"));
                break;
            case ScanUrlAnalysisUtil.TYPE_WEB_PACKET:
                openBean.setType(uri.getHost());
                openBean.setToken(uri.getQueryParameter("token"));
                break;
            case ScanUrlAnalysisUtil.TYPE_WEB_GROUNP:
                openBean.setType(uri.getHost());
                openBean.setToken(uri.getQueryParameter("token"));
                break;
        }
        return openBean;
    }

    /**
     * Accept external red envelopes, external transfer error notification
     * @param notice
     * @param type
     */
    public void showMsgTip(MsgNoticeBean notice,String type){
        Object[] objs = null;
        if (notice.object != null) {
            objs = (Object[]) notice.object;
        }
        MsgSendBean msgSendBean = (MsgSendBean) objs[0];
        if (msgSendBean == null || !type.equals(msgSendBean.getTips()))
            return;

        switch (notice.ntEnum) {
            case MSG_SEND_SUCCESS:
                if(msgSendBean.getTips() != null && msgSendBean.getTips().equals("scan")){
                    ActivityUtil.goBack(activity);
                }
                break;
            case MSG_SEND_FAIL:
                int errorNum = (int) objs[1];
                if(msgSendBean.getType() == MsgSendBean.SendType.TypeOutPacket){
                    switch (errorNum){
                        case 1:
                            ToastUtil.getInstance().showToast(R.string.Chat_Failed_to_get_redpact);
                            //ToastEUtil.makeText(activity,R.string.Chat_Failed_to_get_redpact,ToastEUtil.TOAST_STATUS_FAILE).show();
                            break;
                        case 2:
                            ToastUtil.getInstance().showToast(R.string.Wallet_You_already_open_this_luckypacket);
                            //ToastEUtil.makeText(activity,R.string.Wallet_You_already_open_this_luckypacket,ToastEUtil.TOAST_STATUS_FAILE).show();
                            break;
                    }
                }else if(msgSendBean.getType() == MsgSendBean.SendType.TypeOutTransfer){
                    switch (errorNum){
                        case 1:
                            ToastUtil.getInstance().showToast(R.string.Wallet_Transfer_Failed);
                            //ToastEUtil.makeText(activity,R.string.Wallet_Transfer_Failed,ToastEUtil.TOAST_STATUS_FAILE).show();
                            break;
                        case 2:
                            ToastUtil.getInstance().showToast(R.string.Wallet_Transfer_Failed);
                            //ToastEUtil.makeText(activity,R.string.Wallet_Transfer_Failed,ToastEUtil.TOAST_STATUS_FAILE).show();
                            break;
                    }
                }
                break;
        }
    }

    private void dealResult(String resule, ScanResultBean resultBean){
        UserOrderBean userOrderBean = null;
        switch (resule){
            case TYPE_WEB_FRIEND:
                if (!resultBean.getAddress().equals(SharedPreferenceUtil.getInstance().getUser().getAddress())) {
                    ContactEntity friendEntity = ContactHelper.getInstance().loadFriendEntity(resultBean.getAddress());
                    if (friendEntity != null) {
                        FriendInfoActivity.startActivity(activity, resultBean.getAddress());
                    } else {
                        StrangerInfoActivity.startActivity(activity, resultBean.getAddress(), SourceType.QECODE);
                    }
                    if(resultBean.getTip().equals("scan")){
                        ActivityUtil.goBack(activity);
                    }
                }
                break;
            case TYPE_WEB_PAY:
                Double amount = null;
                if (resultBean.getAmount() != null)
                    amount = Double.valueOf(resultBean.getAmount());
                if (!resultBean.getAddress().equals(SharedPreferenceUtil.getInstance().getUser().getAddress())) {
                    TransferToActivity.startActivity(activity, resultBean.getAddress(), amount);
                    if(resultBean.getTip().equals("scan")){
                        ActivityUtil.goBack(activity);
                    }
                } else {
                    ToastEUtil.makeText(activity,R.string.Wallet_Could_not_get_himself_sent_money_transfer).show();
                }
                break;
            case TYPE_WEB_TRANSFER:
                MsgSendBean transferBean = new MsgSendBean();
                transferBean.setType(MsgSendBean.SendType.TypeOutTransfer);
                transferBean.setTips(resultBean.getTip());

                userOrderBean = new UserOrderBean();
                userOrderBean.outerTransfer(resultBean.getToken(), transferBean);
                break;
            case TYPE_WEB_PACKET:
                MsgSendBean packetBean = new MsgSendBean();
                packetBean.setType(MsgSendBean.SendType.TypeOutPacket);
                packetBean.setTips(resultBean.getTip());

                userOrderBean = new UserOrderBean();
                userOrderBean.outerRedPacket(resultBean.getToken(), packetBean);
                break;
            case TYPE_WEB_GROUNP:
                ApplyJoinGroupActivity.startActivity(activity, ApplyJoinGroupActivity.EApplyGroup.TOKEN, resultBean.getToken());
                if(resultBean.getTip().equals("scan")){
                    ActivityUtil.goBack(activity);
                }
                break;

            default:
                break;
        }
    }

    public void analysisUrl(String value){
        if(RegularUtil.matches(value, Url_Matches)){
            try {
                URL url = new URL(value);
                String host = url.getHost();
                if(RegularUtil.matches(host, Connect_Url) && url.getQuery() != null && url.getQuery().contains("token")){
                    String[] pathArray = url.getPath().split("/");
                    String token = Uri.parse(value).getQueryParameter("token");

                    ScanResultBean resultBean = new ScanResultBean();
                    resultBean.setToken(token);
                    resultBean.setTip("scan");
                    dealResult(pathArray[pathArray.length-1],resultBean);
                }else{
                    OuterWebsiteActivity.startActivity(activity,value);
                    ActivityUtil.goBack(activity);
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            return;
        }

        if(value.contains(TYPE_WEB_GROUP_)){
            String[] valueArray = value.replace(TYPE_WEB_GROUP_,"").split("/");
            ApplyJoinGroupActivity.startActivity(activity, ApplyJoinGroupActivity.EApplyGroup.QRSCAN, valueArray[0],value);
            ActivityUtil.goBack(activity);
            return;
        }

        if(SupportKeyUril.checkAddress(value)){
            checkIsFriend(value,null);
            return;
        }

        if(RegularUtil.matches(value, Request_Matches)){
            String[] data = value.split("\\?" + RequestActivity.TRANSFER_AMOUNT_HEAD);
            String address = data[0].replace(RequestActivity.TRANSFER_SCAN_HEAD,"");
            if(SupportKeyUril.checkAddress(address)){
                try {
                    String amount = Uri.parse(value.replace(RequestActivity.TRANSFER_SCAN_HEAD,""))
                            .getQueryParameter("amount");
                    requestUserInfo(address,Double.valueOf(amount),1);
                }catch (Exception e){
                    e.printStackTrace();
                }
                return;
            }
        }
        if(value.contains(RequestActivity.TRANSFER_SCAN_HEAD)){
            String address = value.replace(RequestActivity.TRANSFER_SCAN_HEAD,"");
            if(SupportKeyUril.checkAddress(address)){
                requestUserInfo(address,null,1);
                return;
            }
            return;
        }

        ToastEUtil.makeText(activity,R.string.Login_scan_string_error);
    }

    private void checkIsFriend(final String address, final Double amout) {
        new AsyncTask<Void, Void, ContactEntity>() {
            @Override
            protected ContactEntity doInBackground(Void... params) {
                return ContactHelper.getInstance().loadFriendEntity(address);
            }

            @Override
            protected void onPostExecute(ContactEntity friendEntity) {
                super.onPostExecute(friendEntity);
                if (friendEntity == null) {
                    requestUserInfo(address,amout,0);
                } else {
                    FriendInfoActivity.startActivity(activity, friendEntity.getPub_key());
                    ActivityUtil.goBack(activity);
                }
            }
        }.execute();
    }

    /**
     * Request user information
     * @param address
     * @param amout
     * @param status 1:friend 2:stranger
     */
    private void requestUserInfo(final String address, final Double amout, final int status) {
        final UserBean userBean = SharedPreferenceUtil.getInstance().getUser();
        final Connect.SearchUser searchUser = Connect.SearchUser.newBuilder()
                .setCriteria(address)
                .build();
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.CONNECT_V1_USER_SEARCH, searchUser, new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                try {
                    Connect.IMResponse imResponse = Connect.IMResponse.parseFrom(response.getBody().toByteArray());
                    Connect.StructData structData = DecryptionUtil.decodeAESGCMStructData(userBean.getPriKey(), imResponse.getCipherData());
                    Connect.UserInfo sendUserInfo = Connect.UserInfo.parseFrom(structData.getPlainData());
                    if(sendUserInfo != null && !TextUtils.isEmpty(sendUserInfo.getPubKey())){
                        if(status == 1){
                            TransferToActivity.startActivity(activity,address,amout);
                        }else{
                            StrangerInfoActivity.startActivity(activity, address, SourceType.QECODE);
                        }
                    }else{
                        TransferAddressActivity.startActivity(activity,address,amout);
                    }
                    ActivityUtil.goBack(activity);
                } catch (InvalidProtocolBufferException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Connect.HttpResponse response) {
                if(response.getCode() == 2404){
                    TransferAddressActivity.startActivity(activity,address,amout);
                }
                ActivityUtil.goBack(activity);
            }
        });
    }

}
