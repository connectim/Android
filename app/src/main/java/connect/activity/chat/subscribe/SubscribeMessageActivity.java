package connect.activity.chat.subscribe;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import connect.activity.base.BaseActivity;
import connect.activity.chat.adapter.SubscribeMessageAdapter;
import connect.activity.chat.subscribe.contract.SubscribeMessageContract;
import connect.activity.chat.subscribe.presenter.SubscribeMessagePresenter;
import connect.database.green.DaoHelper.SubscribeDetailHelper;
import connect.database.green.bean.SubscribeDetailEntity;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.widget.RecycleViewScrollHelper;
import connect.widget.TopToolBar;

public class SubscribeMessageActivity extends BaseActivity implements SubscribeMessageContract.BView {

    @Bind(R.id.toolbar)
    TopToolBar toolbar;
    @Bind(R.id.recyclerview)
    RecyclerView recyclerview;

    private static String TAG = "_SubscribeMessageActivity";
    private static String RSS_ID = "RSS_ID";
    private SubscribeMessageActivity activity;

    private Long rssId;
    protected RecycleViewScrollHelper scrollHelper;
    protected ScrollPositionListener positionListener = new ScrollPositionListener();
    private SubscribeMessageContract.Presenter presenter;
    private SubscribeMessageAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscribe_message);
        ButterKnife.bind(this);
        initView();
    }

    public static void startActivity(Activity activity, Long rssId) {
        Intent intent = new Intent(activity, SubscribeMessageActivity.class);
        intent.putExtra(RSS_ID, rssId);
        activity.startActivity(intent);
    }

    @Override
    public void initView() {
        activity = this;
        toolbar.setBlackStyle();
        toolbar.setLeftImg(R.mipmap.back_white);
        toolbar.setTitle(getString(R.string.Chat_Subscribe));
        toolbar.setLeftListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityUtil.goBack(activity);
            }
        });

        rssId = getIntent().getLongExtra(RSS_ID, 0);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(activity);
        linearLayoutManager.setOrientation(OrientationHelper.VERTICAL);
        linearLayoutManager.setStackFromEnd(true);
        adapter = new SubscribeMessageAdapter(activity);
        recyclerview.setLayoutManager(linearLayoutManager);
        recyclerview.setAdapter(adapter);
        scrollHelper = new RecycleViewScrollHelper(positionListener);
        scrollHelper.setCheckIfItemViewFullRecycleViewForTop(true);
        scrollHelper.attachToRecycleView(recyclerview);

        loadMessageEntities();
        new SubscribeMessagePresenter(this).start();
    }

    public void loadMessageEntities() {
        List<SubscribeDetailEntity> subscribeDetailEntities = SubscribeDetailHelper.subscribeDetailHelper.selectLastSubscribeDetailEntity(rssId);
        adapter.setData(subscribeDetailEntities);
        updateRead();
    }

    public void loadMoreMessageEntities() {
        long messageId = adapter.lastMessageId();
        List<SubscribeDetailEntity> detailEntities = SubscribeDetailHelper.subscribeDetailHelper.selectLastSubscribeDetailEntity(rssId, messageId);

        View firstChild = recyclerview.getChildAt(0);
        int top = firstChild.getTop();
        adapter.insertMoreEnties(detailEntities);
        scrollHelper.scrollToPosition(detailEntities.size(), top);//Some errors, top - SystemUtil.dipToPx(48)
    }

    @Override
    public void setPresenter(SubscribeMessageContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public Activity getActivity() {
        return activity;
    }

    private class ScrollPositionListener implements RecycleViewScrollHelper.OnScrollPositionChangedListener {


        @Override
        public void onScrollToTop() {
            loadMoreMessageEntities();
        }

        @Override
        public void onScrollToBottom() {

        }

        @Override
        public void onScrollToUnknown(boolean isTopViewVisible, boolean isBottomViewVisible) {

        }
    }

    private void updateRead(){
        new AsyncTask<Void,Void,Void>(){
            @Override
            protected Void doInBackground(Void... params) {
                SubscribeDetailHelper.subscribeDetailHelper.updateSubscribeMessageRead(rssId);
                return null;
            }
        }.execute();
    }

}
