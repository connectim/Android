package connect.ui.activity.wallet;

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

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.ui.activity.R;
import connect.ui.activity.home.bean.MsgNoticeBean;
import connect.ui.activity.wallet.support.ScanUrlAnalysisUtil;
import connect.ui.base.BaseScanActivity;
import connect.utils.ActivityUtil;
import connect.utils.ProgressUtil;
import connect.view.ScanBgView;
import connect.view.album.entity.ImageInfo;
import connect.view.album.ui.activity.PhotoAlbumActivity;

/**
 * Scan the qr code transfer
 * Created by Administrator on 2016/12/27.
 */
public class ScanTransferActivity extends BaseScanActivity {

    @Bind(R.id.capture_preview)
    SurfaceView capturePreview;
    @Bind(R.id.capture_crop_view)
    ScanBgView captureCropView;
    @Bind(R.id.capture_container)
    RelativeLayout captureContainer;
    @Bind(R.id.left_img)
    ImageView leftImg;
    @Bind(R.id.photos_tv)
    TextView photosTv;
    private ScanTransferActivity mActivity;
    private ScanUrlAnalysisUtil analysisUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet_scan_transfer);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        initView();
    }

    @Override
    public void initView() {
        mActivity = this;
        setViewFind(capturePreview, captureCropView, captureContainer);
        analysisUtil = new ScanUrlAnalysisUtil(mActivity);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(MsgNoticeBean notice) {
        analysisUtil.showMsgTip(notice, "scan");
    }

    @Override
    public void scanCall(String value) {
        analysisUtil.analysisUrl(value);
    }

    @OnClick(R.id.left_img)
    void goBack(View view) {
        ActivityUtil.goBackBottom(mActivity);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK){
            ActivityUtil.goBackBottom(this);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @OnClick(R.id.photos_tv)
    void goSeleAlbm(View view){
        PhotoAlbumActivity.startActivity(mActivity,PhotoAlbumActivity.OPEN_ALBUM_CODE,1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == PhotoAlbumActivity.OPEN_ALBUM_CODE && requestCode == PhotoAlbumActivity.OPEN_ALBUM_CODE){
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
                    analysisUtil.analysisUrl((String) msg.obj);
                    break;
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

}
