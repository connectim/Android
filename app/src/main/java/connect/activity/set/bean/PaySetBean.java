package connect.activity.set.bean;

import java.io.Serializable;

import protos.Connect;

/**
 * Created by Administrator on 2016/12/7.
 */
public class PaySetBean implements Serializable {

    private String payPin;
    private Boolean noSecretPay;
    private Boolean fingerprint;
    private Long fee;
    private boolean isAutoFee;
    private Long autoMaxFee;
    private String versionPay;

    public PaySetBean(String paypin,String versionPay, Long fee, Long autoMaxFee, boolean isAutoFee, Boolean fingerprint, Boolean noSecretPay) {
        this.payPin = paypin;
        this.versionPay = versionPay;
        this.fee = fee;
        this.autoMaxFee = autoMaxFee;
        this.isAutoFee = isAutoFee;
        this.fingerprint = fingerprint;
        this.noSecretPay = noSecretPay;
    }

    public PaySetBean(Connect.PaymentSetting paymentSetting) {
        this.setPayPin(paymentSetting.getPayPin());
        this.setNoSecretPay(paymentSetting.getNoSecretPay());
        if(paymentSetting.getFee() == 0){
            this.setFee(10000L);
        }else{
            this.setFee(paymentSetting.getFee());
        }
        this.setAutoFee(false);
        this.setAutoMaxFee(10000L);
        this.setVersionPay("0");
    }

    public String getPayPin() {
        return payPin;
    }

    public void setPayPin(String payPin) {
        this.payPin = payPin;
    }

    public Boolean getNoSecretPay() {
        return noSecretPay;
    }

    public void setNoSecretPay(Boolean noSecretPay) {
        this.noSecretPay = noSecretPay;
    }

    public Long getFee() {
        return fee;
    }

    public void setFee(Long fee) {
        this.fee = fee;
    }

    public Boolean getFingerprint() {
        return fingerprint;
    }

    public void setFingerprint(Boolean fingerprint) {
        this.fingerprint = fingerprint;
    }

    public boolean isAutoFee() {
        return isAutoFee;
    }

    public void setAutoFee(boolean autoFee) {
        isAutoFee = autoFee;
    }

    public Long getAutoMaxFee() {
        return autoMaxFee;
    }

    public void setAutoMaxFee(Long autoMaxFee) {
        this.autoMaxFee = autoMaxFee;
    }

    public String getVersionPay() {
        return versionPay;
    }

    public void setVersionPay(String versionPay) {
        this.versionPay = versionPay;
    }

    public static PaySetBean initPaySet() {
        return new PaySetBean("", "0",10000L, 10000L, false, false, false);
    }


}
