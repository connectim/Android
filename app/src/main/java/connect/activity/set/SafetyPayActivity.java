package connect.activity.set;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wallet.NativeWallet;
import com.wallet.bean.CurrencyEnum;
import com.wallet.bean.EncryptionPinBean;
import com.wallet.currency.BaseCurrency;
import com.wallet.inter.WalletListener;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.activity.base.BaseActivity;
import connect.activity.set.bean.PaySetBean;
import connect.activity.wallet.bean.WalletBean;
import connect.activity.wallet.manager.PinBean;
import connect.activity.wallet.manager.PinManager;
import connect.activity.wallet.manager.WalletManager;
import connect.database.SharePreferenceUser;
import connect.database.green.DaoHelper.CurrencyHelper;
import connect.database.green.DaoHelper.ParamManager;
import connect.database.green.bean.CurrencyEntity;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.ToastEUtil;
import connect.utils.data.RateFormatUtil;
import connect.widget.TopToolBar;
import wallet_gateway.WalletOuterClass;

/**
 * Payment Settings.
 */
public class SafetyPayActivity extends BaseActivity {

    @Bind(R.id.toolbar_top)
    TopToolBar toolbarTop;
    @Bind(R.id.pay_pass_tv)
    TextView payPassTv;
    @Bind(R.id.pas_ll)
    LinearLayout pasLl;
    @Bind(R.id.miner_tv)
    TextView minerTv;
    @Bind(R.id.miner_ll)
    LinearLayout minerLl;

    private SafetyPayActivity mActivity;
    private PaySetBean paySetBean;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_payment);
        ButterKnife.bind(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        initView();
    }

    @Override
    public void initView() {
        mActivity = this;
        toolbarTop.setBlackStyle();
        toolbarTop.setLeftImg(R.mipmap.back_white);
        toolbarTop.setTitle(null, R.string.Set_Payment);

        paySetBean = ParamManager.getInstance().getPaySet();
        if (paySetBean != null) {
            minerTv.setText(paySetBean.isAutoFee() ? getString(R.string.Set_Auto) :
                    getString(R.string.Set_BTC_symbol) + " " + RateFormatUtil.longToDoubleBtc(paySetBean.getFee()));
        }

        if(!WalletManager.getInstance().isCreateWallet()){
            pasLl.setVisibility(View.INVISIBLE);
        }
    }

    @OnClick(R.id.left_img)
    void goBack(View view) {
        ActivityUtil.goBack(mActivity);
    }

    @OnClick(R.id.miner_ll)
    void goMinerFee(View view) {
        SafetyPayFeeActivity.startActivity(mActivity);
    }

    @OnClick(R.id.pas_ll)
    void goSetPassword(View view) {
        // Check payment password
        WalletBean walletBean = SharePreferenceUser.getInstance().getWalletInfo();
        PinManager.getInstance().checkPwd(mActivity, walletBean.getPayload(), new WalletListener<PinBean>() {
            @Override
            public void success(final PinBean pinBean) {
                // Set up to pay the password
                PinManager.getInstance().showSetNewPin(mActivity, new WalletListener<String>() {
                    @Override
                    public void success(String pin) {
                        requestPayload(pinBean, pin);
                    }

                    @Override
                    public void fail(WalletError error) {}
                });
            }

            @Override
            public void fail(WalletError error) {}
        });
    }

    /**
     * Set the new password needs to be updated wallet information and content
     *
     * @param pinBean old pass
     * @param pinNew new pass
     */
    private void requestPayload(final PinBean pinBean, final String pinNew) {
        final CurrencyEntity currencyEntity = CurrencyHelper.getInstance().loadCurrency(CurrencyEnum.BTC.getCode());
        final EncryptionPinBean encoPinBean = NativeWallet.getInstance().encryptionPin(BaseCurrency.CATEGORY_BASESEED, pinBean.getBaseSeed(),pinNew);

        WalletManager.getInstance().updateWallet(encoPinBean.getPayload(), encoPinBean.getVersion(), new WalletListener<WalletBean>() {
            @Override
            public void success(WalletBean bean) {
                if (currencyEntity.getCategory() == BaseCurrency.CATEGORY_PRIKEY) {
                    String priKey = NativeWallet.getInstance().decryptionPin(BaseCurrency.CATEGORY_PRIKEY, currencyEntity.getPayload(), pinBean.getPin());
                    EncryptionPinBean priEncoPinBean = NativeWallet.getInstance().encryptionPin(BaseCurrency.CATEGORY_PRIKEY, priKey, pinNew);

                    WalletManager.getInstance().setCurrencyInfo(priEncoPinBean.getPayload(), null, new WalletListener<WalletOuterClass.Coin>() {
                        @Override
                        public void success(WalletOuterClass.Coin coin) {
                            ToastEUtil.makeText(mActivity,R.string.Set_Set_success).show();
                        }

                        @Override
                        public void fail(WalletError error) {
                            ToastEUtil.makeText(mActivity,R.string.Set_Setting_Faied).show();
                        }
                    });
                } else {
                    ToastEUtil.makeText(mActivity,R.string.Set_Set_success).show();
                }
            }

            @Override
            public void fail(WalletError error) {
                ToastEUtil.makeText(mActivity,R.string.Set_Setting_Faied).show();
            }
        });
    }

}
