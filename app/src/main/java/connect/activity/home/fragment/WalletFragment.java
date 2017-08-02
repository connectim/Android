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

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.activity.base.BaseFragment;
import connect.activity.home.bean.WalletMenuBean;
import connect.activity.wallet.PacketActivity;
import connect.activity.wallet.RequestActivity;
import connect.activity.wallet.ScanTransferActivity;
import connect.activity.wallet.TransactionActivity;
import connect.activity.wallet.TransferActivity;
import connect.activity.wallet.adapter.WalletMenuAdapter;
import connect.activity.wallet.bean.RateBean;
import connect.activity.wallet.bean.WalletBean;
import connect.database.MemoryDataManager;
import connect.database.green.DaoHelper.CurrencyHelper;
import connect.database.green.DaoHelper.ParamManager;
import connect.database.green.bean.CurrencyEntity;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.DialogUtil;
import connect.utils.data.RateFormatUtil;
import connect.utils.okhttp.HttpRequest;
import connect.wallet.cwallet.NativeWallet;
import connect.wallet.cwallet.bean.CurrencyEnum;
import connect.wallet.cwallet.currency.BaseCurrency;
import connect.wallet.cwallet.inter.WalletListener;
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
    @Bind(R.id.txt1)
    TextView txt1;
    @Bind(R.id.txt2)
    TextView txt2;
    @Bind(R.id.relativelayout_1)
    RelativeLayout relativelayout1;

    private FragmentActivity mActivity;
    private RateBean rateBean;
    private CurrencyEntity currencyEntity = null;

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

        WalletMenuAdapter walletMenuAdapter = new WalletMenuAdapter(menuList, mActivity);
        walletMenuRecycler.setLayoutManager(new GridLayoutManager(mActivity, 3));
        walletMenuRecycler.setAdapter(walletMenuAdapter);
        walletMenuAdapter.setOnItemClickListener(new WalletMenuAdapter.OnItemClickListener() {
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
                    }
                }
            }
        });
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
    void goback(View view) {
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

    @OnClick(R.id.txt2)
    void creatWalletClick() {
        int state = (int) txt2.getTag();
        switch (state) {
            case 1:
                NativeWallet.getInstance().showSetPin(mActivity, new WalletListener<String>() {
                    @Override
                    public void success(String pin) {
                        NativeWallet.getInstance().createCurrency(CurrencyEnum.BTC, BaseCurrency.CATEGORY_PRIKEY,
                                MemoryDataManager.getInstance().getPriKey(), pin,
                                MemoryDataManager.getInstance().getAddress(),
                                new WalletListener<CurrencyEntity>() {
                                    @Override
                                    public void success(CurrencyEntity tempEntity) {
                                        relativelayout1.setVisibility(View.GONE);
                                        currencyEntity = tempEntity;
                                    }

                                    @Override
                                    public void fail(WalletError error) {
                                    }
                                });
                    }

                    @Override
                    public void fail(WalletError error) {
                    }
                });
                break;
            case 2:
                Bundle bundle = new Bundle();
                bundle.putSerializable("type", CurrencyEnum.BTC);
                RandomVoiceActivity.startActivity(mActivity, bundle);
                break;
        }
    }

    /**
     * Synchronous wallet information
     */
    private void syncWallet() {
        relativelayout1.setVisibility(View.GONE);
        NativeWallet.getInstance().syncWalletInfo(new WalletListener<Integer>() {
            @Override
            public void success(Integer status) {
                txt2.setTag(status);
                switch (status){
                    case 0:// Have wallet data
                        currencyEntity = CurrencyHelper.getInstance().loadCurrency(CurrencyEnum.BTC.getCode());
                        long balance = currencyEntity.getBalance() == null ? 0 : currencyEntity.getBalance();
                        amountTv.setText(mActivity.getString(R.string.Set_BTC_symbol) + " " + RateFormatUtil.longToDoubleBtc(balance));
                        break;
                    case 1:// The user needs to create a currency for the old user
                        if (relativelayout1 != null) {
                            txt1.setText(getString(R.string.Wallet_not_update_wallet));
                            txt2.setText(getString(R.string.Wallet_Immediately_update));
                            relativelayout1.setVisibility(View.VISIBLE);
                        }
                        break;
                    case 2:// The user does not have a wallet and needs to create a wallet
                        if (relativelayout1 != null) {
                            txt1.setText(getString(R.string.Wallet_not_create_wallet));
                            txt2.setText(getString(R.string.Wallet_Immediately_create));
                            relativelayout1.setVisibility(View.VISIBLE);
                        }
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void fail(WalletError error) {

            }
        });
    }

    /**
     * Get wallet balance
     * Get the wallet balance
     */
    private void requestCurrencyCoin() {
        NativeWallet.getInstance().initCurrency(CurrencyEnum.BTC).requestCoinInfo(new WalletListener<WalletOuterClass.Coin>() {
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
            public void fail(WalletError error) {
            }
        });
    }

    /**
     * Asking rate
     * Request exchange rate
     */
    private void requestRate() {
        if (rateBean == null || TextUtils.isEmpty(rateBean.getUrl()))
            return;
        HttpRequest.getInstance().get(rateBean.getUrl(), new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

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
        });
    }

    /**
     * Collect random numbers to return
     * Get the collected random number
     *
     * @param bundle
     */
    public void callBaseSeed(Bundle bundle) {
        final String baseSend = bundle.getString("random");
        NativeWallet.getInstance().showSetPin(mActivity, new WalletListener<String>() {
            @Override
            public void success(String pin) {
                createWallet(baseSend, pin);
            }

            @Override
            public void fail(WalletError error) {
            }
        });
    }

    /**
     * Create a wallet
     *
     * @param baseSend
     * @param pin
     */
    private void createWallet(final String baseSend, final String pin) {
        NativeWallet.getInstance().createWallet(baseSend, pin, new WalletListener<WalletBean>() {
            @Override
            public void success(WalletBean walletBean) {
                NativeWallet.getInstance().createCurrency(CurrencyEnum.BTC, BaseCurrency.CATEGORY_BASESEED, baseSend, pin, "",
                        new WalletListener<CurrencyEntity>() {
                            @Override
                            public void success(CurrencyEntity tempEntity) {
                                relativelayout1.setVisibility(View.GONE);
                                currencyEntity = tempEntity;
                            }

                            @Override
                            public void fail(WalletError error) {
                            }
                        });
            }

            @Override
            public void fail(WalletError error) {
            }
        });
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }
}