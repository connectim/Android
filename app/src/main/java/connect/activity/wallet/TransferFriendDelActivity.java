package connect.activity.wallet;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.database.green.bean.ContactEntity;
import connect.ui.activity.R;
import connect.activity.home.view.LineDecoration;
import connect.activity.wallet.adapter.FriendDelAdapter;
import connect.activity.wallet.bean.FriendSeleBean;
import connect.activity.base.BaseActivity;
import connect.utils.ActivityUtil;
import connect.widget.TopToolBar;

/**
 * Delete selected friends
 * Created by Administrator on 2017/1/18.
 */

public class TransferFriendDelActivity extends BaseActivity {

    @Bind(R.id.toolbar_top)
    TopToolBar toolbarTop;
    @Bind(R.id.recyclerview)
    RecyclerView recyclerview;

    private TransferFriendDelActivity mActivity;
    private FriendDelAdapter friendDelAdapter;

    public static void startActivity(Activity activity, int code, List<ContactEntity> list) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("list",new FriendSeleBean(list));
        ActivityUtil.next(activity, TransferFriendDelActivity.class,bundle,code);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet_transfer_friend_del);
        ButterKnife.bind(this);
        initView();
    }

    @Override
    public void initView() {
        mActivity = this;
        toolbarTop.setBlackStyle();
        toolbarTop.setLeftImg(R.mipmap.back_white);
        toolbarTop.setTitle(null, R.string.Wallet_Transfer_Delete_Sele);

        friendDelAdapter = new FriendDelAdapter();
        recyclerview.setAdapter(friendDelAdapter);
        recyclerview.setAdapter(friendDelAdapter);
        recyclerview.setLayoutManager(new LinearLayoutManager(mActivity));
        recyclerview.addItemDecoration(new LineDecoration(mActivity));
        recyclerview.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                friendDelAdapter.closeMenu();
            }
        });
        Bundle bundle = getIntent().getExtras();
        FriendSeleBean friendSeleBean = (FriendSeleBean)bundle.getSerializable("list");
        friendDelAdapter.setData(friendSeleBean.getList());
    }

    @OnClick(R.id.left_img)
    void goback(View view) {
        backResult();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            backResult();
            return false;
        }else {
            return super.onKeyDown(keyCode, event);
        }
    }

    private void backResult(){
        Bundle bundle = new Bundle();
        bundle.putSerializable("list",new FriendSeleBean(friendDelAdapter.getDataList()));
        ActivityUtil.goBackWithResult(mActivity,RESULT_OK,bundle);
    }

}
