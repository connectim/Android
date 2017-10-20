package connect.activity.wallet;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;

import com.google.protobuf.InvalidProtocolBufferException;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.activity.base.BaseActivity;
import connect.activity.home.view.LineDecoration;
import connect.activity.wallet.adapter.RedHistoryAdapter;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.ProtoBufUtil;
import connect.utils.UriUtil;
import instant.utils.cryption.DecryptionUtil;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import connect.widget.TopToolBar;
import connect.widget.pullTorefresh.EndlessScrollListener;
import protos.Connect;

/**
 * Created by Administrator on 2016/12/19.
 */
public class PacketHistoryActivity extends BaseActivity {

    @Bind(R.id.toolbar_top)
    TopToolBar toolbarTop;
    @Bind(R.id.recyclerview)
    RecyclerView recyclerview;
    @Bind(R.id.refreshview)
    SwipeRefreshLayout refreshview;
    @Bind(R.id.no_data_lin)
    LinearLayout noDataLin;

    private PacketHistoryActivity mActivity;
    private final int PAGESIZE_MAX = 10;
    private int page = 1;
    private RedHistoryAdapter redHistoryAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet_packet_history);
        ButterKnife.bind(this);
        initView();
    }

    public static void startActivity(Activity activity) {
        ActivityUtil.next(activity, PacketHistoryActivity.class);
    }

    @Override
    public void initView() {
        mActivity = this;
        toolbarTop.setRedStyle();
        toolbarTop.setLeftImg(R.mipmap.back_white);
        toolbarTop.setTitle(null, R.string.Chat_History);

        refreshview.setColorSchemeResources(
                R.color.color_ebecee,
                R.color.color_c8ccd5,
                R.color.color_lightgray
        );
        refreshview.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshview.setRefreshing(false);
                page = 1;
                requestHostory();
            }
        });

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mActivity);
        recyclerview.setLayoutManager(linearLayoutManager);
        recyclerview.addItemDecoration(new LineDecoration(mActivity));
        redHistoryAdapter = new RedHistoryAdapter(mActivity);
        recyclerview.setAdapter(redHistoryAdapter);
        redHistoryAdapter.setItemClickListener(new RedHistoryAdapter.OnItemClickListener() {
            @Override
            public void itemClick(Connect.RedPackageInfo packageInfo) {
                PacketDetailActivity.startActivity(mActivity, packageInfo.getRedpackage().getHashId());
            }
        });
        recyclerview.addOnScrollListener(new EndlessScrollListener() {
            @Override
            public void onLoadMore() {
                page++;
                requestHostory();
            }
        });

        requestHostory();
    }

    @OnClick(R.id.left_img)
    void goback(View view) {
        ActivityUtil.goBack(mActivity);
    }

    private void requestHostory() {
        Connect.History history = Connect.History.newBuilder()
                .setPageIndex(page)
                .setPageSize(PAGESIZE_MAX)
                .build();
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.WALLET_PACKAGE_HOSTORY, history, new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                try {
                    Connect.IMResponse imResponse = Connect.IMResponse.parseFrom(response.getBody().toByteArray());
                    Connect.StructData structData = DecryptionUtil.decodeAESGCMStructData(imResponse.getCipherData());
                    Connect.RedPackageInfos redPackageInfos = Connect.RedPackageInfos.parseFrom(structData.getPlainData());
                    List<Connect.RedPackageInfo> list = redPackageInfos.getRedPackageInfosList();

                    if ((!ProtoBufUtil.getInstance().checkProtoBuf(redPackageInfos) || list.size() == 0) && page == 1) {
                        noDataLin.setVisibility(View.VISIBLE);
                        refreshview.setVisibility(View.GONE);
                    } else {
                        if (page > 1) {
                            redHistoryAdapter.setNotifyData(list, false);
                        } else {
                            redHistoryAdapter.setNotifyData(list, true);
                        }
                        refreshview.setRefreshing(false);
                    }
                } catch (InvalidProtocolBufferException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Connect.HttpResponse response) {}
        });
    }

}
