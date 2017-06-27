package connect.activity.login.presenter;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import java.util.List;
import connect.database.SharedPreferenceUtil;
import connect.ui.activity.R;
import connect.activity.login.bean.UserBean;
import connect.activity.login.contract.SignInVerifyContract;
import connect.activity.set.LinkChangePhoneActivity;
import connect.activity.base.BaseApplication;
import connect.utils.ActivityUtil;
import connect.utils.ExCountDownTimer;
import connect.utils.ProgressUtil;
import connect.utils.ProtoBufUtil;
import connect.utils.ToastEUtil;
import connect.utils.UriUtil;
import connect.utils.okhttp.HttpRequest;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import protos.Connect;

/**
 * Created by Administrator on 2017/4/13 0013.
 */

public class SignInVerifyPresenter implements SignInVerifyContract.Presenter{

    private SignInVerifyContract.View mView;
    private final int CODE_PHONE_ABSENT = 2404;
    private String phone;
    private int countryCode;

    public SignInVerifyPresenter(SignInVerifyContract.View mView,
                                 String countryCode, String phone) {
        this.mView = mView;
        this.countryCode = Integer.valueOf(countryCode);
        this.phone = phone;
    }

    @Override
    public void start() {
        countdownTime();
    }

    public TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s.toString().length() == 6) {
                requestVerifyCode();
            }
            mView.changeBtnNext();
        }
    };

    @Override
    public TextWatcher getEditChange() {
        return textWatcher;
    }

    /**
     * Message authentication code
     */
    public void requestVerifyCode() {
        ProgressUtil.getInstance().showProgress(mView.getActivity());
        final Connect.MobileVerify mobileVerify = Connect.MobileVerify.newBuilder()
                .setCountryCode(Integer.valueOf(countryCode))
                .setNumber(phone)
                .setCode(mView.getCode())
                .build();
        HttpRequest.getInstance().post(UriUtil.CONNECT_V1_SIGN_IN, mobileVerify, new ResultCall<Connect.HttpNotSignResponse>() {
            @Override
            public void onResponse(Connect.HttpNotSignResponse response) {
                ProgressUtil.getInstance().dismissProgress();
                try {
                    Connect.UserInfoDetail userInfoDetail = Connect.UserInfoDetail.parseFrom(response.getBody());
                    if(ProtoBufUtil.getInstance().checkProtoBuf(userInfoDetail)){
                        UserBean userBean = new UserBean();
                        userBean.setPhone(countryCode + "-" + phone);
                        userBean.setAvatar(userInfoDetail.getAvatar());
                        userBean.setName(userInfoDetail.getUsername());
                        userBean.setTalkKey(userInfoDetail.getEncryptionPri());
                        userBean.setPassHint(userInfoDetail.getPasswordHint());
                        userBean.setConnectId(userInfoDetail.getConnectId());
                        mView.goinCodeLogin(userBean);
                    }
                } catch (InvalidProtocolBufferException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Connect.HttpNotSignResponse response) {
                ProgressUtil.getInstance().dismissProgress();
                if(response.getCode() == 2416){
                    ToastEUtil.makeText(mView.getActivity(), R.string.Login_Verification_code_error,ToastEUtil.TOAST_STATUS_FAILE).show();
                    return;
                }

                if (response.getCode() == CODE_PHONE_ABSENT) {
                    ByteString bytes = response.getBody();
                    Connect.SecurityToken securityToken;
                    try {
                        securityToken = Connect.SecurityToken.parseFrom(bytes);
                        Bundle bundle = new Bundle();
                        bundle.putString("token", securityToken.getToken());
                        bundle.putString("phone", countryCode + "-" + phone);
                        mView.goinRandomSend(countryCode + "-" + phone,securityToken.getToken());
                    } catch (InvalidProtocolBufferException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    /**
     *
     * @param type 1：sms  2：voice
     */
    public void reSendCode(int type) {
        Connect.SendMobileCode sendMobileCode = Connect.SendMobileCode.newBuilder()
                .setMobile(countryCode + "-" + phone)
                .setCategory(type).build();
        HttpRequest.getInstance().post(UriUtil.CONNECT_V1_SMS_SEND, sendMobileCode, new ResultCall<Connect.HttpNotSignResponse>() {
            @Override
            public void onResponse(Connect.HttpNotSignResponse response) {
                ToastEUtil.makeText(mView.getActivity(),R.string.Login_SMS_code_has_been_send).show();
                countdownTime();
            }

            @Override
            public void onError(Connect.HttpNotSignResponse response) {
                if(response.getCode() == 2400){
                    ToastEUtil.makeText(mView.getActivity(),R.string.Link_Operation_frequent,ToastEUtil.TOAST_STATUS_FAILE).show();
                }else{
                    ToastEUtil.makeText(mView.getActivity(),R.string.Login_SMS_code_sent_failure,ToastEUtil.TOAST_STATUS_FAILE).show();
                }
            }
        });
    }

    private void countdownTime(){
        ExCountDownTimer exCountDownTimer = new ExCountDownTimer(120*1000,1000){
            @Override
            public void onTick(long millisUntilFinished, int percent) {
                mView.changeBtnTiming(millisUntilFinished/1000);
            }

            @Override
            public void onPause() {

            }

            @Override
            public void onFinish() {
                mView.changeBtnFinsh();
            }
        };
        exCountDownTimer.start();
    }

    @Override
    public void requestBindMobile(final String type){
        Connect.MobileVerify mobileVerify = Connect.MobileVerify.newBuilder()
                .setCountryCode(Integer.valueOf(countryCode))
                .setNumber(phone)
                .setCode(mView.getCode())
                .build();
        String url = "";
        if(type.equals(LinkChangePhoneActivity.LINK_TYPE)){
            url = UriUtil.SETTING_BIND_MOBILE;
        }else if(type.equals(LinkChangePhoneActivity.UNLINK_TYPE)) {
            url = UriUtil.SETTING_UNBIND_MOBILE;
        }
        OkHttpUtil.getInstance().postEncrySelf(url, mobileVerify, new ResultCall<Connect.HttpNotSignResponse>() {
            @Override
            public void onResponse(Connect.HttpNotSignResponse response) {
                UserBean userBean = SharedPreferenceUtil.getInstance().getUser();
                if(type.equals(LinkChangePhoneActivity.LINK_TYPE)){
                    userBean.setPhone(countryCode + "-" + phone);
                }else if(type.equals(LinkChangePhoneActivity.UNLINK_TYPE)) {
                    userBean.setPhone("");
                }
                ToastEUtil.makeText(mView.getActivity(),R.string.Set_Set_success).show();
                SharedPreferenceUtil.getInstance().putUser(userBean);
                List<Activity> list = BaseApplication.getInstance().getActivityList();
                for (Activity activity : list) {
                    if (activity.getClass().getName().equals(LinkChangePhoneActivity.class.getName())){
                        activity.finish();
                    }
                }
                ActivityUtil.goBack(mView.getActivity());
            }

            @Override
            public void onError(Connect.HttpNotSignResponse response) {
                if(response.getCode() == 2414){
                    ToastEUtil.makeText(mView.getActivity(),R.string.Login_Phone_binded).show();
                }else{
                    ToastEUtil.makeText(mView.getActivity(),R.string.Link_update_Failed).show();
                }
            }
        });
    }

}
