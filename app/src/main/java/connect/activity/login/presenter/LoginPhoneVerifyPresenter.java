package connect.activity.login.presenter;

import android.os.Bundle;
import android.text.TextUtils;

import connect.activity.login.bean.CaPubBean;
import connect.activity.login.bean.UserBean;
import connect.activity.login.contract.LoginPhoneVerifyContract;
import connect.database.SharedPreferenceUser;
import connect.database.SharedPreferenceUtil;
import connect.database.green.DaoHelper.ParamManager;
import connect.ui.activity.R;
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
                try {
                    ProgressUtil.getInstance().dismissProgress();
                    Connect.SmsValidateResp smsValidateResp = Connect.SmsValidateResp.parseFrom(response.getBody());
                    Connect.UserInfo userInfo = smsValidateResp.getUserInfo();
                    CaPubBean caPubBean = SharedPreferenceUser.getInstance(userInfo.getUid()).getCaPubBean();
                    switch (smsValidateResp.getStatus()){
                        case 1:
                            Bundle bundle = new Bundle();
                            bundle.putString("token", smsValidateResp.getToken());
                            bundle.putString("phone", countryCode + "-" + phone);
                            mView.launchRandomSend(countryCode + "-" + phone,smsValidateResp.getToken());
                            break;
                        case 2:
                            if(TextUtils.isEmpty(caPubBean.getPubKey())
                                    || TextUtils.isEmpty(userInfo.getPubKey())
                                    || !userInfo.getPubKey().equals(caPubBean.getPubKey())){
                                reSignInCa(smsValidateResp, countryCode + "-" + phone);
                            }else{
                                UserBean userBean = new UserBean(userInfo.getUsername(), userInfo.getAvatar(), caPubBean.getPriKey(), caPubBean.getPubKey(),
                                        countryCode + "-" + phone, userInfo.getConnectId(), userInfo.getUid(), userInfo.getUpdateConnectId());
                                SharedPreferenceUtil.getInstance().putUser(userBean);
                                mView.launchHome(userBean);
                            }
                            break;
                        case 3:
                            if(TextUtils.isEmpty(caPubBean.getPubKey())
                                    || TextUtils.isEmpty(userInfo.getPubKey())
                                    || !userInfo.getPubKey().equals(caPubBean.getPubKey())){
                                mView.launchPassVerify(countryCode + "-" + phone, smsValidateResp.getToken(), true);
                            }else{
                                mView.launchPassVerify(countryCode + "-" + phone, smsValidateResp.getToken(), false);
                            }
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
                    ToastEUtil.makeText(mView.getActivity(), R.string.Login_Verification_code_error,ToastEUtil.TOAST_STATUS_FAILE).show();
                }
            }
        });
    }

    /**
     * update CA data
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
                    UserBean userBean = new UserBean(userInfo.getUsername(), userInfo.getAvatar(), priKey, pubKey,
                            mobile, userInfo.getConnectId(), userInfo.getUid(), userInfo.getUpdateConnectId());

                    SharedPreferenceUser.getInstance(userInfo.getUid()).putCaPubBean(new CaPubBean(priKey, pubKey));
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
}
