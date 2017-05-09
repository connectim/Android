package connect.ui.activity.login.contract;

import android.app.Activity;

import java.util.HashMap;

import connect.ui.base.BasePresenter;
import connect.ui.base.BaseView;
import connect.utils.permission.PermissiomUtilNew;

/**
 * Created by Administrator on 2017/4/14 0014.
 */

public interface RendomSendContract {

    interface View extends BaseView<RendomSendContract.Presenter> {
        Activity getActivity();

        void denyPression();

        void denyPressionDialog();

        void changeViewStatus(int status);

        void finishSuccess(HashMap<String, String> hashMap);

        void setProgressBar(float value);
    }

    interface Presenter extends BasePresenter {
        PermissiomUtilNew.ResultCallBack getPermissomCallBack();

        void releaseResource();
    }

}
