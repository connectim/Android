package connect.ui.activity.set;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import connect.db.SharedPreferenceUtil;
import connect.ui.activity.R;
import connect.ui.activity.login.bean.UserBean;
import connect.ui.base.BaseActivity;
import connect.utils.ActivityUtil;
import connect.view.TopToolBar;
import connect.view.zxing.utils.CreateScan;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Administrator on 2017/1/9.
 */
public class AddressActivity extends BaseActivity {

    @Bind(R.id.toolbar_top)
    TopToolBar toolbarTop;
    @Bind(R.id.scan_img)
    ImageView scanImg;
    @Bind(R.id.address_tv)
    TextView addressTv;

    private AddressActivity mActivity;

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
        Bitmap bitmap = createScan.generateQRCode(userBean.getAddress(),mActivity.getResources().getColor(R.color.color_00ffbf));
        scanImg.setImageBitmap(bitmap);
        addressTv.setText(userBean.getAddress());
    }

    @OnClick(R.id.left_img)
    void goback(View view){
        ActivityUtil.goBack(mActivity);
    }

}
