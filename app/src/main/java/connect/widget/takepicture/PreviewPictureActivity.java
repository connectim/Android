package connect.widget.takepicture;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.ui.activity.R;
import connect.activity.base.BaseActivity;
import connect.utils.ActivityUtil;
import connect.utils.FileUtil;
import connect.utils.glide.GlideUtil;
import connect.widget.HightEqWidthImage;
import connect.widget.TopToolBar;

/**
 * Created by Administrator on 2017/6/28 0028.
 */

public class PreviewPictureActivity extends BaseActivity{

    @Bind(R.id.toolbar_top)
    TopToolBar toolbarTop;
    @Bind(R.id.preview_img)
    HightEqWidthImage previewImg;
    @Bind(R.id.retake_rela)
    RelativeLayout retakeRela;
    @Bind(R.id.send_rela)
    RelativeLayout sendRela;
    @Bind(R.id.phone_bottom_lin)
    RelativeLayout phoneBottomLin;

    private Bundle bundle;
    private PreviewPictureActivity mActivity;
    private String pathImag;
    public static final int REQUEST_CODE = 101;

    public static void startActivity(Activity activity,String path) {
        Bundle bundle = new Bundle();
        bundle.putString("path",path);
        ActivityUtil.next(activity, PreviewPictureActivity.class, bundle,REQUEST_CODE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_picture_preview);
        ButterKnife.bind(this);
        initView();
    }

    @Override
    public void initView() {
        mActivity = this;
        toolbarTop.setBlackStyle();
        toolbarTop.setTitle(null, R.string.Set_Profile_Photo);

        bundle = getIntent().getExtras();
        pathImag = bundle.getString("path");

        GlideUtil.loadAvater(previewImg,pathImag);
    }

    @OnClick(R.id.retake_rela)
    void retake(View view){
        FileUtil.deleteDirectory(pathImag);
        ActivityUtil.goBack(mActivity);
    }

    @OnClick(R.id.send_rela)
    void send(View view){
        Bundle bundle = new Bundle();
        bundle.putString("path", pathImag);
        ActivityUtil.goBackWithResult(mActivity,RESULT_OK,bundle);
    }

}