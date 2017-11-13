package connect.activity.set.contract;

import android.app.Activity;

import connect.activity.base.contract.BasePresenter;
import connect.activity.base.contract.BaseView;

public interface UserInfoAvatarContract {

    interface View extends BaseView<UserInfoAvatarContract.Presenter> {
        Activity getActivity();

        void requestAvaFinish(String path);
    }

    interface Presenter extends BasePresenter {
        void saveImageToGallery();

        void requestAvatar(String pathLocal);
    }

}
