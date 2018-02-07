package connect.activity.workbench;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.activity.base.BaseActivity;
import connect.activity.home.view.LineDecoration;
import connect.activity.workbench.adapter.WarehouseAdapter;
import connect.activity.workbench.bean.UpdateState;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.UriUtil;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import connect.widget.TopToolBar;
import connect.widget.pullTorefresh.EndlessScrollListener;
import protos.Connect;

/**
 * Created by Administrator on 2018/2/5 0005.
 */

public class WarehouseActivity extends BaseActivity {

    @Bind(R.id.no_data_lin)
    LinearLayout noDataLin;
    @Bind(R.id.recyclerview)
    RecyclerView recyclerview;
    @Bind(R.id.refreshview)
    SwipeRefreshLayout refreshview;
    @Bind(R.id.toolbar_top)
    TopToolBar toolbarTop;

    private WarehouseActivity mActivity;
    private int page = 1;
    private int MAX_RECOMMEND_COUNT = 20;
    private WarehouseAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workbench_warehouse);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        initView();
    }

    public static void lunchActivity(Activity activity) {
        ActivityUtil.next(activity, WarehouseActivity.class);
    }

    @Override
    public void initView() {
        mActivity = this;
        toolbarTop.setBlackStyle();
        toolbarTop.setLeftImg(R.mipmap.back_white);
        toolbarTop.setTitle(null, R.string.Work_Warehouse_abnormal_records);
        initRecycler();
    }

    @OnClick(R.id.left_img)
    void goBack(View view) {
        ActivityUtil.goBack(mActivity);
    }

    @Subscribe
    public void onEventMainThread(UpdateState updateState) {
        switch (updateState.getStatusEnum()) {
            case UPDATE_WAREHOUSE:
                page = 1;
                requestStaffs();
                break;
        }
    }

    private void initRecycler() {
        refreshview.setColorSchemeResources(R.color.color_ebecee, R.color.color_c8ccd5, R.color.color_lightgray);
        refreshview.setOnRefreshListener(onRefreshListener);
        recyclerview.addOnScrollListener(endlessScrollListener);

        adapter = new WarehouseAdapter(mActivity);
        adapter.setItemClickListener(onItemClickListener);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mActivity);
        recyclerview.setLayoutManager(linearLayoutManager);
        recyclerview.addItemDecoration(new LineDecoration(mActivity, true));
        recyclerview.setAdapter(adapter);

        page = 1;
        requestStaffs();
    }

    SwipeRefreshLayout.OnRefreshListener onRefreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            page = 1;
            requestStaffs();
        }
    };

    EndlessScrollListener endlessScrollListener = new EndlessScrollListener() {
        @Override
        public void onLoadMore() {
            page++;
            requestStaffs();
        }
    };

    WarehouseAdapter.OnItemClickListener onItemClickListener = new WarehouseAdapter.OnItemClickListener(){
        @Override
        public void itemClick(Connect.StaffLog visitorRecord) {
            WarehouseDetailActivity.lunchActivity(mActivity, visitorRecord.getId());
        }
    };

    private void requestStaffs(){
        Connect.StaffLogsReq staffLogsReq = Connect.StaffLogsReq.newBuilder()
                .setPageNum(page)
                .setPageSize(MAX_RECOMMEND_COUNT).build();

        OkHttpUtil.getInstance().postEncrySelf(UriUtil.STORES_V1_IWORK_LOGS, staffLogsReq, new ResultCall<Connect.HttpNotSignResponse>() {
            @Override
            public void onResponse(Connect.HttpNotSignResponse response) {
                refreshview.setRefreshing(false);
                try {
                    Connect.StructData structData = Connect.StructData.parseFrom(response.getBody());
                    Connect.StaffLogs visitorRecords = Connect.StaffLogs.parseFrom(structData.getPlainData());
                    List<Connect.StaffLog> list = visitorRecords.getListList();
                    if(page == 1 && list.size() == 0){
                        noDataLin.setVisibility(View.VISIBLE);
                        refreshview.setVisibility(View.GONE);
                    }else{
                        noDataLin.setVisibility(View.GONE);
                        refreshview.setVisibility(View.VISIBLE);
                    }

                    if (page > 1) {
                        adapter.setNotifyData(list, false);
                    } else {
                        adapter.setNotifyData(list, true);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Connect.HttpNotSignResponse response) {}
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
