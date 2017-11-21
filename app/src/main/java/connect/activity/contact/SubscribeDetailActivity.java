package connect.activity.contact;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import connect.activity.base.BaseActivity;
import connect.activity.contact.bean.SourceType;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.widget.TopToolBar;

/**
 * Created by Administrator on 2017/11/21 0021.
 */

public class SubscribeDetailActivity extends BaseActivity {

    @Bind(R.id.toolbar)
    TopToolBar toolbar;
    @Bind(R.id.subscribe_image)
    ImageView subscribeImage;
    @Bind(R.id.subscribe_name_tv)
    TextView subscribeNameTv;
    @Bind(R.id.subscribe_describe_tv)
    TextView subscribeDescribeTv;
    @Bind(R.id.button)
    Button button;

    private SubscribeDetailActivity mActivity;

    public static void startActivity(Activity activity) {
        Bundle bundle = new Bundle();
        ActivityUtil.next(activity, SubscribeDetailActivity.class, bundle);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_subscribe_detail);
        ButterKnife.bind(this);
        initView();
    }

    @Override
    public void initView() {
        mActivity = this;
        toolbar.setBlackStyle();
        toolbar.setLeftImg(R.mipmap.back_white);
        toolbar.setTitle(null, R.string.Link_Profile);
    }

}
