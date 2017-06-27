package connect.activity.login;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.ui.activity.R;
import connect.activity.login.bean.UserBean;
import connect.activity.login.contract.ScanLoginContract;
import connect.activity.login.presenter.ScanLoginPresenter;
import connect.activity.base.BaseScanActivity;
import connect.utils.ActivityUtil;
import connect.utils.DialogUtil;
import connect.widget.album.entity.ImageInfo;
import connect.widget.album.ui.activity.PhotoAlbumActivity;
import connect.wallet.jni.AllNativeMethod;

/**
 * Scan to login
 */
public class ScanLoginActivity extends BaseScanActivity implements ScanLoginContract.View{

    @Bind(R.id.capture_preview)
    SurfaceView capturePreview;
    @Bind(R.id.capture_scan_line)
    ImageView captureScanLine;
    @Bind(R.id.capture_crop_view)
    RelativeLayout captureCropView;
    @Bind(R.id.select_album)
    TextView selectAlbum;
    @Bind(R.id.capture_container)
    RelativeLayout captureContainer;
    @Bind(R.id.left_img)
    ImageView leftImg;

    private ScanLoginActivity mActivity;
    private ScanLoginContract.Presenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_scan);
        ButterKnife.bind(this);
        initView();
    }

    @Override
    public void initView() {
        mActivity = this;
        setViewFind(capturePreview, captureCropView, captureContainer);

        TranslateAnimation animation = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, -1.0f, Animation.RELATIVE_TO_PARENT, 0.0f);
        animation.setDuration(1500);
        animation.setRepeatCount(-1);
        animation.setRepeatMode(Animation.RESTART);
        captureScanLine.startAnimation(animation);
        //setLineAnimation(captureScanLine);

        setPresenter(new ScanLoginPresenter(this));
        presenter.start();
    }

    @Override
    public void setPresenter(ScanLoginContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void scanCall(String value) {
        presenter.checkString(value);
    }

    @OnClick(R.id.left_img)
    void goBack(View view){
        ActivityUtil.goBack(mActivity);
    }

    @OnClick(R.id.select_album)
    void goSeleAlbm(View view){
        PhotoAlbumActivity.startActivity(mActivity,PhotoAlbumActivity.OPEN_ALBUM_CODE,1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == PhotoAlbumActivity.OPEN_ALBUM_CODE && requestCode == PhotoAlbumActivity.OPEN_ALBUM_CODE){
            List<ImageInfo> strings = (List<ImageInfo>) data.getSerializableExtra("list");
            if (strings != null && strings.size() > 0) {
                getAblamString(strings.get(0).getImageFile().getAbsolutePath(),presenter.getHandle());
            }
        }
    }

    @Override
    public Activity getActivity() {
        return mActivity;
    }

    @Override
    public void goinCodeLogin(UserBean userBean, String token) {
        if(TextUtils.isEmpty(token)){
            CodeLoginActivity.startActivity(mActivity,userBean);
        }else{
            CodeLoginActivity.startActivity(mActivity,userBean,token);
        }
        finish();
    }

    @Override
    public void goinRegister(final String priKey) {
        DialogUtil.showAlertTextView(mActivity,
                mActivity.getResources().getString(R.string.Set_tip_title),
                mActivity.getResources().getString(R.string.Login_private_not_registered_register_now),
                "", "", false, new DialogUtil.OnItemClickListener() {
                    @Override
                    public void confirm(String value) {
                        String pubKey = AllNativeMethod.cdGetPubKeyFromPrivKey(priKey);
                        String address = AllNativeMethod.cdGetBTCAddrFromPubKey(pubKey);
                        UserBean userBean = new UserBean();
                        userBean.setPriKey(priKey);
                        userBean.setPubKey(pubKey);
                        userBean.setAddress(address);
                        RegisterActivity.startActivity(mActivity,userBean);
                    }

                    @Override
                    public void cancel() {

                    }
                });
    }

}
