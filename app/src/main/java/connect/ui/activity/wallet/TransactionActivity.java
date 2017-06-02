package connect.ui.activity.wallet;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;

import com.google.protobuf.InvalidProtocolBufferException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.db.MemoryDataManager;
import connect.db.SharedPreferenceUtil;
import connect.ui.activity.R;
import connect.ui.activity.login.bean.UserBean;
import connect.ui.activity.wallet.adapter.TransactionAdapter;
import connect.ui.base.BaseActivity;
import connect.utils.ActivityUtil;
import connect.utils.ProtoBufUtil;
import connect.utils.UriUtil;
import connect.utils.log.LogManager;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import connect.view.TopToolBar;
import connect.view.pullTorefresh.XListView;
import protos.Connect;

/**
 * transaction
 * Created by Administrator on 2016/12/10.
 */
public class TransactionActivity extends BaseActivity {

    @Bind(R.id.toolbar_top)
    TopToolBar toolbarTop;
    @Bind(R.id.list_view)
    XListView listView;

    private TransactionActivity mActivity;
    private final int PAGESIZE_MAX = 10;
    private int page = 1;
    private TransactionAdapter ransactionAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet_transaction);
        ButterKnife.bind(this);
        initView();
    }

    @Override
    public void initView() {
        mActivity = this;
        toolbarTop.setBlackStyle();
        toolbarTop.setLeftImg(R.mipmap.back_white);
        toolbarTop.setTitle(null, R.string.Wallet_Transactions);

        ransactionAdapter = new TransactionAdapter();
        listView.setAdapter(ransactionAdapter);
        listView.setXListViewListener(new XListView.IXListViewListener() {
            @Override
            public void onRefresh() {
                page = 1;
                requsetTransaction();
            }

            @Override
            public void onLoadMore() {
                page ++;
                requsetTransaction();
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Connect.Transaction transaction  = (Connect.Transaction)parent.getAdapter().getItem(position);
                BlockchainActivity.startActivity(mActivity,transaction.getHash());
            }
        });
        requsetTransaction();
    }

    @OnClick(R.id.left_img)
    void goback(View view) {
        ActivityUtil.goBack(mActivity);
    }

    public void requsetTransaction(){
        String url = String.format(Locale.ENGLISH,UriUtil.BLOCKCHAIN_ADDRESS_TX, MemoryDataManager.getInstance().getAddress(),page,PAGESIZE_MAX);
        OkHttpUtil.getInstance().get(url, new ResultCall<Connect.HttpNotSignResponse>() {
            @Override
            public void onResponse(Connect.HttpNotSignResponse response) {
                try {
                    Connect.Transactions transactions = Connect.Transactions.parseFrom(response.getBody());
                    List<Connect.Transaction> list = transactions.getTransactionsList();
                    ArrayList<Connect.Transaction> listChecks = new ArrayList<Connect.Transaction>();
                    for(Connect.Transaction transaction : list){
                        if(ProtoBufUtil.getInstance().checkProtoBuf(transaction)){
                            listChecks.add(transaction);
                        }
                    }

                    if(page > 1){
                        ransactionAdapter.setNotifyData(listChecks,false);
                    }else{
                        ransactionAdapter.setNotifyData(listChecks,true);
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
            public void onError(Connect.HttpNotSignResponse response) {
                listView.stopRefresh();
                listView.stopLoadMore();
            }
        });
    }
}
