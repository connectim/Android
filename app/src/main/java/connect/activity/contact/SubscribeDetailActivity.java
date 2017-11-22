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
import connect.activity.contact.bean.RssBean;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.DialogUtil;
import connect.utils.ToastEUtil;
import connect.utils.UriUtil;
import connect.utils.glide.GlideUtil;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import connect.widget.TopToolBar;
import protos.Connect;

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
    private RssBean rssBean;

    public static void startActivity(Activity activity, RssBean rssBean) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("rss", rssBean);
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
        toolbar.setTitle(null, R.string.Wallet_Detail);
        toolbar.setRightTextEnable(false);

        rssBean = (RssBean)getIntent().getExtras().getSerializable("rss");

        if(rssBean.isSubRss()){
            button.setText(R.string.Link_To_view_the_message);
            toolbar.setRightImg(R.mipmap.menu_white);
            toolbar.setRightTextEnable(true);
        } else {
            button.setText(R.string.Link_Subscribe);
        }
        subscribeDescribeTv.setText(rssBean.getDesc());
        subscribeNameTv.setText(rssBean.getTitle());
        GlideUtil.loadAvatarRound(subscribeImage, rssBean.getIcon());
    }

    @OnClick(R.id.left_img)
    void goBack(View view) {
        ActivityUtil.goBack(mActivity);
    }

    @OnClick(R.id.right_lin)
    void otherLoginClick(View view) {
        ArrayList<String> list = new ArrayList<>();
        list.add(mActivity.getString(R.string.Link_Unsubscribe));
        DialogUtil.showBottomView(mActivity, list, new DialogUtil.DialogListItemClickListener() {
            @Override
            public void confirm(int position) {
                switch (position) {
                    case 0:
                        setUnSubscribe(false);
                        break;
                    default:
                        break;
                }
            }
        });
    }

    @OnClick(R.id.button)
    void goButton(View view) {
        if(rssBean.isSubRss()){
            // 进去聊天消息界面
        }else{
            setUnSubscribe(true);
        }
    }

    private void setUnSubscribe(boolean isSubscribe){
        Connect.RSS rss = Connect.RSS.newBuilder()
                .setSubRss(isSubscribe)
                .setRssId(rssBean.getRssId())
                .build();
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.CONNECT_V2_RSS_FOLLOW, rss, new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                ActivityUtil.goBack(mActivity);
            }

            @Override
            public void onError(Connect.HttpResponse response) {
                ToastEUtil.makeText(mActivity, response.getMessage(), ToastEUtil.TOAST_STATUS_FAILE).show();
            }
        });
    }

}
