package connect.activity.login.presenter;

import android.app.Activity;
import android.os.Bundle;

import java.util.List;

import connect.activity.base.BaseApplication;
import connect.activity.login.bean.UserBean;
import connect.activity.login.contract.LoginPhoneVerifyContract;
import connect.activity.set.SafetyPhoneNumberActivity;
import connect.database.SharedPreferenceUtil;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.ExCountDownTimer;
import connect.utils.ProgressUtil;
import connect.utils.ToastEUtil;
import connect.utils.UriUtil;
import connect.utils.cryption.DecryptionUtil;
import connect.utils.cryption.EncryptionUtil;
import connect.utils.cryption.SupportKeyUril;
import connect.utils.okhttp.HttpRequest;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import connect.wallet.jni.AllNativeMethod;
import protos.Connect;

public class LoginPhoneVerifyPresenter implements LoginPhoneVerifyContract.Presenter {

    private LoginPhoneVerifyContract.View mView;
    private String phone;
    private int countryCode;
    private ExCountDownTimer exCountDownTimer;

    /**
     * The constructor.
     *
     * @param mView
     * @param countryCode country code
     * @param phone phone number
     */
    public LoginPhoneVerifyPresenter(LoginPhoneVerifyContract.View mView, String countryCode, String phone) {
        this.mView = mView;
        this.countryCode = Integer.valueOf(countryCode);
        this.phone = phone;
        mView.setPresenter(this);
    }

    @Override
    public void start() {
        countdownTime();
    }

    /**
     * Message authentication code.
     */
    public void requestVerifyCode() {
        ProgressUtil.getInstance().showProgress(mView.getActivity());
        final Connect.MobileVerify mobileVerify = Connect.MobileVerify.newBuilder()
                .setCountryCode(Integer.valueOf(countryCode))
                .setNumber(phone)
                .setCode(mView.getCode())
                .build();
        HttpRequest.getInstance().post(UriUtil.CONNECT_V2_SMS_VALIDATE, mobileVerify, new ResultCall<Connect.HttpNotSignResponse>() {
            @Override
            public void onResponse(Connect.HttpNotSignResponse response) {
                // 用户存在 走登录流程
                // 判断是否有二次密码认证
                // 1:新用户 2:老用户不需要验证密码 3:老用户需要验证密码
                try {
                    ProgressUtil.getInstance().dismissProgress();
                    Connect.SmsValidateResp smsValidateResp = Connect.SmsValidateResp.parseFrom(response.getBody());
                    switch (smsValidateResp.getStatus()){
                        case 1:
                            Bundle bundle = new Bundle();
                            bundle.putString("token", smsValidateResp.getToken());
                            bundle.putString("phone", countryCode + "-" + phone);
                            mView.launchRandomSend(countryCode + "-" + phone,smsValidateResp.getToken());
                            break;
                        case 2:
                            reSignInCa(smsValidateResp, countryCode + "-" + phone);
                            break;
                        case 3:
                            break;
                        default:
                            break;
                    }
                } catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Connect.HttpNotSignResponse response) {
                ProgressUtil.getInstance().dismissProgress();
                if(response.getCode() == 2416) {
                    // 验证码错误
                    ToastEUtil.makeText(mView.getActivity(), R.string.Login_Verification_code_error,ToastEUtil.TOAST_STATUS_FAILE).show();
                }
            }
        });
    }

