package connect.activity.set.contract;

import android.app.Activity;
import android.graphics.Bitmap;

import connect.activity.login.bean.UserBean;
import connect.activity.base.contract.BasePresenter;
import connect.activity.base.contract.BaseView;

/**
 * Created by Administrator on 2017/4/17 0017.
 */

public interface SafetyBackupContract {

    interface View extends BaseView<SafetyBackupContract.Presenter> {
        Activity getActivity();

    }

    interface Presenter extends BasePresenter {
        void saveBackup(Bitmap scanBitmap,UserBean userBean);

        String getEncryStr(UserBean userBean);
    }

}
