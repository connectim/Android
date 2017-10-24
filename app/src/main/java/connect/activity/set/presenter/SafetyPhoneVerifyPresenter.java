package connect.activity.set.presenter;

import android.app.Activity;

import java.util.List;

import connect.activity.base.BaseApplication;
import connect.activity.login.bean.UserBean;
import connect.activity.set.SafetyPhoneNumberActivity;
import connect.activity.set.contract.SafetyPhoneVerifyContract;
import connect.database.SharedPreferenceUtil;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.ExCountDownTimer;
import connect.utils.ProgressUtil;
import connect.utils.ToastEUtil;
import connect.utils.UriUtil;
import connect.utils.okhttp.HttpRequest;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import protos.Connect;

public class SafetyPhoneVerifyPresenter implements SafetyPhoneVerifyContract.Presenter {

    private SafetyPhoneVerifyContract.View mView;
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
    public SafetyPhoneVerifyPresenter(SafetyPhoneVerifyContract.View mView, String countryCode, String phone) {
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

}
