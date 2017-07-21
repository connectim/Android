package connect.activity.wallet;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.google.protobuf.InvalidProtocolBufferException;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.activity.base.BaseActivity;
import connect.activity.wallet.adapter.TransferOutAdapter;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.UriUtil;
import connect.utils.cryption.DecryptionUtil;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import connect.widget.TopToolBar;
import connect.widget.pullTorefresh.EndlessScrollListener;
import protos.Connect;

/**
 * External transfer history
 * Created by Administrator on 2016/12/20.
 */
public class TransferOutViaHistoryActivity extends BaseActivity {

    @Bind(R.id.toolbar_top)
    TopToolBar toolbarTop;
    @Bind(R.id.recyclerview)
    RecyclerView recyclerview;
    @Bind(R.id.refreshview)
    SwipeRefreshLayout refreshview;

    private TransferOutViaHistoryActivity mActivity;
    private final int PAGESIZE_MAX = 10;
    private int page = 1;
    private TransferOutAdapter transferOutAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet_transfer_outvia_history);
        ButterKnife.bind(this);
        initView();
    }

    @Override
    public void initView() {
        mActivity = this;
        toolbarTop.setBlackStyle();
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
        transferOutAdapter = new TransferOutAdapter(mActivity);
        recyclerview.setAdapter(transferOutAdapter);
        recyclerview.addOnScrollListener(new EndlessScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore() {
                page++;
                requestHostory();
            }
        });
        transferOutAdapter.setItemClickListener(new TransferOutAdapter.OnItemClickListener() {
            @Override
            public void itemClick(Connect.ExternalBillingInfo billingInfo) {

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
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.WALLET_BILLING_EXTERNAL_HISTORY, history, new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                try {
                    Connect.IMResponse imResponse = Connect.IMResponse.parseFrom(response.getBody().toByteArray());
                    Connect.StructData structData = DecryptionUtil.decodeAESGCMStructData(imResponse.getCipherData());
                    Connect.ExternalBillingInfos externalBillingInfos = Connect.ExternalBillingInfos.parseFrom(structData.getPlainData());
                    List<Connect.ExternalBillingInfo> list = externalBillingInfos.getExternalBillingInfosList();

                    if (page > 1) {
                        transferOutAdapter.setNotifyData(list, false);
                    } else {
                        transferOutAdapter.setNotifyData(list, true);
                    }
                    refreshview.setRefreshing(false);
                } catch (InvalidProtocolBufferException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Connect.HttpResponse response) {

            }
        });
    }
}
