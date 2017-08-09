package connect.wallet.cwallet;

import android.app.Activity;
import android.text.TextUtils;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import connect.activity.wallet.bean.WalletBean;
import connect.database.SharePreferenceUser;
import connect.database.green.DaoHelper.CurrencyHelper;
import connect.database.green.bean.CurrencyEntity;
import connect.utils.StringUtil;
import connect.utils.cryption.EncoPinBean;
import connect.utils.cryption.SupportKeyUril;
import connect.wallet.cwallet.account.BtcCoinAccount;
import connect.wallet.cwallet.account.CoinAccount;
import connect.wallet.cwallet.bean.CurrencyEnum;
import connect.wallet.cwallet.bean.PinBean;
import connect.wallet.cwallet.currency.BaseCurrency;
import connect.wallet.cwallet.currency.BtcCurrency;
import connect.wallet.cwallet.inter.WalletListener;
import wallet_gateway.WalletOuterClass;

/**
 * Wallet management tools
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
     * Access to designated account
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
     * Gets the specified currency
     * Access to specified currency
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
     * Check the password
     * @param listener
     */
    public void checkPin(Activity mActivity, final WalletListener listener){
        WalletBean walletBean = SharePreferenceUser.getInstance().getWalletInfo();
        baseWallet.checkPwd(mActivity, walletBean.getPayload(), new WalletListener<PinBean>() {
            @Override
            public void success(PinBean pinBean) {
                listener.success(pinBean);
            }

            @Override
            public void fail(WalletError error) {
                listener.fail(error);
            }
        });
    }

    /**
     * Display new password
     * Set a new password
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
     * Synchronous wallet information
     * @param listener
     */
    public void syncWalletInfo(final WalletListener listener){
        baseWallet.syncWallet(new WalletListener<List<WalletOuterClass.Coin>>() {
            @Override
            public void success(List<WalletOuterClass.Coin> list) {
                if(list == null){
                    // Users do not have to create a currency
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
     * Create a wallet
     * @param baseSeed
     * @param pwd
     */
    public void createWallet(String baseSeed, String pwd, final WalletListener listener) {
        baseWallet.createWallet(baseSeed, pwd, new WalletListener<WalletBean>() {
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
     * Update the purse
     * @param payload
     * @param ver
     */
    public void updateWallet(String payload, int ver, final WalletListener listener) {
        WalletBean walletBean =  SharePreferenceUser.getInstance().getWalletInfo();
        walletBean.setPayload(payload);
        walletBean.setVer(ver);
        baseWallet.updateWallet(walletBean, new WalletListener<WalletBean>() {
            @Override
            public void success(WalletBean bean) {
                listener.success(bean);
            }

            @Override
            public void fail(WalletError error) {
                listener.fail(error);
            }
        });
    }

    /**
     * Create a currency
     * @param currencyEnum
     * @param category (priKey\baseSeed\salt+seed)
     * @param value
     * @param pin
     * @param masterAddress
     * @param listener
     */
    public void createCurrency(CurrencyEnum currencyEnum, int category, String value, String pin, String masterAddress,
                               final WalletListener listener) {
        String payload = "";
        String salt = "";
        switch (category){
            case BaseCurrency.CATEGORY_PRIKEY:
                EncoPinBean encoPinBean = SupportKeyUril.encoPinDefult(BaseCurrency.CATEGORY_PRIKEY,value,pin);
                payload = encoPinBean.getPayload();
                break;
            case BaseCurrency.CATEGORY_BASESEED:
                salt = StringUtil.bytesToHexString(SecureRandom.getSeed(64));
                String currencySeed = SupportKeyUril.xor(value, salt);
                masterAddress = initCurrency(currencyEnum).createAddress(currencySeed);
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
     * Currency total balance
     * @param currencyEnum
     */
    public void balance(CurrencyEnum currencyEnum) {
        CoinAccount coinAccount = initAccount(currencyEnum);
        coinAccount.balance();
    }

    /**
     * Hidden address
     * Hide the address
     * @param currencyEnum
     * @param address
     */
    public void hideAddress(CurrencyEnum currencyEnum,String address) {
        CoinAccount coinAccount = initAccount (currencyEnum);
        coinAccount.hideAddress(address);
    }

    /**
     * Transfer accounts
     * @param currencyEnum
     * @param amount
     * @param fromAddress
     * @param toAddress
     */
    public void transfer(CurrencyEnum currencyEnum, double amount, List<String> fromAddress, List<String> toAddress, WalletListener walletListener) {
        CoinAccount coinAccount = initAccount(currencyEnum);
        BaseCurrency baseCurrency = initCurrency(currencyEnum);
        //coinAccount.transfer(baseCurrency, amount, fromAddress, toAddress);
    }
}
