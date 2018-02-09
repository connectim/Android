package connect.widget.clip;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;

import java.io.File;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.ui.activity.R;
import connect.activity.base.BaseActivity;
import connect.utils.ActivityUtil;
import connect.utils.BitmapUtil;
import connect.utils.dialog.DialogUtil;
import connect.widget.TopToolBar;

/**
 * Picture cut
 */
public class ClipImageActivity extends BaseActivity {

    @Bind(R.id.id_clipImageLayout)
    ClipImageLayout idClipImageLayout;
    @Bind(R.id.toolbar_top)
    TopToolBar toolbarTop;
    private String pathLocal;
    private ClipImageActivity mActivity;
    public static final int REQUEST_CODE = 103;

    public static void startActivity(Activity activity, String path, int requestCode) {
        Bundle bundle = new Bundle();
        bundle.putString("path", path);
        ActivityUtil.next(activity, ClipImageActivity.class, bundle, requestCode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clip_image);
        ButterKnife.bind(this);
        initView();
    }

    @Override
    public void initView() {
        mActivity = this;
        toolbarTop.setBlackStyle();
        toolbarTop.setLeftImg(R.mipmap.back_white);
        toolbarTop.setRightText(R.string.Set_Save);
        toolbarTop.setRightTextEnable(true);

        Bundle bundle = getIntent().getExtras();
        pathLocal = bundle.getString("path");
        idClipImageLayout.setImage(pathLocal);
    }

    @OnClick(R.id.left_img)
    void goback(View view) {
        ActivityUtil.goBack(mActivity);
    }

    @OnClick(R.id.right_lin)
    void clip(View view) {
        if(isHavePersion()){
            Bitmap bitmap = idClipImageLayout.clip();
            File file = BitmapUtil.getInstance().saveImage(null, bitmap, 100);
            String path = file.getAbsolutePath();

            if(!TextUtils.isEmpty(path)){
                Bundle bundle = new Bundle();
                bundle.putString("path", path);
                ActivityUtil.goBackWithResult(mActivity,RESULT_OK,bundle);
            }
        }else{
            DialogUtil.showAlertTextView(mActivity, getString(R.string.Set_tip_title),
                    getString(R.string.Chat_Album_Permission_Get),
                    "", "", true, false, new DialogUtil.OnItemClickListener() {
                        @Override
                        public void confirm(String value) {}

                        @Override
                        public void cancel() {}
                    });
        }
    }

    private boolean isHavePersion(){
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M||
                ContextCompat.checkSelfPermission(mActivity, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

}
