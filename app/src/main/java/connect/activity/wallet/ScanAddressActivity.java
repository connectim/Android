package connect.activity.wallet;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
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
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.ProgressUtil;
import connect.widget.ScanBgView;
import connect.widget.album.AlbumActivity;
import connect.widget.album.model.ImageInfo;

/**
 * Scan the qr code address added
 * Created by Administrator on 2016/12/27.
 */
public class ScanAddressActivity extends BaseScanActivity {

    @Bind(R.id.capture_preview)
    SurfaceView capturePreview;
    @Bind(R.id.capture_crop_view)
    ScanBgView captureCropView;
    @Bind(R.id.capture_container)
    RelativeLayout captureContainer;
    @Bind(R.id.left_img)
    ImageView leftImg;
    @Bind(R.id.right_img)
    TextView rightImg;

    private ScanAddressActivity mActivity;
    /** activity back code */
    public static final int BACK_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet_scan_address);
        ButterKnife.bind(this);
        initView();
    }

    @Override
    public void initView() {
        mActivity = this;
        setViewFind(capturePreview, captureCropView, captureContainer);
    }

    @Override
    public void scanCall(String value) {
        checkString(value);
    }

    @OnClick(R.id.left_img)
    void goBack(View view){
        ActivityUtil.goBackBottom(mActivity);
    }

    @OnClick(R.id.right_img)
    void goSeleAlbm(View view){
        AlbumActivity.startActivity(mActivity, AlbumActivity.OPEN_ALBUM_CODE,1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == AlbumActivity.OPEN_ALBUM_CODE && requestCode == AlbumActivity.OPEN_ALBUM_CODE){
            List<ImageInfo> strings = (List<ImageInfo>) data.getSerializableExtra("list");
            if (strings != null && strings.size() > 0) {
                getAblamString(strings.get(0).getImageFile().getAbsolutePath(),mLocalHandler);
            }
        }
    }

    private Handler mLocalHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            ProgressUtil.getInstance().dismissProgress();
            switch (msg.what){
                case PARSE_BARCODE_SUC:
                    checkString((String) msg.obj);
                    break;
            }
        }
    };

    private void checkString(String value) {
        String address = "";
        if(value.contains(":")){
            String[] list = value.split(":");
            address = list[1];
        }else{
            address = value;
        }
        Bundle bundle = new Bundle();
        bundle.putString("address",address);
        ActivityUtil.goBackWithResult(mActivity, RESULT_OK,bundle,R.anim.activity_0_to_0,R.anim.dialog_bottom_dismiss);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK){
            ActivityUtil.goBackBottom(this);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}
