package connect.activity.wallet.manager;

import android.os.AsyncTask;

import com.google.protobuf.ByteString;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import connect.activity.wallet.bean.WalletBean;
import connect.database.SharePreferenceUser;
import connect.database.green.DaoHelper.CurrencyHelper;
import connect.database.green.bean.CurrencyEntity;
import connect.utils.StringUtil;
import connect.utils.cryption.SupportKeyUril;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import connect.wallet.jni.AllNativeMethod;

/**
 * 币种管理
 * Created by Administrator on 2017/7/11 0011.
 */

public class CurrencyManage {

    /**
     * 纯地址生成货币
     */
    public void createCurrencyPri(String address,final CurrencyType currencyType, final OnCreateCurrencyListener onCurrencyListener){
        createCurrency("",currencyType,3,address,onCurrencyListener);
    }

    /**
     * base种子生成货币
     */
    public void createCurrency(String baseSeed, final CurrencyType currencyType, final OnCreateCurrencyListener onCurrencyListener){
        String salt = AllNativeMethod.cdGetHash256(StringUtil.bytesToHexString(SecureRandom.getSeed(64)));
        String currencySeend = SupportKeyUril.xor(baseSeed, salt, 64);
        createCurrency(currencySeend,currencyType,2,"",onCurrencyListener);
    }

    /*###### 创建币种
    - uri: /wallet/v1/currency
    - method: post
    - args:
        * category(1:纯私钥，2:baseseed，3:salt+seed)
        * currency
        * salt
        * master_address*/
    /**
     * 创建币种
     * @param category 1:纯私钥，2:baseSeed，3:salt+seed
     */
    public void createCurrency(String currencySeend, final CurrencyType currencyType, int category, String masterAddress,final OnCreateCurrencyListener onCurrencyListener){
        switch (category){
            case 1://纯私钥
                break;
            case 2://baseSeed
                break;
            case 3://salt+seed
                break;
            default:
                break;
        }

        // 不同币种生成不同的地址
        switch (currencyType){
            case BTC:
                // BIP44 生成对应的公私钥，地址
                //AllNativeMethod.cdGetAccountMasterPubKeyFromSeedBIP44();
                break;
            default:
                break;
        }
        OkHttpUtil.getInstance().postEncrySelf("url", ByteString.copyFrom(new byte[]{}), new ResultCall() {
            @Override
            public void onResponse(Object response) {
                CurrencyHelper.getInstance().insertCurrency(new CurrencyEntity());
                onCurrencyListener.success(new CurrencyEntity());
            }

            @Override
            public void onError(Object response) {

            }
        });
    }

    /*# 获取币种列表
    - uri: /wallet/v1/currency/list
    - method: post
    - args:
        * wid
    - response
    - array coins
        * id
        * currency
        * balance
        * status(0: 隐藏 1:显示)*/
    private void requestCurrencyList(final OnCurrencyListListener onCurrencyListListener){
        WalletBean walletBean = SharePreferenceUser.getInstance().getWalletInfo();
        walletBean.getWid();
        OkHttpUtil.getInstance().postEncrySelf("url", ByteString.copyFrom(new byte[]{}), new ResultCall() {
            @Override
            public void onResponse(Object response) {
                CurrencyHelper.getInstance().insertCurrencyList(new ArrayList<CurrencyEntity>());
                onCurrencyListListener.success(new ArrayList<CurrencyEntity>());
            }

            @Override
            public void onError(Object response) {

            }
        });
    }

    /*###### 设置币种信息
    - uri: /wallet/v1/currency/set
    - method: post
    - args:
        * wid
        * currency
        * status*/
    private void setCurrencyInfo(CurrencyType currencyType, int status, final OnCurrencyListener onCurrencyListener){
        WalletBean walletBean = SharePreferenceUser.getInstance().getWalletInfo();
        walletBean.getWid();
        OkHttpUtil.getInstance().postEncrySelf("url", ByteString.copyFrom(new byte[]{}), new ResultCall() {
            @Override
            public void onResponse(Object response) {
                CurrencyHelper.getInstance().updateCurrency(new CurrencyEntity());
                onCurrencyListener.success();
            }

            @Override
            public void onError(Object response) {

            }
        });
    }

    public interface OnCurrencyListener {
        void success();

        void fail(String message);
    }

    public interface OnCreateCurrencyListener {
        void success(CurrencyEntity currencyEntity);

        void fail(String message);
    }

    public interface OnCurrencyListListener {
        void success(List<CurrencyEntity> list);

        void fail(String message);
    }

}
