package connect.ui.activity.login;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.ui.activity.R;
import connect.ui.base.BaseActivity;
import connect.ui.base.BaseApplication;
import connect.utils.ActivityUtil;
import connect.utils.FileUtil;
import connect.view.HightEqWidthImage;
import connect.view.TopToolBar;

/**
 * Created by Administrator on 2017/2/20.
 */

public class PreviewPhotoActivity extends BaseActivity {

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
    private PreviewPhotoActivity mActivity;
    private String pathImag;

    public static void startActivity(Activity activity,String path) {
        Bundle bundle = new Bundle();
        bundle.putString("path",path);
        ActivityUtil.next(activity, PreviewPhotoActivity.class, bundle);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_preview_photo);
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

        previewImg.setImageBitmap(BitmapFactory.decodeFile(pathImag));
    }

    @OnClick(R.id.retake_rela)
    void retake(View view){
        FileUtil.deleteDirectory(pathImag);
        ActivityUtil.goBack(mActivity);
    }

    @OnClick(R.id.send_rela)
    void send(View view){
        List<Activity> list = BaseApplication.getInstance().getActivityList();
        for (Activity activity : list) {
            if (activity.getClass().getName().equals(RegisterPhotoActivity.class.getName())){
                Bundle bundle = new Bundle();
                bundle.putString("path", pathImag);
                ActivityUtil.goBackWithResult(activity,RESULT_OK,bundle);
            }
        }
        finish();
    }

}
