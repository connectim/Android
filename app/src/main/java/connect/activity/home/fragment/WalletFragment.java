package connect.activity.home.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wallet.bean.CurrencyEnum;
import com.wallet.inter.WalletListener;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.activity.base.BaseFragment;
import connect.activity.home.bean.WalletMenuBean;
import connect.activity.set.GeneralActivity;
import connect.activity.wallet.PacketActivity;
import connect.activity.wallet.RequestActivity;
import connect.activity.wallet.SafetySetActivity;
import connect.activity.wallet.ScanTransferActivity;
import connect.activity.wallet.TransactionActivity;
import connect.activity.wallet.TransferActivity;
import connect.activity.wallet.adapter.WalletMenuAdapter;
import connect.activity.wallet.bean.RateBean;
import connect.activity.wallet.manager.WalletManager;
import connect.database.green.DaoHelper.CurrencyHelper;
import connect.database.green.DaoHelper.ParamManager;
import connect.database.green.bean.CurrencyEntity;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.data.RateFormatUtil;
import connect.utils.okhttp.HttpRequest;
import connect.widget.TopToolBar;
import connect.widget.random.RandomVoiceActivity;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import wallet_gateway.WalletOuterClass;

/**
 * wallet
 */
public class WalletFragment extends BaseFragment {

    @Bind(R.id.wallet_menu_recycler)
    RecyclerView walletMenuRecycler;
    @Bind(R.id.toolbar_top)
    TopToolBar toolbarTop;
    @Bind(R.id.amount_tv)
    TextView amountTv;
    @Bind(R.id.create_hint)
    TextView createHint;
    @Bind(R.id.create_btn)
    TextView createBtn;
    @Bind(R.id.create_rela)
    RelativeLayout createRela;

    private FragmentActivity mActivity;
    private RateBean rateBean;
    private CurrencyEntity currencyEntity = null;
    private int userType;

    public static WalletFragment startFragment() {
        WalletFragment walletFragment = new WalletFragment();
        return walletFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wallet, container, false);
        ButterKnife.bind(this, view);
        mActivity = getActivity();
        initView();
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        rateBean = ParamManager.getInstance().getCountryRate();
        if (mActivity != null && isAdded()) {
            requestRate();
            requestCurrencyCoin();
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
        menuList.add(new WalletMenuBean(R.mipmap.wallet_packet_icon2x, R.string.Set_Setting));

        WalletMenuAdapter walletMenuAdapter = new WalletMenuAdapter(menuList, mActivity);
        walletMenuRecycler.setLayoutManager(new GridLayoutManager(mActivity, 3));
        walletMenuRecycler.setAdapter(walletMenuAdapter);
        walletMenuAdapter.setOnItemClickListener(onItemClickListener);

        amountTv.setText(mActivity.getString(R.string.Set_BTC_symbol) + " " + RateFormatUtil.longToDoubleBtc(0));
        List<CurrencyEntity> listCurrency = CurrencyHelper.getInstance().loadCurrencyList();
        if (listCurrency == null || listCurrency.size() == 0) {
            syncWallet();
        }
    }

    @OnClick(R.id.right_lin)
    void goScan(View view) {
        if (currencyEntity != null) {
            ActivityUtil.nextBottomToTop(mActivity, ScanTransferActivity.class, null, -1);
        }
    }

    @OnClick(R.id.left_img)
    void goBack(View view) {
        if (currencyEntity != null) {
            ActivityUtil.next(mActivity, TransactionActivity.class);
        }
    }

    @OnClick(R.id.amount_tv)
    void switchAccount(View view) {
        String account = amountTv.getText().toString();
        if (mActivity == null || !isAdded() || currencyEntity == null)
            return;

        long balance = currencyEntity.getBalance() == null ? 0 : currencyEntity.getBalance();
        if (account.contains(mActivity.getString(R.string.Set_BTC_symbol)) && rateBean != null && rateBean.getRate() != null) {
            amountTv.setText(rateBean.getSymbol() + " " +
                    RateFormatUtil.foematNumber(RateFormatUtil.PATTERN_OTHER,
                            balance * rateBean.getRate() / RateFormatUtil.BTC_TO_LONG));
        } else {
            amountTv.setText(mActivity.getString(R.string.Set_BTC_symbol) + " " + RateFormatUtil.longToDoubleBtc(balance));
        }
    }

