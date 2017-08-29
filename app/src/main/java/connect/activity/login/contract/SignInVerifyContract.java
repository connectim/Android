package connect.activity.login.contract;

import android.app.Activity;
import android.text.TextWatcher;

import connect.activity.login.bean.UserBean;
import connect.activity.base.contract.BasePresenter;
import connect.activity.base.contract.BaseView;

public interface SignInVerifyContract {

    interface View extends BaseView<SignInVerifyContract.Presenter> {
        Activity getActivity();

        String getCode();

        void setVoiceVisi();

        void goinCodeLogin(UserBean userBean);

        void goinRandomSend(String phone,String token);

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
