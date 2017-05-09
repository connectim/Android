package connect.ui.activity.login.presenter;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Base64;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import connect.ui.activity.R;
import connect.ui.activity.login.bean.UserBean;
import connect.ui.activity.login.contract.ScanLoginContract;
import connect.ui.activity.set.presenter.BackUpPresenter;
import connect.utils.ProgressUtil;
import connect.utils.ToastEUtil;
import connect.utils.ToastUtil;
import connect.utils.UriUtil;
import connect.utils.cryption.DecryptionUtil;
import connect.utils.cryption.SupportKeyUril;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import protos.Connect;

/**
 * Created by Administrator on 2017/4/13 0013.
 */

public class ScanLoginPresenter implements ScanLoginContract.Presenter{

    public final int PARSE_BARCODE_SUC = 601;
    private final ScanLoginContract.View mView;

    public ScanLoginPresenter(ScanLoginContract.View mView) {
        this.mView = mView;
    }

    @Override
    public void start() {

    }

    @Override
    public Handler getHandle() {
        return mLocalHandler;
    }

    public Handler mLocalHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            ProgressUtil.getInstance().dismissProgress();
            switch (msg.what){
                case PARSE_BARCODE_SUC:
                    checkString((String) msg.obj);
                    break;
            }
        }
    };

    @Override
    public void checkString(String value) {
        if(SupportKeyUril.checkPrikey(value)){
            requestExisted(value);
            return;
        }

        try {
            String enStr = value.replace(BackUpPresenter.scanHead,"");
            if(TextUtils.isEmpty(enStr)){
                ToastUtil.getInstance().showToast(R.string.Login_scan_string_error);
            }else{
                byte[] byteArrayDe = Base64.decode(enStr, Base64.DEFAULT);
                Connect.ExoprtPrivkeyQrcode privkeyQrcode = Connect.ExoprtPrivkeyQrcode.parseFrom(byteArrayDe);
                switch (privkeyQrcode.getVersion()){
                    case 1:
                    case 2:
                        UserBean userBean = new UserBean();
                        userBean.setName(privkeyQrcode.getUsername());
                        userBean.setTalkKey(privkeyQrcode.getEncriptionPri());
                        userBean.setPassHint(privkeyQrcode.getPasswordHint());
                        userBean.setPhone(privkeyQrcode.getPhone());
                        userBean.setAvatar("https://short.connect.im/avatar/v1/"
                                + privkeyQrcode.getAvatar() + ".jpg");
                        userBean.setConnectId(privkeyQrcode.getConnectId());
                        mView.goinCodeLogin(userBean,"");
                        break;
                    default:
                        ToastEUtil.makeText(mView.getActivity(),R.string.Login_Invalid_version_number,ToastEUtil.TOAST_STATUS_FAILE).show();
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            ToastEUtil.makeText(mView.getActivity(),R.string.Login_scan_string_error,ToastEUtil.TOAST_STATUS_FAILE).show();
        }
    }

    private void requestExisted(final String priKey){
        OkHttpUtil.getInstance().postEncry(UriUtil.CONNECT_V1_PRIVATE_EXISTED,
                ByteString.copyFrom(SupportKeyUril.createrBinaryRandom()),
                SupportKeyUril.EcdhExts.EMPTY,
                priKey,
                SupportKeyUril.getPubKeyFromPriKey(priKey),
                new ResultCall<Connect.HttpResponse>() {
                    @Override
                    public void onResponse(Connect.HttpResponse response) {
                        try {
                            Connect.IMResponse imResponse = Connect.IMResponse.parseFrom(response.getBody().toByteArray());
                            Connect.StructData structData = DecryptionUtil.decodeAESGCMStructData(SupportKeyUril.EcdhExts.EMPTY,priKey,imResponse.getCipherData());
                            Connect.UserExistedToken existedToken = Connect.UserExistedToken.parseFrom(structData.getPlainData());
                            Connect.UserInfo userInfo = existedToken.getUserInfo();
                            UserBean userBean = new UserBean();
                            userBean.setAddress(userInfo.getAddress());
                            userBean.setName(userInfo.getUsername());
                            userBean.setAvatar(userInfo.getAvatar());
                            userBean.setPriKey(priKey);
                            userBean.setPubKey(userInfo.getPubKey());
                            userBean.setConnectId(userInfo.getConnectId());
                            mView.goinCodeLogin(userBean,existedToken.getToken());
                        } catch (InvalidProtocolBufferException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Connect.HttpResponse response) {
                        if(response.getCode() == 2404){
                            mView.goinRegister(priKey);
                        }
                    }
                });
    }

    public interface OnScanLoginListence{
        Activity getActivity();

        void goinCodeLogin(UserBean userBean,String token);

        void goinRegister(String priKey);
    }

}
