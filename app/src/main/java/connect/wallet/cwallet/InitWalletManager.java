package connect.wallet.cwallet;

import android.app.Activity;
import android.os.Bundle;

import connect.activity.wallet.bean.WalletBean;
import connect.database.MemoryDataManager;
import connect.database.green.DaoHelper.CurrencyHelper;
import connect.database.green.bean.CurrencyEntity;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.DialogUtil;
import connect.wallet.cwallet.bean.CurrencyEnum;
import connect.wallet.cwallet.currency.BaseCurrency;
import connect.wallet.cwallet.inter.WalletListener;
import connect.widget.random.RandomVoiceActivity;

/**
 * Created by Administrator on 2017/8/2 0002.
 */

public class InitWalletManager {

    private Activity mActivity;
    private WalletListener listener;
    private final CurrencyEnum currencyEnum;

    public InitWalletManager(Activity mActivity, CurrencyEnum currencyEnum) {
        this.mActivity = mActivity;
        this.currencyEnum = currencyEnum;
    }

    public void checkWallet(boolean isUpdateAmount, WalletListener listener){
        this.listener = listener;
        CurrencyEntity currencyEntity = CurrencyHelper.getInstance().loadCurrency(currencyEnum.getCode());
        if (currencyEntity == null || isUpdateAmount) {
            syncWallet();
        }else{
            listener.success(currencyEntity);
        }
    }

    public void syncWallet(){
        NativeWallet.getInstance().syncWalletInfo(new WalletListener<Integer>() {
            @Override
            public void success(Integer status) {
                switch (status) {
                    case 0:
                        CurrencyEntity currencyEntity = CurrencyHelper.getInstance().loadCurrency(currencyEnum.getCode());
                        listener.success(currencyEntity);
                        break;
                    case 1:
                    case 2:
                        createWallet(status);
                        break;
                    default:
                        break;
                }
            }
            @Override
            public void fail(WalletError error) {}
        });
    }

    public void createWallet(final int status){
        String massage = "";
        String okMassage = "";
        if(status == 1){
            massage = mActivity.getString(R.string.Wallet_not_update_wallet);
            okMassage = mActivity.getString(R.string.Wallet_Immediately_update);
        }else if(status == 2){
            massage = mActivity.getString(R.string.Wallet_not_create_wallet);
            okMassage = mActivity.getString(R.string.Wallet_Immediately_create);
        }
        DialogUtil.showAlertTextView(mActivity, mActivity.getString(R.string.Set_tip_title),
                massage, "", okMassage, false, new DialogUtil.OnItemClickListener() {
                    @Override
                    public void confirm(String value) {
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("type", CurrencyEnum.BTC);
                        bundle.putInt("status",status);
                        RandomVoiceActivity.startActivity(mActivity,bundle);
                    }

                    @Override
                    public void cancel() {
                        ActivityUtil.goBack(mActivity);
                    }
                });
    }

    public void requestCreateWallet(final String baseSend, final String pin, final int status){
        NativeWallet.getInstance().createWallet(baseSend, pin, new WalletListener<WalletBean>() {
            @Override
            public void success(WalletBean walletBean) {
                int category = 0;
                String value = "";
                String masterAddress = "";
                if(status == 1){
                    category = BaseCurrency.CATEGORY_PRIKEY;
                    value = MemoryDataManager.getInstance().getPriKey();
                    masterAddress = MemoryDataManager.getInstance().getAddress();
                }else if(status == 2){
                    category = BaseCurrency.CATEGORY_BASESEED;
                    value = baseSend;
                    masterAddress = "";
                }
                NativeWallet.getInstance().createCurrency(CurrencyEnum.BTC, category, value, pin, masterAddress,
                        new WalletListener<CurrencyEntity>() {
                            @Override
                            public void success(CurrencyEntity currencyEntity) {
                                listener.success(currencyEntity);
                            }
                            @Override
                            public void fail(WalletError error) {}
                        });
            }
            @Override
            public void fail(WalletError error) {}
        });
    }

    public void setListener(WalletListener listener){
        this.listener = listener;
    }

    public void setmActivity(Activity mActivity) {
        this.mActivity = mActivity;
    }
}
