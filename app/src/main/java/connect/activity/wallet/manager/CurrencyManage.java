package connect.activity.wallet.manager;

import android.app.Activity;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.google.protobuf.ByteString;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import connect.activity.wallet.bean.WalletBean;
import connect.database.MemoryDataManager;
import connect.database.SharePreferenceUser;
import connect.database.green.DaoHelper.CurrencyHelper;
import connect.database.green.bean.CurrencyEntity;
import connect.utils.StringUtil;
import connect.utils.UriUtil;
import connect.utils.cryption.EncoPinBean;
import connect.utils.cryption.SupportKeyUril;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import connect.wallet.jni.AllNativeMethod;
import protos.Connect;
import wallet_gateway.WalletOuterClass;

/**
 * 币种管理
 * Created by Administrator on 2017/7/11 0011.
 */

public class CurrencyManage {

    public static CurrencyType CURRENCY_DEFAULT = CurrencyType.BTC;
    //(1:纯私钥，2:baseseed，3:salt+seed)
    public static final int WALLET_CATEGORY_PRI = 1;
    public static final int WALLET_CATEGORY_BASE = 2;
    public static final int WALLET_CATEGORY_SEED = 3;

    /**
     * 需要密码加密上传payLoad
     * @param activity
     * @param value
     * @param address
     * @param currencyType
     * @param category (1:纯私钥，2:baseseed，3:salt+seed)
     * @param onCurrencyListener
     */
    public void createCurrencyPin(Activity activity, final String value, final String address, final CurrencyType currencyType,
                                  final int category, final OnCreateCurrencyListener onCurrencyListener){
        new PinManager().showSetNewPin(activity, new PinManager.OnPinListener() {
            @Override
            public void success(final String pass) {
                EncoPinBean encoPinBean = SupportKeyUril.encoPinDefult(value,pass);
                createCurrency(encoPinBean.getPayload(),"",currencyType,category,address,onCurrencyListener);
            }
        });
    }

    /**
     * 种子的方式生成币种
     * (1:纯私钥，2:baseseed，3:salt+seed)
     * @param activity
     * @param currencyType
     * @param onCurrencyListener
     */
    public void createCurrencyBaseSeed(Activity activity, final CurrencyType currencyType, final OnCreateCurrencyListener onCurrencyListener){
        final WalletBean walletBean = SharePreferenceUser.getInstance().getWalletInfo();
        if(walletBean == null || TextUtils.isEmpty(walletBean.getPayload())){
            return;
        }
        new PinManager().showCheckPin(activity, new PinManager.OnPinListener() {
            @Override
            public void success(String baseSeed) {
                createCurrencyBaseSeed(baseSeed, currencyType, onCurrencyListener);
            }
        });
    }

    public void createCurrencyBaseSeed(String baseSeed, final CurrencyType currencyType, final OnCreateCurrencyListener onCurrencyListener){
        String salt = AllNativeMethod.cdGetHash256(StringUtil.bytesToHexString(SecureRandom.getSeed(64)));
        String currencySeend = SupportKeyUril.xor(baseSeed, salt, 64);
        String address = cdMasterAddress(currencyType,currencySeend);
        createCurrency("", salt, currencyType, WALLET_CATEGORY_BASE, address,onCurrencyListener);
    }

    private String cdMasterAddress(CurrencyType currencyType,String currencySeend){
        // 不同币种生成不同的地址
        String address = "";
        switch (currencyType){
            case BTC:
                // BIP44 生成对应的公私钥，地址
                String pubKey = AllNativeMethod.cdGetPubKeyFromSeedBIP44(currencySeend,44,0,0,0,0);
                address = AllNativeMethod.cdGetBTCAddrFromPubKey(pubKey);
                break;
            default:
                break;
        }
        return address;
    }

    /*###### 创建币种
    - uri: /wallet/v1/currency
    - method: post
    - args:
        * category(1:纯私钥，2:baseseed，3:salt+seed)
        * currency
        * salt
        * master_address
        * payload*/
    /**
     * 创建币种
     * @param category 1:纯私钥，2:baseSeed，3:salt+seed
     */
    public void createCurrency(String payload, final String salt, final CurrencyType currencyType, final int category, String masterAddress, final OnCreateCurrencyListener onCurrencyListener){
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
        /*message CreateCoinArgs {
        int32 category =1;
        int32 currency = 2;
        string salt = 3;
        string master_address = 4;
        string payload = 5;
        string w_id =6;
    }
    */

        WalletOuterClass.CreateCoinArgs.Builder builder = WalletOuterClass.CreateCoinArgs.newBuilder();
        builder.setSalt(salt);
        builder.setCurrency(currencyType.getCode());
        builder.setCategory(category);
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.WALLET_V2_COINS_CREATE, builder.build(), new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                CurrencyEntity currencyEntity = new CurrencyEntity();
                currencyEntity.setSalt(salt);
                currencyEntity.setBalance(0L);
                currencyEntity.setCurrency(currencyType.getCode());
                currencyEntity.setCategory(category);
                CurrencyHelper.getInstance().insertCurrency(currencyEntity);
                onCurrencyListener.success(currencyEntity);
            }

            @Override
            public void onError(Connect.HttpResponse response) {
                int a = 1;
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
