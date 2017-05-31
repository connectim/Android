package connect.ui.activity.wallet;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;

import com.google.protobuf.InvalidProtocolBufferException;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.db.MemoryDataManager;
import connect.db.SharedPreferenceUtil;
import connect.ui.activity.R;
import connect.ui.activity.login.bean.UserBean;
import connect.ui.activity.wallet.adapter.RedHistoryAdapter;
import connect.ui.base.BaseActivity;
import connect.utils.ActivityUtil;
import connect.utils.UriUtil;
import connect.utils.cryption.DecryptionUtil;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import connect.view.TopToolBar;
import connect.view.pullTorefresh.XListView;
import protos.Connect;

/**
 *
 * Created by Administrator on 2016/12/19.
 */
public class PacketHistoryActivity extends BaseActivity {

    @Bind(R.id.toolbar_top)
    TopToolBar toolbarTop;
    @Bind(R.id.list_view)
    XListView listView;

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

        redHistoryAdapter = new RedHistoryAdapter();
        listView.setAdapter(redHistoryAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Connect.RedPackageInfo redPackageInfo = (Connect.RedPackageInfo)parent.getAdapter().getItem(position);
                PacketDetailActivity.startActivity(mActivity,redPackageInfo.getRedpackage().getHashId());
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
                page ++;
                requestHostory();
            }
        });

        requestHostory();
    }

    @OnClick(R.id.left_img)
    void goback(View view) {
        ActivityUtil.goBack(mActivity);
    }

    private void requestHostory(){
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
                    if(page > 1){
                        redHistoryAdapter.setNotifyData(list,false);
                    }else{
                        redHistoryAdapter.setNotifyData(list,true);
                    }

                    listView.stopRefresh();
                    listView.stopLoadMore();
                    if(list.size() == PAGESIZE_MAX){
                        listView.setPullLoadEnable(true);
                    }else{
                        listView.setPullLoadEnable(false);
                    }
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
