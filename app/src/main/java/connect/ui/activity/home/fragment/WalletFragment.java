package connect.ui.activity.home.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.db.MemoryDataManager;
import connect.db.SharedPreferenceUtil;
import connect.db.green.DaoHelper.ParamManager;
import connect.ui.activity.R;
import connect.ui.activity.home.bean.WalletMenuBean;
import connect.ui.activity.wallet.PacketActivity;
import connect.ui.activity.wallet.RequestActivity;
import connect.ui.activity.wallet.ScanTransferActivity;
import connect.ui.activity.wallet.TransactionActivity;
import connect.ui.activity.wallet.TransferActivity;
import connect.ui.activity.wallet.adapter.WalletMenuAdapter;
import connect.ui.activity.wallet.bean.RateBean;
import connect.ui.activity.wallet.bean.WalletAccountBean;
import connect.ui.base.BaseFragment;
import connect.utils.ActivityUtil;
import connect.utils.ProtoBufUtil;
import connect.utils.data.RateFormatUtil;
import connect.utils.UriUtil;
import connect.utils.okhttp.HttpRequest;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import connect.view.TopToolBar;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import protos.Connect;

/**
 * wallet
 * Created by Administrator on 2016/12/1.
 */
public class WalletFragment extends BaseFragment {

    @Bind(R.id.wallet_menu_recycler)
    RecyclerView walletMenuRecycler;
    @Bind(R.id.toolbar_top)
    TopToolBar toolbarTop;
    @Bind(R.id.amount_tv)
    TextView amountTv;

    private FragmentActivity mActivity;
    private WalletAccountBean accountBean;
    private RateBean rateBean;

    public static WalletFragment startFragment() {
        WalletFragment walletFragment = new WalletFragment();
        return walletFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wallet, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        mActivity = getActivity();
        initView();
        rateBean = ParamManager.getInstance().getCountryRate();
        accountBean = ParamManager.getInstance().getWalletAmount();
        if(accountBean == null)
            accountBean = new WalletAccountBean(0L, 0L);
        if(mActivity != null && isAdded()){
            amountTv.setText(mActivity.getString(R.string.Set_BTC_symbol) + " " + RateFormatUtil.longToDoubleBtc(accountBean.getAmount()));
            requestRate();
            requestWallet();
        }
    }

    private void initView() {
        toolbarTop.setBlackStyle();
        toolbarTop.setTitle(null, R.string.Wallet_Wallet);
        toolbarTop.setRightImg(R.mipmap.wallet_camera_icon2x);
        toolbarTop.setLeftImg(R.mipmap.wallet_transactioons_icon2x);

        ArrayList menuList = new ArrayList<>();
        menuList.add(new WalletMenuBean(R.mipmap.wallet_request2x, R.string.Wallet_Receipt));
        menuList.add(new WalletMenuBean(R.mipmap.wallet_transfer_icon2x, R.string.Wallet_Transfer));
        menuList.add(new WalletMenuBean(R.mipmap.wallet_packet_icon2x, R.string.Wallet_Packet));

        WalletMenuAdapter walletMenuAdapter = new WalletMenuAdapter(menuList, mActivity);
        walletMenuAdapter.setOnItemClickListence(onClickListener);
        walletMenuRecycler.setLayoutManager(new GridLayoutManager(mActivity, 3));
        walletMenuRecycler.setAdapter(walletMenuAdapter);
    }

    @OnClick(R.id.right_lin)
    void goScan(View view) {
        ActivityUtil.nextBottomToTop(mActivity, ScanTransferActivity.class,null,-1);
    }

    @OnClick(R.id.left_img)
    void goback(View view) {
        ActivityUtil.next(mActivity, TransactionActivity.class);
    }

    @OnClick(R.id.amount_tv)
    void switchAccount(View view) {
        String account = amountTv.getText().toString();
        if(mActivity == null || !isAdded())
            return;

        if (account.contains(mActivity.getString(R.string.Set_BTC_symbol)) && rateBean != null && rateBean.getRate() != null) {
            amountTv.setText(rateBean.getSymbol() + " " +
                    RateFormatUtil.foematNumber(RateFormatUtil.PATTERN_OTHER,
                    accountBean.getAmount()*rateBean.getRate()/RateFormatUtil.BTC_TO_LONG));
        }else{
            amountTv.setText(mActivity.getString(R.string.Set_BTC_symbol) + " " + RateFormatUtil.longToDoubleBtc(accountBean.getAmount()));
        }
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int tag = (Integer) v.getTag();
            switch (tag) {
                case 0:
                    ActivityUtil.next(mActivity, RequestActivity.class);
                    break;
                case 1:
                    TransferActivity.startActivity(mActivity);
                    break;
                case 2:
                    PacketActivity.startActivity(mActivity);
                    break;
            }
        }
    };

    private void requestRate() {
        if(rateBean == null || TextUtils.isEmpty(rateBean.getUrl()))
            return;
        HttpRequest.getInstance().get(rateBean.getUrl(), new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String data = response.body().string();
                Type type = new TypeToken<RateBean>() {}.getType();
                RateBean rate = new Gson().fromJson(data, type);
                rateBean.setRate(rate.getRate());
                rateBean.setDatetime(rate.getDatetime());
                ParamManager.getInstance().putCountryRate(rateBean);
            }
        });
    }

    private void requestWallet() {
        String url = String.format(UriUtil.BLOCKCHAIN_UNSPENT_INFO, MemoryDataManager.getInstance().getAddress());
        OkHttpUtil.getInstance().get(url, new ResultCall<Connect.HttpNotSignResponse>() {
            @Override
            public void onResponse(Connect.HttpNotSignResponse response) {
                try {
                    if (response.getCode() == 2000) {
                        Connect.UnspentAmount unspentAmount = Connect.UnspentAmount.parseFrom(response.getBody());
                        if(ProtoBufUtil.getInstance().checkProtoBuf(unspentAmount)){
                            WalletAccountBean accountBean = new WalletAccountBean(unspentAmount.getAmount(), unspentAmount.getAvaliableAmount());
                            ParamManager.getInstance().putWalletAmount(accountBean);
                            amountTv.setText(mActivity.getString(R.string.Set_BTC_symbol) + " " + RateFormatUtil.longToDoubleBtc(accountBean.getAmount()));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Connect.HttpNotSignResponse response) {

            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }
}
