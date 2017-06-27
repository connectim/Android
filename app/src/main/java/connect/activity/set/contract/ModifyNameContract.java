package connect.activity.set.contract;

import connect.activity.base.BasePresenter;
import connect.activity.base.BaseView;

/**
 * Created by Administrator on 2017/4/17 0017.
 */

public interface ModifyNameContract {

    interface View extends BaseView<ModifyNameContract.Presenter> {
        void setCloseVisible(int visibility);

        void setTopRightEnable(boolean isEnable);

        void setFinish();

        void setInitView(int titleResId,int hintResId,String text);
    }

    interface Presenter extends BasePresenter {
        void textChange(String value);

        void requestName(String value);

        void requestID(String value);

        void initData();
    }

}
