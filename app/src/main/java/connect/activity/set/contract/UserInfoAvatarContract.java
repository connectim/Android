package connect.activity.set.contract;

import android.app.Activity;

import connect.activity.base.contract.BasePresenter;
import connect.activity.base.contract.BaseView;

/**
 * Created by Administrator on 2017/4/17 0017.
 */

public interface UserInfoAvatarContract {

    interface View extends BaseView<UserInfoAvatarContract.Presenter> {
        Activity getActivity();

        void requestAvaFninish(String path);
    }

    interface Presenter extends BasePresenter {
        void saveImageToGallery();

        void requestAvater(String pathLocal);
    }

}
