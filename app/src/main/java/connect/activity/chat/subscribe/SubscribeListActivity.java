package connect.activity.chat.subscribe;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import connect.activity.base.BaseActivity;
import connect.activity.chat.adapter.SubscribeListAdapter;
import connect.activity.chat.subscribe.contract.SubscribeListContract;
import connect.activity.chat.subscribe.presenter.SubscribeListPresenter;
import connect.activity.home.view.LineDecoration;
import connect.database.green.DaoHelper.SubscribeConversationHelper;
import connect.database.green.bean.SubscribeConversationEntity;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.widget.TopToolBar;

public class SubscribeListActivity extends BaseActivity implements SubscribeListContract.BView {

    @Bind(R.id.toolbar)
    TopToolBar toolbar;
    @Bind(R.id.recyclerview)
    RecyclerView recyclerview;

    private static String TAG = "_SubscribeListActivity";
    private SubscribeListActivity activity;
    private SubscribeListContract.Presenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_information);
        ButterKnife.bind(this);
        initView();
    }

    public static void startActivity(Activity activity) {
        Intent intent = new Intent(activity, SubscribeListActivity.class);
        activity.startActivity(intent);
    }

    @Override
    public void initView() {
        activity = this;
        toolbar.setBlackStyle();
        toolbar.setLeftImg(R.mipmap.back_white);
        toolbar.setTitle(getString(R.string.Chat_Subscriber));
        toolbar.setLeftListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityUtil.goBack(activity);
            }
        });

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(activity);
        recyclerview.setLayoutManager(linearLayoutManager);
        SubscribeListAdapter subscribeAdapter = new SubscribeListAdapter(activity);
        recyclerview.setAdapter(subscribeAdapter);
        recyclerview.addItemDecoration(new LineDecoration(activity));

        List<SubscribeConversationEntity> conversationEntities = SubscribeConversationHelper.subscribeConversationHelper.selectAllEntity();
        subscribeAdapter.setData(conversationEntities);
        new SubscribeListPresenter(this).start();
    }

    @Override
    public void setPresenter(SubscribeListContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public Activity getActivity() {
        return activity;
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}
