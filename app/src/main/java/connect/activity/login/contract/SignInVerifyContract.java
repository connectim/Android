package connect.activity.login.contract;

import android.app.Activity;
import android.text.TextWatcher;

import connect.activity.login.bean.UserBean;
import connect.activity.base.contract.BasePresenter;
import connect.activity.base.contract.BaseView;

/**
 * Created by Administrator on 2017/4/14 0014.
 */

public interface SignInVerifyContract {

    interface View extends BaseView<SignInVerifyContract.Presenter> {
        Activity getActivity();

        String getCode();

        void setVoiceVisi();

        void goinCodeLogin(UserBean userBean);

        void goinRandomSend(String phone,String token);

        void changeBtnNext();

        void changeBtnTiming(long time);

        void changeBtnFinsh();
    }

    interface Presenter extends BasePresenter {
        TextWatcher getEditChange();

        void requestVerifyCode();

        void reSendCode(int type);

        void requestBindMobile(String type);
    }

}
