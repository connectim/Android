package connect.activity.set.contract;

import android.app.Activity;
import android.text.TextWatcher;
import connect.activity.base.contract.BasePresenter;
import connect.activity.base.contract.BaseView;

/**
 * Created by Administrator on 2017/4/17 0017.
 */

public interface ModifyPassContract {

    interface View extends BaseView<ModifyPassContract.Presenter> {
        void setRightTextEnable(boolean isEnable);

        void setHint(String value);

        Activity getActivity();
    }

    interface Presenter extends BasePresenter {

        TextWatcher getPassTextChange();

        void requestPass(String pass,String hint);
    }

}
