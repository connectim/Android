package connect.activity.wallet;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.database.SharePreferenceUser;
import connect.ui.activity.R;
import connect.activity.wallet.adapter.AddAddressAdapter;
import connect.activity.wallet.bean.AddressBean;
import connect.activity.wallet.contract.AddressBookContract;
import connect.activity.wallet.presenter.AddressBookPresenter;
import connect.activity.base.BaseActivity;
import connect.utils.ActivityUtil;
import connect.utils.DialogUtil;
import connect.widget.TopToolBar;

/**
 * Transfer address book
 * Created by Administrator on 2016/12/21.
 */
public class TransferAddressBookActivity extends BaseActivity implements AddressBookContract.View {

    @Bind(R.id.toolbar_top)
    TopToolBar toolbarTop;
    @Bind(R.id.retycler_view)
    RecyclerView retyclerView;

    private Activity mActivity;
    private AddressBookContract.Presenter presenter;
    private AddAddressAdapter addAddressAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet_transfer_address_book);
        ButterKnife.bind(this);
        initView();
    }

    @Override
    public void initView() {
        mActivity = this;
        toolbarTop.setBlackStyle();
        toolbarTop.setLeftImg(R.mipmap.back_white);
        toolbarTop.setTitle(null, R.string.Wallet_Address_Book);
        toolbarTop.setRightImg(R.mipmap.camera2x);
        setPresenter(new AddressBookPresenter(this));

        addAddressAdapter = new AddAddressAdapter(onSideMenuListence);
        retyclerView.setAdapter(addAddressAdapter);
        retyclerView.setLayoutManager(new LinearLayoutManager(mActivity));
        retyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                addAddressAdapter.closeMenu();
            }
        });
        presenter.start();
    }

    @Override
    public Activity getActivity() {
        return mActivity;
    }

    @Override
    public void setPresenter(AddressBookContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @OnClick(R.id.left_img)
    void goback(View view) {
        ActivityUtil.goBack(mActivity);
    }

    @OnClick(R.id.right_lin)
    void goBook(View view) {
        ActivityUtil.nextBottomToTop(mActivity,ScanAddressActivity.class,null,ScanAddressActivity.BACK_CODE);
    }

    private AddAddressAdapter.OnSideMenuListence onSideMenuListence = new AddAddressAdapter.OnSideMenuListence(){
        @Override
        public void seleAddress(int position, AddressBean addressBean) {
            Bundle bundle = new Bundle();
            bundle.putString("address",addressBean.getAddress());
            ActivityUtil.goBackWithResult(mActivity,RESULT_OK,bundle);
        }

        @Override
        public void setTag(final int position, final AddressBean addressBean) {
            DialogUtil.showEditView(mActivity, mActivity.getResources().getString(R.string.Link_Set_Tag),
                    "", "", "", "", "",false,15,new DialogUtil.OnItemClickListener() {
                @Override
                public void confirm(String value) {
                    presenter.requestSetTag(addressBean.getAddress(),value,position);
                }
                @Override
                public void cancel() {

                }
            });
        }

        @Override
        public void delete(int position, AddressBean addressBean) {
            presenter.requestRemove(addressBean.getAddress(),position);
        }

        @Override
        public void addAddress(String address) {
            presenter.requestAddAddress(address);
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            if(requestCode == ScanAddressActivity.BACK_CODE){
                String address = data.getExtras().getString("address");
                presenter.requestAddAddress(address);
            }
        }
    }

    @Override
    public void updataView(ArrayList<AddressBean> listAddress) {
        addAddressAdapter.closeMenu();
        SharePreferenceUser.getInstance().putAddressBook(listAddress);
        addAddressAdapter.setData(listAddress);
    }
}
