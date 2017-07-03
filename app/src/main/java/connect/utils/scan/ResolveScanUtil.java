package connect.utils.scan;

import android.app.Activity;
import android.net.Uri;
import android.os.AsyncTask;

import com.google.protobuf.InvalidProtocolBufferException;

import java.net.URL;

import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.bean.ContactEntity;
import connect.ui.activity.R;
import connect.activity.chat.exts.ApplyJoinGroupActivity;
import connect.activity.chat.exts.OuterWebsiteActivity;
import connect.activity.chat.exts.TransferToActivity;
import connect.activity.contact.FriendInfoActivity;
import connect.activity.contact.StrangerInfoActivity;
import connect.activity.contact.bean.SourceType;
import connect.activity.wallet.RequestActivity;
import connect.activity.wallet.TransferAddressActivity;
import connect.utils.ActivityUtil;
import connect.utils.ProtoBufUtil;
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
    private String Url_Matches = "(?:(?:(?:[a-z]+:)?//))?(?:localhost|(?:25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9][0-9]|[0-9])(?:\\.(?:25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9][0-9]|[0-9])){3}|(?:(?:[a-z\\u00a1-\\uffff0-9]-*)*[a-z\\u00a1-\\uffff0-9]+)(?:\\.(?:[a-z\\u00a1-\\uffff0-9]-*)*[a-z\\u00a1-\\uffff0-9]+)*(?:\\.(?:[a-z\\u00a1-\\uffff]{2,}))\\.?)(?::\\d{2,5})?(?:[/?#][^\\s\"]*)?";
    private String Url_Packet_Transfer_Group =  "(http|https)://.*.connect.im/share/v1/(packet|transfer|group)\\?token=.+";
    private Activity activity;
    private final String ID_FRIEND = "friend";
    private final String ID_STRANGER = "stranger";
    private final String ID_INEXISTENCE = "inexistence";

    public ResolveScanUtil(Activity activity) {
        this.activity = activity;
    }

    /**
     * Scan the content
     * @param value
     */
    public void analysisUrl(final String value){
        //Determine whether to link types
        if(RegularUtil.matches(value, Url_Matches)){
            try {
                URL url = new URL(value);
                if(RegularUtil.matches(value,Url_Packet_Transfer_Group)){
                    String[] pathArray = url.getPath().split("/");
                    String token = Uri.parse(value).getQueryParameter("token");

                    ScanResultBean resultBean = new ScanResultBean();
                    resultBean.setType(pathArray[pathArray.length-1]);
                    resultBean.setToken(token);
                    resultBean.setTip(ResolveUrlUtil.TYPE_OPEN_SCAN);
                    new ResolveUrlUtil(activity).dealResult(resultBean,true);
                }else{
                    OuterWebsiteActivity.startActivity(activity,value);
                    ActivityUtil.goBack(activity);
                }
            } catch (Exception e) {
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
            checkIsFriend(value, new OnResultBack() {
                @Override
                public void call(String status) {
                    switch (status){
                        case ID_FRIEND:
                            FriendInfoActivity.startActivity(activity, value);
                            break;
                        case ID_STRANGER:
                            StrangerInfoActivity.startActivity(activity, value, SourceType.QECODE);
                            break;
                        case ID_INEXISTENCE:
                            TransferAddressActivity.startActivity(activity,value,null);
                            break;
                        default:
                            break;
                    }
                    ActivityUtil.goBack(activity);
                }
            });
            return;
        }

        // Determine whether to transfer links
        if(value.contains(RequestActivity.TRANSFER_SCAN_HEAD)) {
            Double amount = null;
            String valueBitcoin = value.replace(RequestActivity.TRANSFER_SCAN_HEAD,"");
            if(valueBitcoin.contains("amount")) {
                String amountStr = Uri.parse(valueBitcoin).getQueryParameter("amount");
                amount = Double.valueOf(amountStr);
                String[] data = value.split("\\?" + RequestActivity.TRANSFER_AMOUNT_HEAD);
                valueBitcoin = data[0].replace(RequestActivity.TRANSFER_SCAN_HEAD,"");
            }
            if(SupportKeyUril.checkAddress(valueBitcoin)){
                final String finalValueBitcoin = valueBitcoin;
                final Double finalAmount = amount;
                checkIsFriend(valueBitcoin, new OnResultBack() {
                    @Override
                    public void call(String status) {
                        switch (status){
                            case ID_FRIEND:
                                TransferToActivity.startActivity(activity, finalValueBitcoin, finalAmount);
                                break;
                            case ID_STRANGER:
                                TransferToActivity.startActivity(activity,finalValueBitcoin,finalAmount);
                                break;
                            case ID_INEXISTENCE:
                                TransferAddressActivity.startActivity(activity,finalValueBitcoin,finalAmount);
                                break;
                            default:
                                break;
                        }
                        ActivityUtil.goBack(activity);
                    }
                });
                return;
            }
            return;
        }

        ToastEUtil.makeText(activity, R.string.Login_scan_string_error);
    }

    /**
     * Check whether friends
     * @param address
     * @param onResultBack
     */
    private void checkIsFriend(final String address, final OnResultBack onResultBack) {
        new AsyncTask<Void, Void, ContactEntity>() {
            @Override
            protected ContactEntity doInBackground(Void... params) {
                return ContactHelper.getInstance().loadFriendEntity(address);
            }

            @Override
            protected void onPostExecute(ContactEntity friendEntity) {
                super.onPostExecute(friendEntity);
                if (friendEntity == null) {
                    requestUserInfo(address,onResultBack);
                } else {
                    onResultBack.call(ID_FRIEND);
                }
            }
        }.execute();
    }

    /**
     * Request user information
     * @param address
     * @param onResultBack
     */
    private void requestUserInfo(final String address, final OnResultBack onResultBack) {
        final Connect.SearchUser searchUser = Connect.SearchUser.newBuilder()
                .setCriteria(address)
                .build();
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.CONNECT_V1_USER_SEARCH, searchUser, new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                try {
                    Connect.IMResponse imResponse = Connect.IMResponse.parseFrom(response.getBody().toByteArray());
                    Connect.StructData structData = DecryptionUtil.decodeAESGCMStructData(imResponse.getCipherData());
                    Connect.UserInfo sendUserInfo = Connect.UserInfo.parseFrom(structData.getPlainData());
                    if(sendUserInfo != null && ProtoBufUtil.getInstance().checkProtoBuf(sendUserInfo)){
                        onResultBack.call(ID_STRANGER);
                    }else{
                        onResultBack.call(ID_INEXISTENCE);
                    }
                } catch (InvalidProtocolBufferException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Connect.HttpResponse response) {
                if(response.getCode() == 2404){
                    onResultBack.call(ID_INEXISTENCE);
                }
            }
        });
    }

    public interface OnResultBack{
        void call(String status);
    }

}
