package connect.activity.workbench;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.activity.base.BaseActivity;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.BitmapUtil;
import connect.utils.DialogUtil;
import connect.utils.StringUtil;
import connect.utils.TimeUtil;
import connect.utils.ToastEUtil;
import connect.utils.ToastUtil;
import connect.utils.UriUtil;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import connect.utils.system.SystemUtil;
import connect.widget.TopToolBar;
import protos.Connect;

/**
 * Created by Administrator on 2018/1/16 0016.
 */

public class VisitorsAuditActivity extends BaseActivity {

    @Bind(R.id.toolbar_top)
    TopToolBar toolbarTop;
    @Bind(R.id.name_tv)
    TextView nameTv;
    @Bind(R.id.reason_tv)
    TextView reasonTv;
    @Bind(R.id.phone_tv)
    TextView phoneTv;
    @Bind(R.id.time_tv)
    TextView timeTv;
    @Bind(R.id.left_face_image)
    ImageView leftFaceImage;
    @Bind(R.id.is_face_image)
    ImageView isFaceImage;
    @Bind(R.id.right_face_image)
    ImageView rightFaceImage;
    @Bind(R.id.refuse_text)
    TextView refuseText;
    @Bind(R.id.agree_text)
    TextView agreeText;
    @Bind(R.id.bottom_lin)
    LinearLayout bottomLin;
    @Bind(R.id.bottom_relative)
    RelativeLayout bottomRelative;

    private VisitorsAuditActivity mActivity;
    private Connect.VisitorRecord visitorRecord;

    public static void lunchActivity(Activity activity, Connect.VisitorRecord visitorRecord, int status) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("bean", visitorRecord);
        bundle.putInt("status", status);
        ActivityUtil.next(activity, VisitorsAuditActivity.class, bundle);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workbench_visitors_audit);
        ButterKnife.bind(this);
        initView();
    }

    @Override
    public void initView() {
        mActivity = this;
        toolbarTop.setBlackStyle();
        toolbarTop.setLeftImg(R.mipmap.back_white);
        toolbarTop.setTitle(null, R.string.Work_Visitors_to_approval);

        visitorRecord = (Connect.VisitorRecord) getIntent().getExtras().getSerializable("bean");

        nameTv.setText(visitorRecord.getGuestName());
        reasonTv.setText(getString(R.string.Work_Visitors_reason, visitorRecord.getReason()));

        phoneTv.setText(StringUtil.getFormatPhone(visitorRecord.getGuestPhone()));
        String time = TimeUtil.getTime(visitorRecord.getStartTime(), TimeUtil.DATE_FORMAT_MONTH_HOUR) + "——" +
                TimeUtil.getTime(visitorRecord.getEndTime(), TimeUtil.DATE_FORMAT_MONTH_HOUR);
        timeTv.setText(getString(R.string.Work_Visitors_time, time));

        leftFaceImage.setImageBitmap(BitmapUtil.getInstance().base64ToBitmap(visitorRecord.getFaceLeft()));
        isFaceImage.setImageBitmap(BitmapUtil.getInstance().base64ToBitmap(visitorRecord.getFace()));
        rightFaceImage.setImageBitmap(BitmapUtil.getInstance().base64ToBitmap(visitorRecord.getFaceRight()));

        int status = getIntent().getExtras().getInt("status",0);
        if(status == 1){
            bottomRelative.setVisibility(View.GONE);
        }
    }

    @OnClick(R.id.left_img)
    void goback(View view) {
        ActivityUtil.goBack(mActivity);
    }

    @OnClick(R.id.refuse_text)
    void refuseText(View view) {
        showAuditDialog(getResources().getString(R.string.Work_Visitors_sure_to_deny_access), false);
    }

    @OnClick(R.id.agree_text)
    void agreeText(View view) {
        showAuditDialog(getResources().getString(R.string.Work_Visitors_agree_to_visit), true);
    }

    @OnClick(R.id.phone_tv)
    void callPhone(View view) {
        SystemUtil.callPhone(mActivity, visitorRecord.getStaffPhone());
    }

    private void showAuditDialog(String message, final boolean valid) {
        DialogUtil.showAlertTextView(mActivity,
                mActivity.getResources().getString(R.string.Set_tip_title), message,
                "", "", false, new DialogUtil.OnItemClickListener() {
                    @Override
                    public void confirm(String value) {
                        requestAudit(valid);
                    }

                    @Override
                    public void cancel() {
                    }
                });
    }

    private void requestAudit(boolean valid) {
        Connect.Examine examine = Connect.Examine.newBuilder()
                .setGuestId(visitorRecord.getGuestId())
                .setPass(valid)
                .build();
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.CONNECT_V3_PROXY_EXAMINE_VERIFY, examine, new ResultCall<Connect.HttpNotSignResponse>() {
            @Override
            public void onResponse(Connect.HttpNotSignResponse response) {
                ToastUtil.getInstance().showToast(R.string.Login_Update_successful);
                ActivityUtil.goBack(mActivity);
            }

            @Override
            public void onError(Connect.HttpNotSignResponse response) {
                ToastUtil.getInstance().showToast(response.getMessage());
            }
        });
    }

}
