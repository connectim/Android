package connect.utils.scan;

import android.app.Activity;
import android.net.Uri;
import android.os.AsyncTask;

import com.google.protobuf.InvalidProtocolBufferException;

import java.net.URL;
import connect.activity.chat.exts.ApplyJoinGroupActivity;
import connect.activity.chat.exts.OuterWebsiteActivity;
import connect.activity.chat.exts.TransferToActivity;
import connect.activity.contact.FriendInfoActivity;
import connect.activity.contact.StrangerInfoActivity;
import connect.activity.contact.bean.SourceType;
import connect.activity.wallet.TransferAddressActivity;
import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.bean.ContactEntity;
import connect.ui.activity.R;
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

    private Activity activity;
    public static String CONNECT_HEAD = "con_profile://";
    private final String TYPE_WEB_GROUP_ = "group:";
    private final String Url_Matches = "(?:(?:(?:[a-z]+:)?//))?(?:localhost|(?:25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9][0-9]|[0-9])(?:\\.(?:25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9][0-9]|[0-9])){3}|(?:(?:[a-z\\u00a1-\\uffff0-9]-*)*[a-z\\u00a1-\\uffff0-9]+)(?:\\.(?:[a-z\\u00a1-\\uffff0-9]-*)*[a-z\\u00a1-\\uffff0-9]+)*(?:\\.(?:[a-z\\u00a1-\\uffff]{2,}))\\.?)(?::\\d{2,5})?(?:[/?#][^\\s\"]*)?";
    private final String Url_Packet_Transfer_Group =  "(http|https)://.*.connect.im/share/v1/(packet|transfer|group)\\?token=.+";
    private final String TRANSFER_SCAN_HEAD = "bitcoin:";

    public ResolveScanUtil(Activity activity) {
        this.activity = activity;
    }

    /**
     * Scan the content
     * @param value
     */
    public void analysisUrl(final String value){
        if(RegularUtil.matches(value, Url_Matches)){
            //Determine whether to link types
            dealScanUrl(value);
        }else if(value.contains(TYPE_WEB_GROUP_)){
            // Determine whether to join the group of links
            dealScanGroup(value);
        }/*else if(value.contains(TRANSFER_SCAN_HEAD)){
            // Determine whether to transfer links
            dealScanTransfer(value);
        }*/else {
            dealScanValue(value);
        }
    }

    /**
     * Two-dimensional code is a web link
     */
    private void dealScanUrl(String value){
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
    }

    /**
     * Two-dimensional code is a group application link
     */
    private void dealScanGroup(String value){
        String[] valueArray = value.replace(TYPE_WEB_GROUP_,"").split("/");
        ApplyJoinGroupActivity.startActivity(activity, ApplyJoinGroupActivity.EApplyGroup.QRSCAN, valueArray[0],value);
        ActivityUtil.goBack(activity);
    }

    /**
     * The two-dimensional code is the transfer link
     */
    private void dealScanTransfer(String value){
        Double amount = null;
        String valueBitCoin = value.replace(TRANSFER_SCAN_HEAD,"");
        if(valueBitCoin.contains("amount")) {
            String amountStr = Uri.parse(valueBitCoin).getQueryParameter("amount");
            amount = Double.valueOf(amountStr);
            String[] data = value.split("\\?" + "amount=");
            valueBitCoin = data[0].replace(TRANSFER_SCAN_HEAD,"");
        }
        if(SupportKeyUril.checkAddress(valueBitCoin)){
            TransferAddressActivity.startActivity(activity,valueBitCoin,amount);
            return;
        }
    }

    /**
     * The two-dimensional code is connect id
     */
    private void dealScanValue(final String value){
        requestUserInfo(value, new OnResultBack() {
            @Override
            public void call(int status, String uid) {
                switch (status){
                    case 1:
                        FriendInfoActivity.startActivity(activity, uid);
                        break;
                    case 2:
                        StrangerInfoActivity.startActivity(activity, uid, SourceType.QECODE);
                        break;
                    case 3:
                        ToastEUtil.makeText(activity, R.string.Login_scan_string_error, ToastEUtil.TOAST_STATUS_FAILE).show();
                        break;
                    default:
                        break;
                }
                ActivityUtil.goBack(activity);
            }
        });
    }

    /**
     * Request user information
     * @param value
     * @param onResultBack
     */
    private void requestUserInfo(String value, final OnResultBack onResultBack) {
        if(!value.contains(CONNECT_HEAD)){
            onResultBack.call(3, "");
            return;
        }else{
            value = value.replace(CONNECT_HEAD, "");
        }

        ContactEntity contactEntity = ContactHelper.getInstance().loadFriendEntity(value);
        if(contactEntity != null){
            onResultBack.call(1, contactEntity.getUid());
            return;
        }

        final Connect.SearchUser searchUser = Connect.SearchUser.newBuilder()
                .setTyp(2)
                .setCriteria(value)
                .build();
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.CONNECT_V1_USER_SEARCH, searchUser, new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                try {
                    Connect.IMResponse imResponse = Connect.IMResponse.parseFrom(response.getBody().toByteArray());
                    Connect.StructData structData = DecryptionUtil.decodeAESGCMStructData(imResponse.getCipherData());
                    Connect.UserInfo sendUserInfo = Connect.UserInfo.parseFrom(structData.getPlainData());
                    if(sendUserInfo != null){
                        onResultBack.call(2, sendUserInfo.getUid());
                    }else{
                        onResultBack.call(3, "");
                    }
                } catch (InvalidProtocolBufferException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Connect.HttpResponse response) {
                if(response.getCode() == 2404){
                    onResultBack.call(3, "");
                }
            }
        });
    }

    /**
     * status (1:friend 2:stranger 3:no found)
     */
    public interface OnResultBack{
        void call(int status, String uid);
    }

}
