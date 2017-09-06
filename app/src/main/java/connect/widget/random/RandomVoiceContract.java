package connect.widget.random;

import android.app.Activity;

import connect.activity.base.contract.BasePresenter;
import connect.activity.base.contract.BaseView;
import connect.utils.permission.PermissionUtil;

public interface RandomVoiceContract {

    interface View extends BaseView<RandomVoiceContract.Presenter> {
        Activity getActivity();

        void denyPression();

        void denyPressionDialog();

        void changeViewStatus(int status);

        void setProgressBar(float value);

        void successCollect(String random);
    }

    interface Presenter extends BasePresenter {
        void finishSuccess(String random);

        PermissionUtil.ResultCallBack getPermissomCallBack();

        void releaseResource();
    }

}
