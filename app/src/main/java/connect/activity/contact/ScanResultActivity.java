package connect.activity.contact;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.activity.base.BaseActivity;
import connect.activity.workbench.VisitorsAuditActivity;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.widget.TopToolBar;
import protos.Connect;

/**
 * Created by Administrator on 2018/2/6 0006.
 */

public class ScanResultActivity extends BaseActivity {

    @Bind(R.id.toolbar_top)
    TopToolBar toolbarTop;
    @Bind(R.id.result_text)
    TextView resultText;

    private ScanResultActivity mActivity;

    public static void lunchActivity(Activity activity, String value) {
        Bundle bundle = new Bundle();
        bundle.putString("value", value);
        ActivityUtil.next(activity, ScanResultActivity.class, bundle);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_scan_result);
        ButterKnife.bind(this);
        initView();
    }

    @Override
    public void initView() {
        mActivity = this;
        toolbarTop.setBlackStyle();
        toolbarTop.setLeftImg(R.mipmap.back_white);
        toolbarTop.setTitle(null, R.string.Link_Scan_Result);

        String value = getIntent().getExtras().getString("value");
        resultText.setText(value);
        resultText.setTextIsSelectable(true);
    }

    @OnClick(R.id.left_img)
    void goBack(View view) {
        ActivityUtil.goBack(mActivity);
    }

}
