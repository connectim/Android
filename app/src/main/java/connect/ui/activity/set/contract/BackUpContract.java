package connect.ui.activity.set.contract;

import android.app.Activity;
import android.graphics.Bitmap;

import connect.ui.activity.login.bean.UserBean;
import connect.ui.base.BasePresenter;
import connect.ui.base.BaseView;

/**
 * Created by Administrator on 2017/4/17 0017.
 */

public interface BackUpContract {

    interface View extends BaseView<BackUpContract.Presenter> {
        Activity getActivity();

    }

    interface Presenter extends BasePresenter {
        void saveBackup(Bitmap scanBitmap,UserBean userBean);

        String getEncryStr(UserBean userBean);
    }

}
