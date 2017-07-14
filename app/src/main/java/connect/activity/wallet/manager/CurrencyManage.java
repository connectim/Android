package connect.activity.wallet.manager;

import android.app.Activity;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.google.protobuf.ByteString;

import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Unique;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import connect.activity.wallet.bean.WalletBean;
import connect.database.MemoryDataManager;
import connect.database.SharePreferenceUser;
import connect.database.green.DaoHelper.CurrencyHelper;
import connect.database.green.bean.CurrencyAddressEntity;
import connect.database.green.bean.CurrencyEntity;
import connect.utils.StringUtil;
import connect.utils.UriUtil;
import connect.utils.cryption.EncoPinBean;
import connect.utils.cryption.SupportKeyUril;
import connect.utils.okhttp.HttpRequest;
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
    private final int MASTER_ADDRESS_INDEX = 0;
    //(1:纯私钥，2:baseseed，3:salt+seed)
    public static final int WALLET_CATEGORY_PRI = 1;
    public static final int WALLET_CATEGORY_BASE = 2;
    public static final int WALLET_CATEGORY_SEED = 3;

    /**
     * 需要密码加密上传payLoad
     * @param category (1:纯私钥，2:baseseed，3:salt+seed)
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
     */
    public void createCurrencyBaseSeed(Activity activity, final CurrencyType currencyType, final OnCreateCurrencyListener onCurrencyListener){
        final WalletBean walletBean = SharePreferenceUser.getInstance().getWalletInfo();
        if(walletBean == null || TextUtils.isEmpty(walletBean.getPayload())){
            return;
        }
        new PinManager().showCheckPin(activity, walletBean.getPayload(), new PinManager.OnPinListener() {
            @Override
            public void success(String baseSeed) {
                createCurrencyBaseSeed(baseSeed, currencyType, onCurrencyListener);
            }
        });
    }

    public void createCurrencyBaseSeed(String baseSeed, final CurrencyType currencyType, final OnCreateCurrencyListener onCurrencyListener){
        String salt = AllNativeMethod.cdGetHash256(StringUtil.bytesToHexString(SecureRandom.getSeed(64)));
        String currencySeend = SupportKeyUril.xor(baseSeed, salt, 64);
        CurrencyAddressEntity addressEntity = createCurrencyAddress(currencyType,currencySeend);
        createCurrency("", salt, currencyType, WALLET_CATEGORY_BASE, addressEntity.getAddress(),onCurrencyListener);
    }

    /**
     * 不同币种生成不同的地址
     * @param currencyType
     * @param currencySeed
     * @return
     */
    private CurrencyAddressEntity createCurrencyAddress(CurrencyType currencyType,String currencySeed){
        // 获取对应币种的最大index
        CurrencyAddressEntity addressBean = new CurrencyAddressEntity();
        int index = MASTER_ADDRESS_INDEX;
        List<CurrencyAddressEntity> addressList = CurrencyHelper.getInstance().loadCurrencyAddress(currencyType.getCode());
        if(addressList != null && addressList.size() > 0){
            for(CurrencyAddressEntity addressEntity : addressList){
                if(index < addressEntity.getIndex()){
                    index = addressEntity.getIndex();
                }
            }
            index ++;
        }
        String address = "";
        // 不同币种生成不同的地址
        switch (currencyType){
            case BTC:
                // BIP44 生成对应的公私钥，地址
                String pubKey = AllNativeMethod.cdGetPubKeyFromSeedBIP44(currencySeed,44,0,0,0,index);
                address = AllNativeMethod.cdGetBTCAddrFromPubKey(pubKey);
                break;
            default:
                break;
        }
        addressBean.setAddress(address);
        addressBean.setIndex(index);
        return addressBean;
    }

    /**
     * 创建币种
     * @param category 1:纯私钥，2:baseSeed，3:salt+seed
     */
    public void createCurrency(final String payload, final String salt, final CurrencyType currencyType, final int category, final String masterAddress, final OnCreateCurrencyListener onCurrencyListener){
        WalletOuterClass.CreateCoinArgs.Builder builder = WalletOuterClass.CreateCoinArgs.newBuilder();
        builder.setSalt(salt);
        builder.setCurrency(currencyType.getCode());
        builder.setCategory(category);
        builder.setPayload(payload);
        builder.setMasterAddress(masterAddress);
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.WALLET_V2_COINS_CREATE, builder.build(), new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                CurrencyEntity currencyEntity = new CurrencyEntity();
                currencyEntity.setSalt(salt);
                currencyEntity.setBalance(0L);
                currencyEntity.setCurrency(currencyType.getCode());
                currencyEntity.setCategory(category);
                currencyEntity.setMasterAddress(masterAddress);
                currencyEntity.setPayload(payload);
                currencyEntity.setStatus(1);
                CurrencyHelper.getInstance().insertCurrency(currencyEntity);

                CurrencyAddressEntity addressEntity = new CurrencyAddressEntity();
                addressEntity.setBalance(0L);
                addressEntity.setStatus(1);
                addressEntity.setCurrency(currencyType.getCode());
                addressEntity.setAddress(masterAddress);
                addressEntity.setIndex(MASTER_ADDRESS_INDEX);
                addressEntity.setLabel("");
                CurrencyHelper.getInstance().insertCurrencyAddress(addressEntity);

                onCurrencyListener.success(currencyEntity);
            }

            @Override
            public void onError(Connect.HttpResponse response) {

            }
        });
    }

    /**
     * 设置币种信息
     */
    public void setCurrencyInfo(final CurrencyEntity currencyEntity, final OnCurrencyListener onCurrencyListener){
        WalletOuterClass.Coin.Builder builder = WalletOuterClass.Coin.newBuilder();
        builder.setSalt(currencyEntity.getSalt());
        builder.setCategory(currencyEntity.getCategory());
        builder.setPayload(currencyEntity.getPayload());
        builder.setCurrency(currencyEntity.getCurrency());
        builder.setBalance(currencyEntity.getBalance());
        builder.setStatus(currencyEntity.getStatus());
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.WALLET_V2_COINS_UPDATA, builder.build(), new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                CurrencyHelper.getInstance().updateCurrency(currencyEntity);
                onCurrencyListener.success();
            }

            @Override
            public void onError(Connect.HttpResponse response) {
                onCurrencyListener.fail(response.getMessage());
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