package connect.activity.login.contract;

import android.app.Activity;

import connect.activity.login.bean.UserBean;
import connect.activity.base.contract.BasePresenter;
import connect.activity.base.contract.BaseView;

public interface LoginPhoneVerifyContract {

    interface View extends BaseView<LoginPhoneVerifyContract.Presenter> {
        Activity getActivity();

        String getCode();

        void setVoiceVisi();

        void launchCodeLogin(UserBean userBean);

        void launchRandomSend(String phone,String token);

        void changeBtnTiming(long time);

        void changeBtnFinish();
    }

    interface Presenter extends BasePresenter {
        void requestVerifyCode();

        void reSendCode(int type);

        void requestBindMobile(String type);

        void pauseDownTimer();
    }

}
