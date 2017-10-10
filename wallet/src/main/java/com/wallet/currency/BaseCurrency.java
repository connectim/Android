package com.wallet.currency;

import com.wallet.bean.CurrencyBean;

import java.util.ArrayList;
import java.util.List;

/**
 * 基础货币管理类
 */

public interface BaseCurrency {

    /** 纯私钥版本 */
    int CATEGORY_PRIKEY = 1;
    /** 原始种子版本 */
    int CATEGORY_BASESEED = 2;
    /** 货币种子版本 */
    int CATEGORY_CURRENCY = 3;

    /**
     * 创建货币
     *
     * @param type 原始种子
     * @param baseSeed 原始种子 私钥 货币种子
     * @return 货币信息(baseSeed currencySeed salt index masterAddress)
     */
    CurrencyBean createCurrency(int type, String baseSeed);

    /**
     * 增加货币的地址
     *
     * @param baseSeed 原始种子
     * @param salt 生成货币种子的盐
     * @param index 位数
     * @return 货币信息(baseSeed currencySeed salt index masterAddress)
     */
    CurrencyBean addCurrencyAddress(String baseSeed, String salt, int index);

    /**
     * 获取对应地址的私钥
     *
     * @param baseSeed 原始种子
     * @param salt 生成货币种子的盐
     * @param indexList 生成地址位数数组
     * @return 对应地址私钥数组
     */
    List<String> getPriKeyFromAddressIndex(String baseSeed, String salt, List<Integer> indexList);

    /**
     * 签名交易
     *
     * @param priList 私钥数组
     * @param tvs 输入字符串
     * @param rowHex 原始交易
     * @return 签名交易
     */
    String getSignRawTrans(ArrayList<String> priList, String tvs, String rowHex);

    /**
     * 广播交易
     *
     * @param rawString 签名交易
     * @return Tx用于查询交易详情
     */
    String broadcastTransfer(String rawString);



}
