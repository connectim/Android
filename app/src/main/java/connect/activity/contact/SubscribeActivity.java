package connect.activity.contact;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;

import com.google.protobuf.ByteString;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.activity.base.BaseActivity;
import connect.activity.contact.adapter.SubscribeAdapter;
import connect.activity.contact.bean.RssBean;
import connect.activity.home.view.LineDecoration;
import connect.database.green.DaoHelper.SubscribeConversationHelper;
import connect.instant.model.CSubscriberChat;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.TimeUtil;
import connect.utils.UriUtil;
import connect.utils.cryption.DecryptionUtil;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import connect.widget.TopToolBar;
import protos.Connect;

/**
 * Subscribe number
 */

public class SubscribeActivity extends BaseActivity {

    @Bind(R.id.toolbar)
    TopToolBar toolbar;
    @Bind(R.id.recyclerview)
    RecyclerView recyclerview;
    @Bind(R.id.quotes_layout)
    LinearLayout quotesLayout;

    private SubscribeActivity mActivity;
    private SubscribeAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_subscribe);
        ButterKnife.bind(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        initView();
    }

    @Override
    public void initView() {
        mActivity = this;
        toolbar.setBlackStyle();
        toolbar.setLeftImg(R.mipmap.back_white);
        toolbar.setTitle(null, R.string.Link_Subscribe_to_the_center);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mActivity);
        recyclerview.setLayoutManager(linearLayoutManager);
        recyclerview.addItemDecoration(new LineDecoration(mActivity));
        adapter = new SubscribeAdapter(mActivity);
        adapter.setOnItemListener(onItemListener);
        recyclerview.setAdapter(adapter);

        getSubscribeList();
    }

    @OnClick(R.id.left_img)
    void goBack(View view) {
        ActivityUtil.goBack(mActivity);
    }

    @OnClick(R.id.quotes_layout)
    void goQuotesLayout(View view) {
        ActivityUtil.next(mActivity, SubscribeMarketActivity.class);
    }

    SubscribeAdapter.OnItemListener onItemListener = new SubscribeAdapter.OnItemListener() {
        @Override
        public void itemClick(int position, Connect.RSS entity) {
            RssBean rssBean = new RssBean(entity.getRssId(), entity.getIcon(), entity.getTitle(), entity.getDesc(), entity.getSubRss());
            SubscribeDetailActivity.startActivity(mActivity, rssBean);
        }
    };

    private void getSubscribeList() {
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.CONNECT_V2_RSS, ByteString.copyFrom(new byte[]{}), new ResultCall<Connect.HttpNotSignResponse>() {
            @Override
            public void onResponse(Connect.HttpNotSignResponse response) {
                try {
                    Connect.StructData structData = Connect.StructData.parseFrom(response.getBody());
                    Connect.RSSList rssList = Connect.RSSList.parseFrom(structData.getPlainData());

                    ArrayList<Connect.RSS> subscribeList = new ArrayList<>();
                    ArrayList<Connect.RSS> unSubscribeList = new ArrayList<>();
                    for (Connect.RSS rss : rssList.getRssListList()) {
                        if (rss.getSubRss()) {
                            CSubscriberChat.cSubscriberChat.updateConversationListEntity(
                                    rss.getRssId(),
                                    rss.getIcon(),
                                    rss.getTitle(),
                                    getString(R.string.Chat_Subscribe_Success),
                                    TimeUtil.getCurrentTimeInLong(),
                                    1);
                            subscribeList.add(rss);
                        } else {
                            SubscribeConversationHelper.subscribeConversationHelper.removeConversationEntity(rss.getRssId());
                            unSubscribeList.add(rss);
                        }
                    }
                    ArrayList<Connect.RSS> list = new ArrayList<>();
                    list.addAll(subscribeList);
                    list.addAll(unSubscribeList);
                    adapter.setNotify(list);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Connect.HttpNotSignResponse response) {
            }
        });
    }

}
