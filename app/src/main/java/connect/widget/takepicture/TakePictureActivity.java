package connect.widget.takepicture;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.activity.base.BaseActivity;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.permission.PermissionUtil;
import connect.utils.system.SystemDataUtil;
import connect.widget.TopToolBar;
import connect.widget.album.AlbumActivity;
import connect.widget.album.model.AlbumFile;
import connect.widget.clip.ClipImageActivity;

/**
 * Created by Administrator on 2017/6/28 0028.
 */

public class TakePictureActivity extends BaseActivity implements TakePictureContract.View{

    @Bind(R.id.toolbar_top)
    TopToolBar toolbarTop;
    @Bind(R.id.surface_rela)
    RelativeLayout surfaceRela;
    @Bind(R.id.sele_photos_tv)
    ImageView selePhotosTv;
    @Bind(R.id.takePhoto_img)
    RelativeLayout takePhotoImg;
    @Bind(R.id.switch_photos_img)
    ImageView switchPhotosImg;
    @Bind(R.id.surfaceView_rela)
    RelativeLayout surfaceViewRela;

    private TakePictureActivity mActivity;
    SurfaceView surfaceView;
    private SurfaceHolder viewHolder;
    public static final int REQUEST_CODE = 100;

    private TakePictureContract.Presenter presenter = null;

    public static void startActivity(Activity activity) {
        ActivityUtil.next(activity, TakePictureActivity.class, REQUEST_CODE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_picture);
        ButterKnife.bind(this);
        new TakePicturePresenter(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        initView();
    }

    @Override
    public void initView() {
        mActivity = this;
        toolbarTop.setBlackStyle();
        toolbarTop.setLeftImg(R.mipmap.back_white);
        toolbarTop.setTitle(null, R.string.Login_Take_Photo);

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(SystemDataUtil.getScreenWidth(), SystemDataUtil.getScreenWidth());
        layoutParams.addRule(RelativeLayout.BELOW, R.id.toolbar_top);
        surfaceRela.setLayoutParams(layoutParams);

        presenter.start();
    }

    @OnClick(R.id.left_img)
    void goBack(View view) {
        ActivityUtil.goBack(mActivity);
    }

    @OnClick(R.id.takePhoto_img)
    void setTakePhoto(View view) {
        takePhotoImg.setEnabled(false);
        presenter.setTakePhoto(mPictureCallback);
    }

    @OnClick(R.id.sele_photos_tv)
    void setSelePhoto(View view) {
        AlbumActivity.startActivity(mActivity, AlbumActivity.OPEN_ALBUM_CODE, 1);
    }

    @OnClick(R.id.switch_photos_img)
    void setChange(View view) {
        presenter.setChangeCamera(viewHolder);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionUtil.getInstance().onRequestPermissionsResult(mActivity, requestCode, permissions, grantResults, presenter.getPermissionCallBack());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == AlbumActivity.OPEN_ALBUM_CODE && requestCode == AlbumActivity.OPEN_ALBUM_CODE) {
            List<AlbumFile> strings = (List<AlbumFile>) data.getSerializableExtra("list");
            if (strings != null && strings.size() > 0) {
                ClipImageActivity.startActivity(mActivity, strings.get(0).getPath(), ClipImageActivity.REQUEST_CODE);
            }
        }

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case ClipImageActivity.REQUEST_CODE:
                case PreviewPictureActivity.REQUEST_CODE:
                    String photo_path = data.getExtras().getString("path");
                    Bundle bundle = new Bundle();
                    bundle.putString("path", photo_path);
                    ActivityUtil.goBackWithResult(mActivity,RESULT_OK,bundle);
                    break;
                default:
                    break;
            }
        }
    }

    private Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            takePhotoImg.setEnabled(true);
            String path = presenter.getPicturePath(data,toolbarTop.getHeight());
            if(!TextUtils.isEmpty(path)){
                PreviewPictureActivity.startActivity(mActivity, path);
            }
        }
    };

    @Override
    public void setPresenter(TakePictureContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public Activity getActivity() {
        return mActivity;
    }

    @Override
    public void initCameraView() {
        surfaceView = new SurfaceView(mActivity);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        surfaceView.setLayoutParams(layoutParams);
        surfaceViewRela.removeAllViews();
        surfaceViewRela.addView(surfaceView);

        viewHolder = surfaceView.getHolder();
        viewHolder.addCallback(presenter.getSurfaceCallback());
        viewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    protected void onPause() {
        super.onPause();
        presenter.releasedCamera();
    }

}
