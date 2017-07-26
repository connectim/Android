package connect.wallet.cwallet;

import android.app.Activity;
import android.text.TextUtils;
import android.widget.Toast;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import connect.activity.base.BaseApplication;
import connect.activity.wallet.bean.WalletBean;
import connect.database.SharePreferenceUser;
import connect.database.green.DaoHelper.CurrencyHelper;
import connect.database.green.bean.CurrencyEntity;
import connect.utils.StringUtil;
import connect.utils.UriUtil;
import connect.utils.cryption.DecryptionUtil;
import connect.utils.cryption.EncoPinBean;
import connect.utils.cryption.SupportKeyUril;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import connect.wallet.cwallet.account.BtcCoinAccount;
import connect.wallet.cwallet.account.CoinAccount;
import connect.wallet.cwallet.bean.CurrencyEnum;
import connect.wallet.cwallet.currency.BaseCurrency;
import connect.wallet.cwallet.currency.BtcCurrency;
import connect.wallet.cwallet.inter.WalletListener;
import connect.wallet.jni.AllNativeMethod;
import protos.Connect;
import wallet_gateway.WalletOuterClass;

/**
 * 钱包管理工具类
 * Created by Administrator on 2017/7/18.
 */
public class NativeWallet {

    private static NativeWallet nativeWallet;
    private BaseWallet baseWallet;
    private Map<CurrencyEnum, BaseCurrency> baseCurrencyMap;
    private Map<CurrencyEnum, CoinAccount> coinAccountMap;

    public static NativeWallet getInstance() {
        if (nativeWallet == null) {
            synchronized (NativeWallet.class) {
                if (nativeWallet == null) {
                    nativeWallet = new NativeWallet();
                }
            }
        }
        return nativeWallet;
    }

    public NativeWallet() {
        baseCurrencyMap = new HashMap<>();
        coinAccountMap = new HashMap<>();
        baseWallet = new BaseWallet();
    }

    /**
     * 获取指定账户
     * @param currencyEnum
     * @return
     */
    public CoinAccount initAccount (CurrencyEnum currencyEnum){
        if (coinAccountMap == null) {
            coinAccountMap = new HashMap<>();
        }

        CoinAccount coinAccount = coinAccountMap.get(currencyEnum);
        if (coinAccount == null) {
            switch (currencyEnum) {
                case BTC:
                    coinAccount = new BtcCoinAccount();
                    coinAccountMap.put(currencyEnum, coinAccount);
                    break;
            }
        }
        return coinAccount;
    }

    /**
     * 获取指定币种
     * @param currencyEnum
     * @return
     */
    public BaseCurrency initCurrency(CurrencyEnum currencyEnum) {
        if (baseCurrencyMap == null) {
            baseCurrencyMap = new HashMap<>();
        }

        BaseCurrency baseCurrency = baseCurrencyMap.get(currencyEnum);
        if (baseCurrency == null) {
            switch (currencyEnum) {
                case BTC:
                    baseCurrency = new BtcCurrency();
                    baseCurrencyMap.put(currencyEnum, baseCurrency);
                    break;
            }
        }
        return baseCurrency;
    }

    /**
     * 检查密码
     * @param listener
     */
    public void checkPin(Activity mActivity, final WalletListener listener){
        final WalletBean walletBean = SharePreferenceUser.getInstance().getWalletInfo();
        if(walletBean != null){
            String payload = "";
            if(!TextUtils.isEmpty(walletBean.getPayload())){
                payload = walletBean.getPayload();
            }else{
                CurrencyEntity currencyEntity = CurrencyHelper.getInstance().loadCurrency(CurrencyEnum.BTC.getCode());
                payload = currencyEntity.getPayload();
            }
            baseWallet.checkPwd(mActivity,payload, new WalletListener<String>() {
                @Override
                public void success(String seed) {
                    listener.success(seed);
                }

                @Override
                public void fail(WalletError error) {
                    listener.fail(error);
                }
            });
        }else{
            listener.fail(WalletListener.WalletError.DBError);
        }
    }

    /**
     * 显示输入新密码
     * @param listen
     */
    public void showSetPin(Activity mActivity, final WalletListener listen){
        baseWallet.showSetNewPin(mActivity,new WalletListener<String>() {
            @Override
            public void success(String pin) {
                listen.success(pin);
            }

            @Override
            public void fail(WalletError error) {

            }
        });
    }

