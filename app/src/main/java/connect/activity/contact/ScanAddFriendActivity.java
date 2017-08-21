package connect.activity.contact;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
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
import connect.database.MemoryDataManager;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.ProgressUtil;
import connect.utils.scan.ResolveScanUtil;
import connect.utils.scan.ResolveUrlUtil;
import connect.utils.system.SystemUtil;
import connect.widget.ScanBgView;
import connect.widget.album.AlbumActivity;
import connect.widget.album.entity.ImageInfo;
import connect.widget.zxing.utils.CreateScan;

/**
 * Created by Administrator on 2016/12/27.
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
    @Bind(R.id.scan_img)
    ImageView scanImg;
    @Bind(R.id.address_tv)
    TextView addressTv;
    @Bind(R.id.scan_rela)
    RelativeLayout scanRela;
    @Bind(R.id.content_rela)
    RelativeLayout contentRela;
    @Bind(R.id.bottom_capture_img)
    TextView bottomCaptureImg;
    @Bind(R.id.bottom_scan_img)
    ImageView bottomScanImg;
    @Bind(R.id.bottom_scan_rela)
    RelativeLayout bottomScanRela;
    @Bind(R.id.move_down_img)
    ImageView moveDownImg;
    @Bind(R.id.move_up_img)
    ImageView moveUpImg;
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
        setViewFind(capturePreview, captureCropView, captureContainer);

        CreateScan createScan = new CreateScan();
        Bitmap bitmap = createScan.generateQRCode(MemoryDataManager.getInstance().getAddress(), getResources().getColor(R.color.color_00ffbf));
        scanImg.setImageBitmap(bitmap);
        bottomScanImg.setImageBitmap(bitmap);
        addressTv.setText(MemoryDataManager.getInstance().getAddress());
        resolveScanUtil = new ResolveScanUtil(mActivity);
    }

    @OnClick(R.id.right_img)
    void goBack(View view) {
        ActivityUtil.goBackBottom(mActivity);
    }

    @OnClick(R.id.bottom_capture_img)
    void switchScan(View view) {
        onStart();
        moveDownImg.setVisibility(View.GONE);

        float xScale = (float) bottomScanRela.getWidth() / scanRela.getWidth();
        float yScale = (float) bottomScanRela.getHeight() / scanRela.getHeight();
        Animation scaleAnimation = new ScaleAnimation(1f, xScale, 1f, yScale,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 1f);

        float yTrans = bottomScanRela.getBottom() - scanRela.getBottom() + SystemUtil.dipToPx(35);
        Animation translateAnimation = new TranslateAnimation(0f, 0f, 1f, yTrans);
        AnimationSet set = new AnimationSet(true);
        set.addAnimation(scaleAnimation);
        set.addAnimation(translateAnimation);
        set.setDuration(500);
        scanRela.startAnimation(set);
        set.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                bottomScanRela.setVisibility(View.VISIBLE);
                bottomCaptureImg.setVisibility(View.GONE);
                scanRela.setVisibility(View.GONE);

                moveUpImg.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    @OnClick(R.id.bottom_scan_rela)
    void switchAddresss(View view) {
        onPause();
        moveUpImg.setVisibility(View.GONE);
        bottomCaptureImg.setVisibility(View.VISIBLE);

        float scanRelaHeight = (float) SystemUtil.dipToPx(270);
        float scanRelaWith = (float) SystemUtil.dipToPx(250);
        float xScale = scanRelaWith / bottomScanRela.getWidth();
        float yScale = scanRelaHeight / bottomScanRela.getHeight();
        Animation scaleAnimation = new ScaleAnimation(1f, xScale, 1f, yScale,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 1f);

        float yTrans = contentRela.getTop() - bottomScanRela.getTop() - (bottomScanRela.getHeight() - scanRelaHeight) + SystemUtil.dipToPx(40);
        Animation translateAnimation = new TranslateAnimation(0f, 0f, 1f, yTrans);

        AnimationSet set = new AnimationSet(true);
        set.addAnimation(scaleAnimation);
        set.addAnimation(translateAnimation);
        set.setDuration(500);
        bottomScanRela.startAnimation(set);
        set.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                bottomScanRela.setVisibility(View.GONE);
                scanRela.setVisibility(View.VISIBLE);
                moveDownImg.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    @OnClick(R.id.photos_tv)
    void goSeleAlbm(View view){
        AlbumActivity.startActivity(mActivity,AlbumActivity.OPEN_ALBUM_CODE,1);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(MsgNoticeBean notice) {
        new ResolveUrlUtil(mActivity).showMsgTip(notice,ResolveUrlUtil.TYPE_OPEN_SCAN, true);
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
                    resolveScanUtil.analysisUrl((String) msg.obj);
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
