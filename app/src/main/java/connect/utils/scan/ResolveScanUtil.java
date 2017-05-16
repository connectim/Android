package connect.utils.scan;

import android.app.Activity;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.google.protobuf.InvalidProtocolBufferException;

import java.net.MalformedURLException;
import java.net.URL;

import connect.db.SharedPreferenceUtil;
import connect.db.green.DaoHelper.ContactHelper;
import connect.db.green.bean.ContactEntity;
import connect.ui.activity.R;
import connect.ui.activity.chat.exts.ApplyJoinGroupActivity;
import connect.ui.activity.chat.exts.OuterWebsiteActivity;
import connect.ui.activity.chat.exts.TransferToActivity;
import connect.ui.activity.contact.FriendInfoActivity;
import connect.ui.activity.contact.StrangerInfoActivity;
import connect.ui.activity.contact.bean.SourceType;
import connect.ui.activity.login.bean.UserBean;
import connect.ui.activity.wallet.RequestActivity;
import connect.ui.activity.wallet.TransferAddressActivity;
import connect.utils.ActivityUtil;
import connect.utils.RegularUtil;
import connect.utils.ToastEUtil;
import connect.utils.UriUtil;
import connect.utils.cryption.DecryptionUtil;
import connect.utils.cryption.SupportKeyUril;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import protos.Connect;

/**
 * Scan Url parsing
 */

public class ResolveScanUtil {

    public static final String TYPE_WEB_GROUP_ = "group:";
    private String Request_Matches = "bitcoin:.*.?amount=.*";
    private String Url_Matches = "(?:(?:(?:[a-z]+:)?//))?(?:localhost|(?:25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9][0-9]|[0-9])(?:\\.(?:25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9][0-9]|[0-9])){3}|(?:(?:[a-z\\u00a1-\\uffff0-9]-*)*[a-z\\u00a1-\\uffff0-9]+)(?:\\.(?:[a-z\\u00a1-\\uffff0-9]-*)*[a-z\\u00a1-\\uffff0-9]+)*(?:\\.(?:[a-z\\u00a1-\\uffff]{2,}))\\.?)(?::\\d{2,5})?(?:[/?#][^\\s\"]*)?";
    private String Connect_Url = ".*.connect.im";
    private Activity activity;

    public ResolveScanUtil(Activity activity) {
        this.activity = activity;
    }

    /**
     * Scan the content
     * @param value
     */
    public void analysisUrl(String value){
        //Determine whether to link types
        if(RegularUtil.matches(value, Url_Matches)){
            try {
                URL url = new URL(value);
                String host = url.getHost();
                if(RegularUtil.matches(host, Connect_Url) && url.getQuery() != null && url.getQuery().contains("token")){
                    String[] pathArray = url.getPath().split("/");
                    String token = Uri.parse(value).getQueryParameter("token");

                    ScanResultBean resultBean = new ScanResultBean();
                    resultBean.setType(pathArray[pathArray.length-1]);
                    resultBean.setToken(token == null ? "" : token);
                    new ResolveUrlUtil(activity).dealResult(resultBean,true);
                }else{
                    OuterWebsiteActivity.startActivity(activity,value);
                    ActivityUtil.goBack(activity);
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            return;
        }

        // Determine whether to join the group of links
        if(value.contains(TYPE_WEB_GROUP_)){
            String[] valueArray = value.replace(TYPE_WEB_GROUP_,"").split("/");
            ApplyJoinGroupActivity.startActivity(activity, ApplyJoinGroupActivity.EApplyGroup.QRSCAN, valueArray[0],value);
            ActivityUtil.goBack(activity);
            return;
        }

        // Determine whether to address
        if(SupportKeyUril.checkAddress(value)){
            checkIsFriend(value,null,false);
            return;
        }

        // Determine whether to transfer links
        if(value.contains(RequestActivity.TRANSFER_SCAN_HEAD)) {
            String amount = null;
            String valueBitcoin = value.replace(RequestActivity.TRANSFER_SCAN_HEAD,"");
            if(valueBitcoin.contains("amount")) {
                amount = Uri.parse(valueBitcoin).getQueryParameter("amount");
                String[] data = value.split("\\?" + RequestActivity.TRANSFER_AMOUNT_HEAD);
                valueBitcoin = data[0].replace(RequestActivity.TRANSFER_SCAN_HEAD,"");
            }
            if(SupportKeyUril.checkAddress(valueBitcoin)){
                checkIsFriend(valueBitcoin,Double.valueOf(amount),true);
                return;
            }
            return;
        }

        ToastEUtil.makeText(activity, R.string.Login_scan_string_error);
    }

    /**
     * Check whether friends
     * @param address
     * @param amount
     */
    private void checkIsFriend(final String address, final Double amount, final boolean isTransfer) {
        new AsyncTask<Void, Void, ContactEntity>() {
            @Override
            protected ContactEntity doInBackground(Void... params) {
                return ContactHelper.getInstance().loadFriendEntity(address);
            }

            @Override
            protected void onPostExecute(ContactEntity friendEntity) {
                super.onPostExecute(friendEntity);
                if (friendEntity == null) {
                    requestUserInfo(address,amount,false);
                } else {
                    if(isTransfer){
                        TransferToActivity.startActivity(activity,address,amount);
                    }else {
                        FriendInfoActivity.startActivity(activity, friendEntity.getPub_key());
                    }
                    ActivityUtil.goBack(activity);
                }
            }
        }.execute();
    }

    /**
     * Request user information
     * @param address
     * @param amount
     * @param isTransfer
     */
    private void requestUserInfo(final String address, final Double amount, final boolean isTransfer) {
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
                        if(isTransfer){
                            TransferToActivity.startActivity(activity,address,amount);
                        }else{
                            StrangerInfoActivity.startActivity(activity, address, SourceType.QECODE);
                        }
                    }else{
                        TransferAddressActivity.startActivity(activity,address,amount);
                    }
                    ActivityUtil.goBack(activity);
                } catch (InvalidProtocolBufferException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Connect.HttpResponse response) {
                if(response.getCode() == 2404){
                    TransferAddressActivity.startActivity(activity,address,amount);
                }
                ActivityUtil.goBack(activity);
            }
        });
    }

}