    /**
     * 重置支付密码
     */
    public void setPin(Activity mActivity,final String seed, final WalletListener listener) {
        baseWallet.showSetNewPin(mActivity,new WalletListener<String>() {
            @Override
            public void success(String pin) {
                WalletBean walletBean =  SharePreferenceUser.getInstance().getWalletInfo();
                EncoPinBean encoPinBean = SupportKeyUril.encoPinDefult(seed,pin);
                if(!TextUtils.isEmpty(walletBean.getPayload())){
                    // baseSeed的方式需要同步钱包信息
                    walletBean.setPayload(encoPinBean.getPayload());
                    walletBean.setVer(SupportKeyUril.PIN_VERSION);
                    baseWallet.updateWallet(walletBean, new WalletListener<WalletBean>() {
                        @Override
                        public void success(WalletBean bean) {
                            listener.success(WalletListener.success);
                        }

                        @Override
                        public void fail(WalletError error) {
                            listener.fail(error);
                        }
                    });
                }else{
                    // priKey加密方式需要同步货币信息
                    BaseCurrency baseCurrency = initCurrency(CurrencyEnum.BTC);
                    CurrencyEntity currencyEntity = baseCurrency.getCurrencyData();
                    currencyEntity.setPayload(encoPinBean.getPayload());
                    baseCurrency.setCurrencyInfo(currencyEntity, new WalletListener<CurrencyEntity>() {
                        @Override
                        public void success(CurrencyEntity entity) {
                            listener.success(WalletListener.success);
                        }

                        @Override
                        public void fail(WalletError error) {
                            listener.success(error);
                        }
                    });
                }
            }

            @Override
            public void fail(WalletError error) {

            }
        });
    }

    /**
     * 同步钱包信息
     * @param listener
     */
    public void syncWalletInfo(final WalletListener listener){
        baseWallet.syncWallet(new WalletListener<List<WalletOuterClass.Coin>>() {
            @Override
            public void success(List<WalletOuterClass.Coin> list) {
                if(list == null){
                    //用户没有创建过币种
                    baseWallet.requestUserStatus(new WalletListener<Integer>() {
                        @Override
                        public void success(Integer category) {
                            listener.success(category);
                        }
                        @Override
                        public void fail(WalletError error) {}
                    });
                }else{
                    listener.success(0);
                }
            }

            @Override
            public void fail(WalletError error) {

            }
        });
    }

    /**
     * 创建钱包
     * @param baseseed 种子
     * @param pwd 密码
     */
    public void createWallet(String baseseed, String pwd, final WalletListener listener) {
        baseWallet.createWallet(baseseed, pwd, new WalletListener<WalletBean>() {
            @Override
            public void success(WalletBean walletBean) {
                listener.success(walletBean);
            }

            @Override
            public void fail(WalletError error) {
                listener.fail(error);
            }
        });
    }

    /**
     * 更新钱包
     * @param payload 加密种子
     * @param ver 加密版本号
     */
    public void updateWallet(String payload, int ver) {
        WalletBean walletBean =  SharePreferenceUser.getInstance().getWalletInfo();
        walletBean.setPayload(payload);
        walletBean.setVer(ver);
        baseWallet.updateWallet(walletBean, new WalletListener<WalletBean>() {
            @Override
            public void success(WalletBean bean) {

            }

            @Override
            public void fail(WalletError error) {

            }
        });
    }

    /**
     * 创建币种(priKey\baseSeed\salt+seed)
     * @param currencyEnum
     */
    public void createCurrency(CurrencyEnum currencyEnum, int category, String value, String pin, String masterAddress,
                               final WalletListener listener) {
        String payload = "";
        String salt = "";
        switch (category){
            case BaseCurrency.CATEGORY_PRIKEY:
                String valueHex = StringUtil.bytesToHexString(value.getBytes());
                EncoPinBean encoPinBean = SupportKeyUril.encoPinDefult(valueHex,pin);
                payload = encoPinBean.getPayload();
                break;
            case BaseCurrency.CATEGORY_BASESEED:
                salt = AllNativeMethod.cdGetHash256(StringUtil.bytesToHexString(SecureRandom.getSeed(64)));
                String currencySeend = SupportKeyUril.xor(value, salt, 64);
                masterAddress = initCurrency(currencyEnum).ceaterAddress(currencySeend);
                break;
            case BaseCurrency.CATEGORY_SALT_SEED:
                break;
            default:
                break;
        }
        baseWallet.createCurrency(currencyEnum, payload, salt, category, masterAddress, new WalletListener<CurrencyEntity>() {
            @Override
            public void success(CurrencyEntity currencyEntity) {
                listener.success(currencyEntity);
            }

            @Override
            public void fail(WalletError error) {
                listener.fail(error);
            }
        });
    }

    /**
     * 创建地址
     * @param currencyEnum
     */
    public void createAddress(CurrencyEnum currencyEnum){
        BaseCurrency baseCurrency = initCurrency(currencyEnum);
        baseCurrency.createAddress();
    }

    /**
     * 币种总余额
     * @param currencyEnum
     */
    public void balance(CurrencyEnum currencyEnum) {
        CoinAccount coinAccount = initAccount(currencyEnum);
        coinAccount.balance();
    }

    /**
     * 隐藏地址
     * @param currencyEnum
     * @param address
     */
    public void hideAddress(CurrencyEnum currencyEnum,String address) {
        CoinAccount coinAccount = initAccount (currencyEnum);
        coinAccount.hideAddress(address);
    }



    /**
     * 转账
     * @param currencyEnum 币种类型
     * @param amount 金额
     * @param fromAddress 输入地址列表
     * @param toAddress 输出地址列表
     */
    public void transfer(CurrencyEnum currencyEnum, double amount, List<String> fromAddress, List<String> toAddress, WalletListener walletListener) {
        CoinAccount coinAccount = initAccount(currencyEnum);
        BaseCurrency baseCurrency = initCurrency(currencyEnum);
        //coinAccount.transfer(baseCurrency, amount, fromAddress, toAddress);
    }
}
