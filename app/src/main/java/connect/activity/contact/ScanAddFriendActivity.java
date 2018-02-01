package connect.activity.contact;

import android.app.Activity;
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
import connect.activity.base.BaseScanActivity;
import connect.activity.home.bean.MsgNoticeBean;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.ProgressUtil;
import connect.utils.scan.ResolveScanUtil;
import connect.utils.scan.ResolveUrlUtil;
import connect.widget.ScanBgView;
import connect.widget.album.AlbumActivity;
import connect.widget.album.model.AlbumFile;

/**
 * scan it
 */
public class ScanAddFriendActivity extends BaseScanActivity {

    @Bind(R.id.capture_preview)
    SurfaceView capturePreview;
    @Bind(R.id.capture_crop_view)
    ScanBgView captureCropView;
    @Bind(R.id.capture_container)
    RelativeLayout captureContainer;
    @Bind(R.id.right_img)
    ImageView rightImg;
    @Bind(R.id.content_rela)
    RelativeLayout contentRela;
    /*@Bind(R.id.bottom_capture_img)
    TextView bottomCaptureImg;*/
    @Bind(R.id.photos_tv)
    TextView photosTv;

    private ScanAddFriendActivity mActivity;
    private ResolveScanUtil resolveScanUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coantact_scan_addfriend);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        initView();
    }

    @Override
    public void initView() {
        mActivity = this;
        setViewFind(capturePreview);
        resolveScanUtil = new ResolveScanUtil(mActivity);
    }

    @OnClick(R.id.right_img)
    void goBack(View view) {
        ActivityUtil.goBackBottom(mActivity);
    }

    /*@OnClick(R.id.bottom_capture_img)
    void switchScan(View view) {
        ActivityUtil.next(mActivity, UserConnectIdActivity.class);
    }*/

    @OnClick(R.id.photos_tv)
    void goSelectAlbm(View view){
        AlbumActivity.startActivity(mActivity,AlbumActivity.OPEN_ALBUM_CODE,1);
    }

    private Handler mLocalHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            ProgressUtil.getInstance().dismissProgress();
            switch (msg.what){
                case PARSE_BARCODE_SUC:
                    resolveScanUtil.analysisUrl((String) msg.obj);
                    break;
                default:
                    break;
            }
        }
    };

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(MsgNoticeBean notice) {
        new ResolveUrlUtil(mActivity).showMsgTip(notice,ResolveUrlUtil.TYPE_OPEN_SCAN, true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == AlbumActivity.OPEN_ALBUM_CODE && requestCode == AlbumActivity.OPEN_ALBUM_CODE){
            List<AlbumFile> strings = (List<AlbumFile>) data.getSerializableExtra("list");
            if (strings != null && strings.size() > 0) {
                getAblamString(strings.get(0).getPath(),mLocalHandler);
            }
        }
    }

    @Override
    public void scanCall(String value) {
        resolveScanUtil.analysisUrl(value);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK){
            ActivityUtil.goBackBottom(this);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

}
