package connect.activity.set.contract;

import android.app.Activity;
import android.graphics.Bitmap;

import connect.activity.login.bean.UserBean;
import connect.activity.base.BasePresenter;
import connect.activity.base.BaseView;

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
