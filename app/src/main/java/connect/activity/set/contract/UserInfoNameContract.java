package connect.activity.set.contract;

import android.app.Activity;

import connect.activity.base.contract.BasePresenter;
import connect.activity.base.contract.BaseView;

/**
 * Created by Administrator on 2017/4/17 0017.
 */

public interface UserInfoNameContract {

    interface View extends BaseView<UserInfoNameContract.Presenter> {
        void setFinish();

        void setInitView(int titleResId,int hintResId,String text);

        Activity getActivity();
    }

    interface Presenter extends BasePresenter {
        void requestName(String value);

        void requestID(String value);

        void initData();
    }

}
