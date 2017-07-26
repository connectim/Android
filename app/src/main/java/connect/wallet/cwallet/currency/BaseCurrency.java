package connect.wallet.cwallet.currency;

import java.util.ArrayList;

import connect.database.green.bean.CurrencyEntity;
import connect.wallet.cwallet.inter.WalletListener;

/**
 * 币种基类 定义了币种基础的方法
 * Created by Administrator on 2017/7/18.
 */

public abstract class BaseCurrency {

    // 1:纯私钥，2:baseSeed，3:salt+seed
    public static final int CATEGORY_PRIKEY = 1;
    public static final int CATEGORY_BASESEED = 2;
    public static final int CATEGORY_SALT_SEED = 3;
    /**
     * 广播
     */
    public void publish(String hashid, String rawtx) {

    }

    /**
     * 签名交易
     *
     * @param signraw 由外部组装指定币种的签名字符串
     */
    public void signRawTx(String signraw) {

    }

    /**
     * 创建地址
     */
    public abstract void createAddress();

    /**
     * 获取币种信息
     */
    public abstract void requestCoinInfo(WalletListener listener);

    /**
     * 手续费
     */
    public abstract void fee();

    /**
     * 获取货币数据
     */
    public abstract CurrencyEntity getCurrencyData();

    /**
     * 设置币种信息
     */
    public abstract void setCurrencyInfo(final CurrencyEntity currencyEntity, WalletListener listener);

    /**
     * 获取货币地址
     */
    public abstract String ceaterAddress(String currencySeed);

    /**
     * 获取货币私钥
     */
    public abstract String ceaterPriKey(String baseSeed, String salt,int index);

    /**
     * 签名交易
     */
    public abstract String getSignRawTrans(ArrayList<String> priList, String tvs, String rowhex);

    /**
     * 货币转换成对应Long型
     */
    public abstract long doubleToLongCurrency(double value);

    /**
     * Long型转换成对应货币
     */
    public abstract String longToDoubleCurrency(long value);

}