    /**
     * 更新CA认证
     */
    private void reSignInCa(Connect.SmsValidateResp smsValidateResp, final String mobile){
        final String priKey = SupportKeyUril.getNewPriKey();
        final String pubKey = AllNativeMethod.cdGetPubKeyFromPrivKey(priKey);
        Connect.UpdateCa updateCa = Connect.UpdateCa.newBuilder()
                .setCaPub(pubKey)
                .setMobile(mobile)
                .setToken(smsValidateResp.getToken())
                .build();
        Connect.IMRequest imRequest = OkHttpUtil.getInstance().getIMRequest(EncryptionUtil.ExtendedECDH.EMPTY, priKey, pubKey, updateCa.toByteString());
        HttpRequest.getInstance().post(UriUtil.CONNECT_V2_SIGN_IN_CA, imRequest, new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                try {
                    Connect.IMResponse imResponse = Connect.IMResponse.parseFrom(response.getBody().toByteArray());
                    Connect.StructData structData = DecryptionUtil.decodeAESGCMStructData(EncryptionUtil.ExtendedECDH.EMPTY,
                            priKey, imResponse.getCipherData());
                    Connect.UserInfo userInfo = Connect.UserInfo.parseFrom(structData.getPlainData());
                    UserBean userBean = new UserBean();
                    userBean.setAvatar(userInfo.getAvatar());
                    userBean.setConnectId(userInfo.getConnectId());
                    userBean.setName(userInfo.getUsername());
                    userBean.setPhone(mobile);
                    userBean.setPriKey(priKey);
                    userBean.setPubKey(pubKey);
                    userBean.setUid(userInfo.getUid());
                    SharedPreferenceUtil.getInstance().putUser(userBean);
                    mView.launchHome(userBean);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Connect.HttpResponse response) {
                ToastEUtil.makeText(mView.getActivity(), response.getMessage(), ToastEUtil.TOAST_STATUS_FAILE);
            }
        });
    }

    /**
     * send sms code
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
        exCountDownTimer = new ExCountDownTimer(120*1000,1000){
            @Override
            public void onTick(long millisUntilFinished, int percent) {
                mView.changeBtnTiming(millisUntilFinished / 1000);
            }

            @Override
            public void onPause() {}

            @Override
            public void onFinish() {
                mView.changeBtnFinish();
            }
        };
        exCountDownTimer.start();
    }

    @Override
    public void pauseDownTimer(){
        exCountDownTimer.pause();
    }

    @Override
    public void requestBindMobile(final String type){
        ProgressUtil.getInstance().showProgress(mView.getActivity());
        Connect.MobileVerify mobileVerify = Connect.MobileVerify.newBuilder()
                .setCountryCode(Integer.valueOf(countryCode))
                .setNumber(phone)
                .setCode(mView.getCode())
                .build();
        String url = "";
        if (type.equals(SafetyPhoneNumberActivity.LINK_TYPE)) {
            url = UriUtil.SETTING_BIND_MOBILE;
        } else if(type.equals(SafetyPhoneNumberActivity.UNLINK_TYPE)) {
            url = UriUtil.SETTING_UNBIND_MOBILE;
        }
        OkHttpUtil.getInstance().postEncrySelf(url, mobileVerify, new ResultCall<Connect.HttpNotSignResponse>() {
            @Override
            public void onResponse(Connect.HttpNotSignResponse response) {
                ProgressUtil.getInstance().dismissProgress();
                UserBean userBean = SharedPreferenceUtil.getInstance().getUser();
                if (type.equals(SafetyPhoneNumberActivity.LINK_TYPE)) {
                    userBean.setPhone(countryCode + "-" + phone);
                }else if (type.equals(SafetyPhoneNumberActivity.UNLINK_TYPE)) {
                    userBean.setPhone("");
                }
                ToastEUtil.makeText(mView.getActivity(),R.string.Set_Set_success).show();
                SharedPreferenceUtil.getInstance().putUser(userBean);
                List<Activity> list = BaseApplication.getInstance().getActivityList();
                for (Activity activity : list) {
                    if (activity.getClass().getName().equals(SafetyPhoneNumberActivity.class.getName())) {
                        activity.finish();
                    }
                }
                ActivityUtil.goBack(mView.getActivity());
            }

            @Override
            public void onError(Connect.HttpNotSignResponse response) {
                ProgressUtil.getInstance().dismissProgress();
                if (response.getCode() == 2414) {
                    ToastEUtil.makeText(mView.getActivity(),R.string.Login_Phone_binded).show();
                } else {
                    ToastEUtil.makeText(mView.getActivity(),R.string.Link_update_Failed).show();
                }
            }
        });
    }

}
