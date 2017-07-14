package connect.activity.chat.exts;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.google.protobuf.InvalidProtocolBufferException;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import connect.activity.base.BaseActivity;
import connect.activity.wallet.adapter.GroupGatherRecordsAdapter;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.ToastUtil;
import connect.utils.UriUtil;
import connect.utils.cryption.DecryptionUtil;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import connect.widget.TopToolBar;
import connect.widget.pullTorefresh.EndlessScrollListener;
import protos.Connect;

/**
 * group gather records
 */
public class GroupGatherRecordsActivity extends BaseActivity {

    @Bind(R.id.toolbar_top)
    TopToolBar toolbarTop;
    @Bind(R.id.recyclerview)
    RecyclerView recyclerview;
    @Bind(R.id.refreshview)
    SwipeRefreshLayout refreshview;


    private GroupGatherRecordsActivity activity;
    private final int PAGESIZE_MAX = 10;
    private int page = 1;
    private GroupGatherRecordsAdapter recordsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_gather_records);
        ButterKnife.bind(this);
        initView();
    }

    public static void startActivity(Activity activity) {
        ActivityUtil.next(activity, GroupGatherRecordsActivity.class);
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
                requestHostory();
                refreshview.setRefreshing(false);
            }
        });

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(activity);
        recordsAdapter = new GroupGatherRecordsAdapter(activity);
        recyclerview.setAdapter(recordsAdapter);
        recyclerview.addOnScrollListener(new EndlessScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore() {
                page++;
                requestHostory();
            }
        });
        recordsAdapter.setItemClickListener(new GroupGatherRecordsAdapter.OnItemClickListener() {
            @Override
            public void itemClick(Connect.Crowdfunding crowdfunding) {
                GatherDetailGroupActivity.startActivity(activity, crowdfunding.getHashId());
            }
        });

        requestHostory();
    }

    private void requestHostory() {
        Connect.UserCrowdfundingInfo history = Connect.UserCrowdfundingInfo.newBuilder()
                .setPageIndex(page)
                .setPageSize(PAGESIZE_MAX)
                .build();
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.CROWDFUN_RECORDS, history, new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                try {
                    Connect.IMResponse imResponse = Connect.IMResponse.parseFrom(response.getBody().toByteArray());
                    Connect.StructData structData = DecryptionUtil.decodeAESGCMStructData(imResponse.getCipherData());
                    Connect.Crowdfundings crowdfundings = Connect.Crowdfundings.parseFrom(structData.getPlainData());
                    List<Connect.Crowdfunding> list = crowdfundings.getListList();

                    refreshview.setRefreshing(false);
                    if (page > 1) {
                        recordsAdapter.setNotifyData(list, false);
                    } else {
                        recordsAdapter.setNotifyData(list, true);
                    }
                } catch (InvalidProtocolBufferException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Connect.HttpResponse response) {
                ToastUtil.getInstance().showToast(response.getCode() + response.getMessage());
            }
        });
    }
}