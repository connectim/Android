package connect.activity.workbench.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import connect.activity.base.BaseFragment;
import connect.activity.contact.bean.AppsState;
import connect.activity.home.view.LineDecoration;
import connect.activity.workbench.VisitorsAuditActivity;
import connect.activity.workbench.adapter.VisitorAdapter;
import connect.activity.workbench.bean.UpdateState;
import connect.ui.activity.R;
import connect.utils.UriUtil;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import connect.utils.system.SystemUtil;
import connect.widget.pullTorefresh.EndlessScrollListener;
import protos.Connect;

/**
 * Created by Administrator on 2018/1/17 0017.
 */

public class AuditFragment extends BaseFragment {

    @Bind(R.id.recyclerview)
    RecyclerView recyclerview;
    @Bind(R.id.refreshview)
    SwipeRefreshLayout refreshview;
    @Bind(R.id.no_data_lin)
    LinearLayout noDataLin;

    private FragmentActivity mActivity;
    private int page = 1;
    private int MAX_RECOMMEND_COUNT = 20;
    private VisitorAdapter adapter;

    public static AuditFragment startFragment() {
        AuditFragment auditFragment = new AuditFragment();
        return auditFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_audit, container, false);
        ButterKnife.bind(this, view);
        EventBus.getDefault().register(this);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mActivity = getActivity();
        initView();
    }

    @Override
    public void onResume() {
        super.onResume();
        //initView();
    }

    private void initView() {
        page = 1;
        refreshview.setColorSchemeResources(R.color.color_ebecee, R.color.color_c8ccd5, R.color.color_lightgray);
        refreshview.setOnRefreshListener(onRefreshListener);
        recyclerview.addOnScrollListener(endlessScrollListener);

        adapter = new VisitorAdapter(mActivity, 1);
        adapter.setItemClickListener(onItemClickListener);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mActivity);
        recyclerview.setLayoutManager(linearLayoutManager);
        recyclerview.addItemDecoration(new LineDecoration(mActivity, true));
        recyclerview.setAdapter(adapter);
        initData();
    }

    @Subscribe
    public void onEventMainThread(UpdateState updateState) {
        switch (updateState.getStatusEnum()) {
            case UPDATE_VISITOR:
                initData();
                break;
        }
    }

    public void initData(){
        page = 1;
        requestVisitorData();
    }

    SwipeRefreshLayout.OnRefreshListener onRefreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            page = 1;
            requestVisitorData();
        }
    };

    EndlessScrollListener endlessScrollListener = new EndlessScrollListener() {
        @Override
        public void onLoadMore() {
            page++;
            requestVisitorData();
        }
    };

    VisitorAdapter.OnItemClickListener onItemClickListener = new VisitorAdapter.OnItemClickListener() {
        @Override
        public void itemClick(Connect.VisitorRecord visitorRecord) {
            VisitorsAuditActivity.lunchActivity(mActivity, visitorRecord, 0);
        }

        @Override
        public void callClick(Connect.VisitorRecord visitorRecord) {
            SystemUtil.callPhone(mActivity, visitorRecord.getStaffPhone());
        }
    };

    private void requestVisitorData() {
        Connect.VisitorRecordsReq visitorRecordsReq = Connect.VisitorRecordsReq.newBuilder()
                .setPageNum(page + "")
                .setPageSize(MAX_RECOMMEND_COUNT + "")
                .build();
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.CONNECT_V3_PROXY_VISITOR_RECORDS, visitorRecordsReq, new ResultCall<Connect.HttpNotSignResponse>() {
            @Override
            public void onResponse(Connect.HttpNotSignResponse response) {
                if (refreshview == null) {
                    return;
                }
                refreshview.setRefreshing(false);
                try {
                    Connect.StructData structData = Connect.StructData.parseFrom(response.getBody());
                    Connect.VisitorRecords visitorRecords = Connect.VisitorRecords.parseFrom(structData.getPlainData());
                    List<Connect.VisitorRecord> list = visitorRecords.getListList();
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
            public void onError(Connect.HttpNotSignResponse response) {
                refreshview.setRefreshing(false);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
        EventBus.getDefault().unregister(this);
    }
}
