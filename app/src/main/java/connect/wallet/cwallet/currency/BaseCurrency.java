package connect.wallet.cwallet.currency;

/**
 * 币种基类 定义了币种基础的方法
 * Created by Administrator on 2017/7/18.
 */

public abstract class BaseCurrency {

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
     * 创建币种
     */
    public void createCurrency(){

    }

    /**
     * 创建地址
     */
    public abstract void createAddress();

    /**
     * 手续费
     */
    public abstract void fee();
}
