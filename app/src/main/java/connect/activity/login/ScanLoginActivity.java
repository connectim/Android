package connect.activity.login;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.activity.base.BaseScanActivity;
import connect.activity.login.bean.UserBean;
import connect.activity.login.contract.ScanLoginContract;
import connect.activity.login.presenter.ScanLoginPresenter;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.DialogUtil;
import connect.utils.ProgressUtil;
import connect.wallet.jni.AllNativeMethod;
import connect.widget.album.AlbumActivity;
import connect.widget.album.model.AlbumFile;

/**
 * Scan to login.
 */
public class ScanLoginActivity extends BaseScanActivity implements ScanLoginContract.View {

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
        showBackgroupAni(captureScanLine);
        new ScanLoginPresenter(this).start();
    }

    @OnClick(R.id.left_img)
    void goBack(View view) {
        ActivityUtil.goBack(mActivity);
    }

    @OnClick(R.id.select_album)
    void goSeleAlbm(View view) {
        AlbumActivity.startActivity(mActivity,AlbumActivity.OPEN_ALBUM_CODE,1);
    }

    @Override
    public void scanCall(String value) {
        presenter.checkString(value);
    }

    public Handler mLocalHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case PARSE_BARCODE_SUC:
                    presenter.checkString((String) msg.obj);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == AlbumActivity.OPEN_ALBUM_CODE && requestCode == AlbumActivity.OPEN_ALBUM_CODE) {
            List<AlbumFile> strings = (List<AlbumFile>) data.getSerializableExtra("list");
            if (strings != null && strings.size() > 0) {
                ProgressUtil.getInstance().showProgress(mActivity);
                getAblamString(strings.get(0).getPath(), mLocalHandler);
            }
        }
    }

    @Override
    public void setPresenter(ScanLoginContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public Activity getActivity() {
        return mActivity;
    }

    @Override
    public void goIntoCodeLogin(UserBean userBean, String token) {
        if (TextUtils.isEmpty(token)) {
            LoginUserActivity.startActivity(mActivity,userBean);
        } else {
            //LoginUserActivity.startActivity(mActivity,userBean,token);
        }
        finish();
    }

    /**
     * The private key has not been registered
     *
     * @param priKey private key
     */
    @Override
    public void goIntoRegister(final String priKey) {
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
                    public void cancel() {}
                });
    }

}
