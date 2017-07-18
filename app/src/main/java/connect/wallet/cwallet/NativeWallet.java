package connect.wallet.cwallet;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import connect.wallet.cwallet.account.BtcCoinAccount;
import connect.wallet.cwallet.account.CoinAccount;
import connect.wallet.cwallet.bean.CurrencyEnum;
import connect.wallet.cwallet.currency.BaseCurrency;
import connect.wallet.cwallet.currency.BtcCurrency;
import connect.wallet.cwallet.inter.WalletListener;

/**
 * 钱包管理工具类
 * Created by Administrator on 2017/7/18.
 */
public class NativeWallet {

    private static NativeWallet nativeWallet;

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
    }

    private BaseWallet baseWallet;
    private Map<CurrencyEnum, BaseCurrency> baseCurrencyMap;
    private Map<CurrencyEnum, CoinAccount> coinAccountMap;

    /**
     * 钱包基本功能
     * @return
     */
    public BaseWallet initBaseWallet(){
        if(baseWallet==null){
            baseWallet=new BaseWallet();
        }
        return baseWallet;
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
     * 设置密码
     */
    public void setPwd() {
        BaseWallet baseWallet = initBaseWallet();
        baseWallet.setPwd();
    }

    /**
     * 校验密码
     * @param pwd 密码
     */
    public boolean checkPwd(String pwd) {
        BaseWallet baseWallet = initBaseWallet();
        return baseWallet.checkPwd(pwd);
    }

    /**
     * 创建钱包
     * @param currencyEnum 钱包类型
     * @param baseseed 种子
     * @param pwd 密码
     */
    public void createWallet(CurrencyEnum currencyEnum, String baseseed, String pwd, int n) {
        BaseWallet baseWallet = initBaseWallet();
        baseWallet.createWallet(currencyEnum, baseseed, pwd, n);
    }

    /**
     * 更新钱包
     * @param payload 加密种子
     * @param version 版本号
     */
    public void updateWallet(String payload, String version) {
        BaseWallet baseWallet = initBaseWallet();
        baseWallet.updateWallet();
    }

    /**
     * 创建币种
     * @param currencyEnum
     */
    public void createCurrency(CurrencyEnum currencyEnum) {
        BaseCurrency baseCurrency = initCurrency(currencyEnum);
        baseCurrency.createCurrency();
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
     * 获取地址列表
     * @param currencyEnum
     * @return
     */
    public List<Object> addressList(CurrencyEnum currencyEnum) {
        CoinAccount coinAccount = initAccount(currencyEnum);
        return coinAccount.addressList();
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
        coinAccount.transfer(baseCurrency, amount, fromAddress, toAddress);
    }
}