    @OnClick(R.id.create_btn)
    void createWalletClick() {
        userType = (int) createBtn.getTag();
        Bundle bundle = new Bundle();
        bundle.putSerializable("type", CurrencyEnum.BTC);
        bundle.putInt("status", userType);
        RandomVoiceActivity.startActivity(mActivity,bundle);
    }

    WalletMenuAdapter.OnItemClickListener onItemClickListener = new WalletMenuAdapter.OnItemClickListener(){
        @Override
        public void itemClick(int position) {
            if (currencyEntity != null) {
                switch (position) {
                    case 0:
                        ActivityUtil.next(mActivity, RequestActivity.class);
                        break;
                    case 1:
                        TransferActivity.startActivity(mActivity);
                        break;
                    case 2:
                        PacketActivity.startActivity(mActivity);
                        break;
                    case 3:
                        ActivityUtil.next(mActivity, SafetySetActivity.class);
                        break;
                }
            }
        }
    };

    /**
     * Synchronous wallet information
     */
    private void syncWallet() {
        WalletManager.getInstance().syncWallet(new WalletListener<Integer>() {
            @Override
            public void success(Integer status) {
                createBtn.setTag(status);
                switch (status){
                    case 0:// Have wallet data
                        currencyEntity = CurrencyHelper.getInstance().loadCurrency(CurrencyEnum.BTC.getCode());
                        long balance = currencyEntity.getBalance() == null ? 0 : currencyEntity.getBalance();
                        amountTv.setText(mActivity.getString(R.string.Set_BTC_symbol) + " " + RateFormatUtil.longToDoubleBtc(balance));
                        break;
                    case 1:// The user needs to create a currency for the old user
                        if (createRela != null) {
                            createHint.setText(getString(R.string.Wallet_not_update_wallet));
                            createBtn.setText(getString(R.string.Wallet_Immediately_update));
                            createRela.setVisibility(View.VISIBLE);
                        }
                        break;
                    case 2:// The user does not have a wallet and needs to create a wallet
                        if (createRela != null) {
                            createHint.setText(getString(R.string.Wallet_not_create_wallet));
                            createBtn.setText(getString(R.string.Wallet_Immediately_create));
                            createRela.setVisibility(View.VISIBLE);
                        }
                        break;
                    default:
                        break;
                }
            }
            @Override
            public void fail(WalletError error) {}
        });
    }

    /**
     * Collect random numbers to return
     * @param bundle
     */
    public void callBaseSeed(Bundle bundle) {
        final String baseSend = bundle.getString("random");
        final String pin = bundle.getString("pin");
        final int status = bundle.getInt("status");

        WalletManager.getInstance().createWallet(baseSend, pin, status ,new WalletListener<CurrencyEntity>(){
            @Override
            public void success(CurrencyEntity entity) {
                createRela.setVisibility(View.GONE);
                currencyEntity = entity;
            }

            @Override
            public void fail(WalletError error) {}
        });
    }

    /**
     * Get wallet balance
     */
    private void requestCurrencyCoin() {
        WalletManager.getInstance().requestCoinInfo(CurrencyEnum.BTC, new WalletListener<WalletOuterClass.Coin>() {
            @Override
            public void success(WalletOuterClass.Coin coin) {
                if (coin != null) {
                    currencyEntity = CurrencyHelper.getInstance().loadCurrency(CurrencyEnum.BTC.getCode());
                    long balance = currencyEntity.getBalance() == null ? 0 : currencyEntity.getBalance();
                    amountTv.setText(mActivity.getString(R.string.Set_BTC_symbol) + " " + RateFormatUtil.longToDoubleBtc(balance));
                } else {
                    currencyEntity = CurrencyHelper.getInstance().loadCurrency(CurrencyEnum.BTC.getCode());
                    amountTv.setText(mActivity.getString(R.string.Set_BTC_symbol) + " " + RateFormatUtil.longToDoubleBtc(0L));
                }
            }
            @Override
            public void fail(WalletError error) {}
        });
    }

    /**
     * Request exchange rate
     */
    private void requestRate() {
        if (rateBean == null || TextUtils.isEmpty(rateBean.getUrl()))
            return;
        HttpRequest.getInstance().get(rateBean.getUrl(), new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String data = response.body().string();
                Type type = new TypeToken<RateBean>() {
                }.getType();
                RateBean rate = new Gson().fromJson(data, type);
                rateBean.setRate(rate.getRate());
                rateBean.setDatetime(rate.getDatetime());
                ParamManager.getInstance().putCountryRate(rateBean);
            }
            @Override
            public void onFailure(Call call, IOException e) {}
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }
}