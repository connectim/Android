package connect.activity.set;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import connect.activity.login.bean.UserBean;
import connect.database.SharedPreferenceUtil;
import connect.ui.activity.R;
import connect.activity.base.BaseActivity;
import connect.utils.ActivityUtil;
import connect.utils.scan.ResolveScanUtil;
import connect.widget.TopToolBar;
import connect.widget.zxing.utils.CreateScan;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * show connect id
 */
public class UserConnectIdActivity extends BaseActivity {

    @Bind(R.id.toolbar_top)
    TopToolBar toolbarTop;
    @Bind(R.id.scan_img)
    ImageView scanImg;

    private UserConnectIdActivity mActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_address);
        ButterKnife.bind(this);
        initView();
    }

    @Override
    public void initView() {
        mActivity = this;
        toolbarTop.setBlackStyle();
        toolbarTop.setLeftImg(R.mipmap.back_white);
        toolbarTop.setTitle(null, R.string.Set_My_QR_code);
        UserBean userBean = SharedPreferenceUtil.getInstance().getUser();

        CreateScan createScan = new CreateScan();
        Bitmap bitmap = createScan.generateQRCode(ResolveScanUtil.CONNECT_HEAD + userBean.getUid(),
                mActivity.getResources().getColor(R.color.color_00ffbf));
        scanImg.setImageBitmap(bitmap);
    }

    @OnClick(R.id.left_img)
    void goBack(View view){
        ActivityUtil.goBack(mActivity);
    }

}
