package connect.activity.workbench;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.activity.base.BaseActivity;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.BitmapUtil;
import connect.utils.TimeUtil;
import connect.utils.ToastUtil;
import connect.utils.UriUtil;
import connect.utils.glide.GlideUtil;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import connect.widget.TopToolBar;
import protos.Connect;

/**
 * Created by Administrator on 2018/2/5 0005.
 */

public class WarehouseDetailActivity extends BaseActivity {

    @Bind(R.id.toolbar_top)
    TopToolBar toolbarTop;
    @Bind(R.id.type_tv)
    TextView typeTv;
    @Bind(R.id.time_tv)
    TextView timeTv;
    @Bind(R.id.is_face_image)
    ImageView isFaceImage;
    @Bind(R.id.know_text)
    TextView knowText;
    @Bind(R.id.bottom_relative)
    RelativeLayout bottomRelative;

    private WarehouseDetailActivity mActivity;
    private long staffId;
    private Connect.StaffLog staffLog;

    public static void lunchActivity(Activity activity, Long id) {
        Bundle bundle = new Bundle();
        bundle.putLong("id", id);
        ActivityUtil.next(activity, WarehouseDetailActivity.class, bundle);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workbench_warehouse_detail);
        ButterKnife.bind(this);
        initView();
    }

    @Override
    public void initView() {
        mActivity = this;
        toolbarTop.setBlackStyle();
        toolbarTop.setLeftImg(R.mipmap.back_white);
        toolbarTop.setTitle(null, R.string.Wallet_Detail);
        staffId = getIntent().getExtras().getLong("id");
        requestStaff();
    }

    private void initData(){
        timeTv.setText(getString(R.string.Work_Time, TimeUtil.getTime(staffLog.getDateTime()*1000, TimeUtil.DEFAULT_DATE_FORMAT)));
        typeTv.setText(getString(R.string.Work_Entering_the_warehouse, staffLog.getLocation()));
        isFaceImage.setImageBitmap(BitmapUtil.getInstance().base64ToBitmap(staffLog.getFace()));
        if(staffLog.getStatus() == 1){
            bottomRelative.setVisibility(View.GONE);
        }
    }

    @OnClick(R.id.left_img)
    void goBack(View view) {
        ActivityUtil.goBack(mActivity);
    }

    @OnClick(R.id.know_text)
    void comfirmInfo(View view) {
        requestComfirm();
    }

    private void requestStaff(){
        Connect.UnRegisterMessage unRegisterMessage = Connect.UnRegisterMessage.newBuilder()
                .setId(staffId).build();
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.STORES_V1_IWORK_LOGS_DETAIL, unRegisterMessage, new ResultCall<Connect.HttpNotSignResponse>() {
            @Override
            public void onResponse(Connect.HttpNotSignResponse response) {
                try {
                    Connect.StructData structData = Connect.StructData.parseFrom(response.getBody());
                    staffLog = Connect.StaffLog.parseFrom(structData.getPlainData());
                    initData();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Connect.HttpNotSignResponse response) {}
        });
    }

    private void requestComfirm(){
        Connect.UnRegisterCheck  unRegisterCheck = Connect.UnRegisterCheck .newBuilder()
                .setId(staffId)
                .setStatus(1).build();
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.STORES_V1_IWORK_LOG_COMFIRM, unRegisterCheck, new ResultCall<Connect.HttpNotSignResponse>() {
            @Override
            public void onResponse(Connect.HttpNotSignResponse response) {
                ToastUtil.getInstance().showToast(R.string.Login_Update_successful);
                ActivityUtil.goBack(mActivity);
            }

            @Override
            public void onError(Connect.HttpNotSignResponse response) {}
        });
    }

}
