package connect.activity.login.contract;

import android.app.Activity;

import java.util.HashMap;

import connect.activity.base.contract.BasePresenter;
import connect.activity.base.contract.BaseView;
import connect.activity.login.bean.UserBean;
import connect.utils.permission.PermissionUtil;

/**
 * Created by Administrator on 2017/4/14 0014.
 */

public interface RendomSendContract {

    interface View extends BaseView<RendomSendContract.Presenter> {
        Activity getActivity();

        void denyPression();

        void denyPressionDialog();

        void changeViewStatus(int status);

        void setProgressBar(float value);

        void goinRegister(UserBean userBean);
    }

    interface Presenter extends BasePresenter {
        void finishSuccess(HashMap<String, String> hashMap);

        PermissionUtil.ResultCallBack getPermissomCallBack();

        void releaseResource();
    }

}
