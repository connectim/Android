package connect.wallet.cwallet;

import connect.wallet.cwallet.bean.CurrencyEnum;

/**
 * Created by Administrator on 2017/7/18.
 */

public class BaseWallet {

    /**
     * 设置钱包密码,修改钱包密码后 需要同步下更新下钱包接口，重新加密baseSsed上传
     */
    public void setPwd(){

    }

    /**
     * 校验密码
     */
    public boolean checkPwd(String pwd) {
        return true;
    }

    /**
     * 创建钱包
     */
    public void createWallet(CurrencyEnum currencyEnum, String baseseed, String pwd, int n) {

    }

    /**
     * 更新钱包
     */
    public void updateWallet(){

    }
}
