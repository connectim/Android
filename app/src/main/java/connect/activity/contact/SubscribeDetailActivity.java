package connect.activity.contact;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.activity.base.BaseActivity;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.DialogUtil;
import connect.widget.TopToolBar;

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
    public static final int unSubscribe = 1;
    public static final int subscribed = 2;

    public static void startActivity(Activity activity, int status) {
        Bundle bundle = new Bundle();
        bundle.putInt("status", status);
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
        toolbar.setRightTextEnable(false);

        Bundle bundle = getIntent().getExtras();
        int status = bundle.getInt("status");
        if(status == unSubscribe){
            button.setText("订阅");
        }else if(status == subscribed){
            button.setText("进入消息");
            toolbar.setRightImg(R.mipmap.menu_white);
            toolbar.setRightTextEnable(true);
        }
    }

    @OnClick(R.id.left_img)
    void goBack(View view) {
        ActivityUtil.goBack(mActivity);
    }

    @OnClick(R.id.right_lin)
    void otherLoginClick(View view) {
        ArrayList<String> list = new ArrayList<>();
        list.add("取消订阅");
        DialogUtil.showBottomView(mActivity, list, new DialogUtil.DialogListItemClickListener() {
            @Override
            public void confirm(int position) {
                switch (position) {
                    case 0:

                        break;
                    default:
                        break;
                }
            }
        });
    }

    @OnClick(R.id.button)
    void goButton(View view) {

    }

}
