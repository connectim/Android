package com.wallet.bean;

public class CurrencyBean {

    int type;
    String baseSeed;
    String currencySeed;
    String salt;
    String priKey;
    int index;
    String masterAddress;

    public CurrencyBean() {}

    public CurrencyBean(int type,String baseSeed, String currencySeed, String salt, String priKey,int index, String masterAddress) {
        this.type = type;
        this.baseSeed = baseSeed;
        this.currencySeed = currencySeed;
        this.salt = salt;
        this.priKey = priKey;
        this.index = index;
        this.masterAddress = masterAddress;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getBaseSeed() {
        return baseSeed;
    }

    public void setBaseSeed(String baseSeed) {
        this.baseSeed = baseSeed;
    }

    public String getCurrencySeed() {
        return currencySeed;
    }

    public void setCurrencySeed(String currencySeed) {
        this.currencySeed = currencySeed;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public String getPriKey() {
        return priKey;
    }

    public void setPriKey(String priKey) {
        this.priKey = priKey;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getMasterAddress() {
        return masterAddress;
    }

    public void setMasterAddress(String masterAddress) {
        this.masterAddress = masterAddress;
    }
}
