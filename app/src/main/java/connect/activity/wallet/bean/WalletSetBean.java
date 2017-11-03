package connect.activity.wallet.bean;

import android.text.TextUtils;

import connect.database.green.DaoHelper.ParamManager;
import connect.utils.system.SystemDataUtil;

/**
 * Created by Administrator on 2017/11/2 0002.
 */

public class WalletSetBean {

    private String gesture; // 手势密码
    private String currency; // 系统默认转换货币
    private long fee; // 手续费
    private boolean autoFee; // 是否自动计算手续费
    private long autoMaxFee; // 自动计算手续费最大阀值

    public String getGesture() {
        return gesture;
    }

    public void setGesture(String gesture) {
        this.gesture = gesture;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public long getFee() {
        return fee;
    }

    public void setFee(long fee) {
        this.fee = fee;
    }

    public boolean isAutoFee() {
        return autoFee;
    }

    public void setAutoFee(boolean autoFee) {
        this.autoFee = autoFee;
    }

    public long getAutoMaxFee() {
        return autoMaxFee;
    }

    public void setAutoMaxFee(long autoMaxFee) {
        autoMaxFee = autoMaxFee;
    }

    public static WalletSetBean initWalletSet(){
        WalletSetBean walletSetBean = new WalletSetBean();
        walletSetBean.setAutoMaxFee(100000);
        walletSetBean.setAutoFee(false);
        walletSetBean.setFee(10000);
        walletSetBean.setGesture("");

        String countryCode = SystemDataUtil.getCountryCode();
        walletSetBean.setCurrency(TextUtils.isEmpty(countryCode) ? "" : countryCode);
        ParamManager.getInstance().putWalletSet(walletSetBean);
        return walletSetBean;
    }

    public static WalletSetBean putGesture(String gesture){
        WalletSetBean walletSetBean = ParamManager.getInstance().getWalletSet();
        walletSetBean.setGesture(gesture);
        ParamManager.getInstance().putWalletSet(walletSetBean);
        return walletSetBean;
    }

    public static WalletSetBean putCurrency(String currency){
        WalletSetBean walletSetBean = ParamManager.getInstance().getWalletSet();
        walletSetBean.setCurrency(currency);
        ParamManager.getInstance().putWalletSet(walletSetBean);
        return walletSetBean;
    }

    public static WalletSetBean putFee(long fee){
        WalletSetBean walletSetBean = ParamManager.getInstance().getWalletSet();
        walletSetBean.setFee(fee);
        ParamManager.getInstance().putWalletSet(walletSetBean);
        return walletSetBean;
    }

    public static WalletSetBean putAutoFee(boolean autoFee){
        WalletSetBean walletSetBean = ParamManager.getInstance().getWalletSet();
        walletSetBean.setAutoFee(autoFee);
        ParamManager.getInstance().putWalletSet(walletSetBean);
        return walletSetBean;
    }

    public static WalletSetBean putAutoMaxFee(long autoMaxFee){
        WalletSetBean walletSetBean = ParamManager.getInstance().getWalletSet();
        walletSetBean.setAutoMaxFee(autoMaxFee);
        ParamManager.getInstance().putWalletSet(walletSetBean);
        return walletSetBean;
    }

}
