package connect.activity.workbench;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;

import java.io.File;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.activity.base.BaseActivity;
import connect.activity.home.view.LineDecoration;
import connect.activity.workbench.adapter.VisitorAdapter;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.BitmapUtil;
import connect.utils.UriUtil;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import connect.widget.TopToolBar;
import connect.widget.pullTorefresh.EndlessScrollListener;
import connect.widget.zxing.utils.CreateScan;
import protos.Connect;

public class VisitorsActivity extends BaseActivity {

    @Bind(R.id.toolbar_top)
    TopToolBar toolbarTop;
    @Bind(R.id.recyclerview)
    RecyclerView recyclerview;
    @Bind(R.id.refreshview)
    SwipeRefreshLayout refreshview;
    @Bind(R.id.no_data_lin)
    LinearLayout noDataLin;

    private VisitorsActivity mActivity;
    private int page = 1;
    private int MAX_RECOMMEND_COUNT = 20;
    private VisitorAdapter adapter;

    public static void lunchActivity(Activity activity) {
        ActivityUtil.next(activity, VisitorsActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workbench_visitors);
        ButterKnife.bind(this);
        initView();
    }

    @Override
    public void initView() {
        mActivity = this;
        toolbarTop.setBlackStyle();
        toolbarTop.setLeftImg(R.mipmap.back_white);
        toolbarTop.setTitle(null, R.string.Work_Visitors_info);
        toolbarTop.setRightText(R.string.Link_Invite);
        toolbarTop.setRightTextEnable(true);

        refreshview.setColorSchemeResources(R.color.color_ebecee, R.color.color_c8ccd5, R.color.color_lightgray);
        refreshview.setOnRefreshListener(onRefreshListener);
        recyclerview.addOnScrollListener(endlessScrollListener);

        adapter = new VisitorAdapter(mActivity);
        adapter.setItemClickListener(onItemClickListener);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mActivity);
        recyclerview.setLayoutManager(linearLayoutManager);
        recyclerview.addItemDecoration(new LineDecoration(mActivity, true));
        recyclerview.setAdapter(adapter);
        requestVisitorData();
    }

    @OnClick(R.id.left_img)
    void goback(View view) {
        ActivityUtil.goBack(mActivity);
    }

    @OnClick(R.id.right_lin)
    void shareInvite(View view) {
        Connect.Staff staff = Connect.Staff.newBuilder().build();
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.CONNECT_V3_PROXY_TOKEN, staff, new ResultCall<Connect.HttpNotSignResponse>() {
            @Override
            public void onResponse(Connect.HttpNotSignResponse response) {
                try {
                    Connect.StructData structData = Connect.StructData.parseFrom(response.getBody());
                    Connect.Staff staff1 = Connect.Staff.parseFrom(structData.getPlainData());

                    CreateScan createScan = new CreateScan();
                    Bitmap bitmap = createScan.generateQRCode(" https://wx-kq.bitmain.com/guest/info?token=" + staff1.getToken());
                    File file = BitmapUtil.getInstance().bitmapSavePath(bitmap);
                    shareMsg(getResources().getString(R.string.Work_Visitors_share), "", "", file);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Connect.HttpNotSignResponse response) {
                int a = 1;
            }
        });
    }

    SwipeRefreshLayout.OnRefreshListener onRefreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            page = 0;
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
                refreshview.setRefreshing(false);
                try {
                    Connect.StructData structData = Connect.StructData.parseFrom(response.getBody());
                    Connect.VisitorRecords visitorRecords = Connect.VisitorRecords.parseFrom(structData.getPlainData());
                    List<Connect.VisitorRecord> list = visitorRecords.getListList();
                    if(page == 1 && list.size() == 0){
                        noDataLin.setVisibility(View.VISIBLE);
                        refreshview.setVisibility(View.GONE);
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

    public void shareMsg(String activityTitle, String msgTitle, String msgText, File file) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        if (file == null) {
            intent.setType("text/plain");
        } else {
            if (file != null && file.exists() && file.isFile()) {
                intent.setType("image/jpg");
                Uri u = Uri.fromFile(file);
                intent.putExtra(Intent.EXTRA_STREAM, u);
            }
        }
        intent.putExtra(Intent.EXTRA_SUBJECT, msgTitle);
        intent.putExtra(Intent.EXTRA_TEXT, msgText);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(Intent.createChooser(intent, activityTitle));
    }

}
