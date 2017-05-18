package connect.ui.activity.chat.exts;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;

import com.google.protobuf.InvalidProtocolBufferException;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import connect.db.MemoryDataManager;
import connect.db.SharedPreferenceUtil;
import connect.ui.activity.R;
import connect.ui.activity.login.bean.UserBean;
import connect.ui.activity.wallet.adapter.GroupGatherRecordsAdapter;
import connect.ui.base.BaseActivity;
import connect.utils.ActivityUtil;
import connect.utils.ToastUtil;
import connect.utils.UriUtil;
import connect.utils.cryption.DecryptionUtil;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import connect.view.TopToolBar;
import connect.view.pullTorefresh.XListView;
import protos.Connect;

/**
 * group gather records
 */
public class GroupGatherRecordsActivity extends BaseActivity {

    @Bind(R.id.toolbar_top)
    TopToolBar toolbarTop;
    @Bind(R.id.list_view)
    XListView listView;

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

        recordsAdapter = new GroupGatherRecordsAdapter();
        listView.setAdapter(recordsAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Connect.Crowdfunding crowdfunding = (Connect.Crowdfunding) parent.getAdapter().getItem(position);
                GatherDetailGroupActivity.startActivity(activity, crowdfunding.getHashId());
            }
        });
        listView.setXListViewListener(new XListView.IXListViewListener() {
            @Override
            public void onRefresh() {
                page = 1;
                requestHostory();
            }

            @Override
            public void onLoadMore() {
                page++;
                requestHostory();
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
                    if (page > 1) {
                        recordsAdapter.setNotifyData(list, false);
                    } else {
                        recordsAdapter.setNotifyData(list, true);
                    }

                    listView.stopRefresh();
                    listView.stopLoadMore();
                    if (list.size() == PAGESIZE_MAX) {
                        listView.setPullLoadEnable(true);
                    } else {
                        listView.setPullLoadEnable(false);
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