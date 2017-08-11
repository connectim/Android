package connect.activity.chat.exts;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import java.util.List;
import butterknife.Bind;
import butterknife.ButterKnife;
import connect.activity.base.BaseActivity;
import connect.activity.chat.exts.contract.GroupCrowdingRecordContract;
import connect.activity.chat.exts.presenter.GroupCrowdingRecordPresenter;
import connect.activity.wallet.adapter.GroupGatherRecordsAdapter;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.widget.TopToolBar;
import connect.widget.pullTorefresh.EndlessScrollListener;
import protos.Connect;

/**
 * group gather records
 */
public class GroupCrowdingRecordsActivity extends BaseActivity implements GroupCrowdingRecordContract.BView{

    @Bind(R.id.toolbar_top)
    TopToolBar toolbarTop;
    @Bind(R.id.recyclerview)
    RecyclerView recyclerview;
    @Bind(R.id.refreshview)
    SwipeRefreshLayout refreshview;

    private GroupCrowdingRecordsActivity activity;
    private int page = 1;
    private GroupGatherRecordsAdapter recordsAdapter;

    private GroupCrowdingRecordContract.Presenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_gather_records);
        ButterKnife.bind(this);
        initView();
    }

    public static void startActivity(Activity activity) {
        ActivityUtil.next(activity, GroupCrowdingRecordsActivity.class);
    }


    @Override
    public void initView() {
        activity = this;
        toolbarTop.setBlackStyle();
        toolbarTop.setLeftImg(R.mipmap.back_white);
        toolbarTop.setLeftListence(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityUtil.goBack(activity);
            }
        });
        toolbarTop.setTitle(null, R.string.Chat_History);

        refreshview.setColorSchemeResources(
                R.color.color_ebecee,
                R.color.color_c8ccd5,
                R.color.color_lightgray
        );
        refreshview.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                page = 1;
                presenter.requestGroupCrowdingRecords(page, 10);
                refreshview.setRefreshing(false);
            }
        });

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(activity);
        recordsAdapter = new GroupGatherRecordsAdapter(activity);
        recyclerview.setLayoutManager(linearLayoutManager);
        recyclerview.setAdapter(recordsAdapter);
        recyclerview.addOnScrollListener(new EndlessScrollListener() {
            @Override
            public void onLoadMore() {
                page++;
                presenter.requestGroupCrowdingRecords(page, 10);
            }
        });
        recordsAdapter.setItemClickListener(new GroupGatherRecordsAdapter.OnItemClickListener() {
            @Override
            public void itemClick(Connect.Crowdfunding crowdfunding) {
                CrowdingDetailActivity.startActivity(activity, crowdfunding.getHashId());
            }
        });

        new GroupCrowdingRecordPresenter(this).start();
        presenter.requestGroupCrowdingRecords(page, 10);
    }

    @Override
    public void setPresenter(GroupCrowdingRecordContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public Activity getActivity() {
        return activity;
    }

    @Override
    public void crowdingRecords(List<Connect.Crowdfunding> list) {
        refreshview.setRefreshing(false);
        if (page > 1) {
            recordsAdapter.setNotifyData(list, false);
        } else {
            recordsAdapter.setNotifyData(list, true);
        }
    }
}