package connect.widget.takepicture;

import android.app.Activity;
import android.hardware.Camera;
import android.view.SurfaceHolder;

import connect.activity.base.contract.BasePresenter;
import connect.activity.base.contract.BaseView;
import connect.utils.permission.PermissionUtil;

public class TakePictureContract {

    interface View extends BaseView<Presenter> {
        Activity getActivity();

        void initCameraView();
    }

    interface Presenter extends BasePresenter {
        PermissionUtil.ResultCallBack getPermissionCallBack();

        String getPicturePath(byte[] data, int retY);

        SurfaceHolder.Callback getSurfaceCallback();

        void setTakePhoto(Camera.PictureCallback mPictureCallback);

        void setChangeCamera(SurfaceHolder viewHolder);

        void releasedCamera();
    }

}
