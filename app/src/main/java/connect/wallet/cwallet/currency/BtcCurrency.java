package connect.wallet.cwallet.currency;

import android.text.TextUtils;

import com.google.gson.Gson;

import java.text.DecimalFormat;
import java.util.ArrayList;

import connect.activity.home.bean.EstimatefeeBean;
import connect.activity.wallet.bean.SignRawBean;
import connect.database.SharedPreferenceUtil;
import connect.database.green.DaoHelper.CurrencyHelper;
import connect.database.green.bean.CurrencyEntity;
import connect.utils.UriUtil;
import connect.utils.cryption.DecryptionUtil;
import connect.utils.cryption.SupportKeyUril;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import connect.wallet.cwallet.NativeWallet;
import connect.wallet.cwallet.bean.CurrencyEnum;
import connect.wallet.cwallet.inter.WalletListener;
import connect.wallet.jni.AllNativeMethod;
import protos.Connect;
import wallet_gateway.WalletOuterClass;

/**
 * BTC 币种管理
 * Created by Administrator on 2017/7/18.
 */

public class BtcCurrency extends BaseCurrency {

    /** BTC (decimal turn o tLong) */
    public static final double BTC_TO_LONG = Math.pow(10,8);
    /** Bitcoin input format */
    public static final String PATTERN_BTC = "##0.00000000";

    @Override
    public void createAddress() {

    }

    @Override
    public void fee() {

    }

    @Override
    public CurrencyEntity getCurrencyData(){
        return CurrencyHelper.getInstance().loadCurrency(CurrencyEnum.BTC.getCode());
    }

    @Override
    public String ceaterAddress(String currencySeed){
        String pubKey = AllNativeMethod.cdGetPubKeyFromSeedBIP44(currencySeed,44,0,0,0,0);
        return AllNativeMethod.cdGetBTCAddrFromPubKey(pubKey);
    }

    @Override
    public String ceaterPriKey(String baseSeed, String salt,int index){
        String currencySeend = SupportKeyUril.xor(baseSeed, salt, 64);
        String priKey = AllNativeMethod.cdGetPrivKeyFromSeedBIP44(currencySeend,44,0,0,0,index);
        return priKey;
    }

    /**
     * 设置币种信息
     */
    @Override
    public void setCurrencyInfo(final CurrencyEntity currencyEntity, final WalletListener listener){
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
                listener.success(currencyEntity);
            }

            @Override
            public void onError(Connect.HttpResponse response) {
                listener.fail(WalletListener.WalletError.NETError);
            }
        });
    }

    @Override
    public void requestCoinInfo(final WalletListener listener) {
        WalletOuterClass.Coin coin = WalletOuterClass.Coin.newBuilder()
                .setCurrency(CurrencyEnum.BTC.getCode())
                .build();
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.WALLET_V2_COINS_INFO, coin, new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                try {
                    Connect.IMResponse imResponse = Connect.IMResponse.parseFrom(response.getBody().toByteArray());
                    Connect.StructData structData = DecryptionUtil.decodeAESGCMStructData(imResponse.getCipherData());
                    WalletOuterClass.CoinsDetail coinsDetail = WalletOuterClass.CoinsDetail.parseFrom(structData.getPlainData());
                    CurrencyHelper.getInstance().insertCurrencyCoin(coinsDetail.getCoin());
                    listener.success(coinsDetail.getCoin());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Connect.HttpResponse response) {
                listener.fail(WalletListener.WalletError.NETError);
            }
        });
    }

    /**
     * 签名交易
     * @param priList 签名交易的PriKey数组
     * @param tvs 所有输入地址的Unspent字符串集合
     * @param rowhex 原始交易
     * @return 签名交易
     */
    @Override
    public String getSignRawTrans(ArrayList<String> priList, String tvs, String rowhex) {
        String signTransfer = rowhex + " " +
                tvs + " " +
                new Gson().toJson(priList);
        String signRawTransfer = AllNativeMethod.cdSignRawTranscation(signTransfer);
        SignRawBean signRawBean = new Gson().fromJson(signRawTransfer, SignRawBean.class);
        if(signRawBean.isComplete()){
            return signRawBean.getHex();
        }else{
            return "";
        }
    }

    public long doubleToLongCurrency(double value){
        return Math.round(value * BTC_TO_LONG);
    }

    public String longToDoubleCurrency(long value) {
        DecimalFormat myformat = new DecimalFormat(PATTERN_BTC);
        String format = myformat.format(value / BTC_TO_LONG);
        return format.replace(",", ".");
    }

    /**
     * 判断金额是否肮脏
     */
    private static boolean isHaveDustWithAmount(long amount) {
        EstimatefeeBean feeBean = SharedPreferenceUtil.getInstance().getEstimatefee();
        if(feeBean == null || TextUtils.isEmpty(feeBean.getData())){
            return false;
        }else{
            return (amount * 1000 / (3 * 182)) < Double.valueOf(feeBean.getData()) * Math.pow(10, 8) / 10;
        }
    }

    /**
     * 自动计算手续费
     */
    private static long getAutoFeeWithUnspentLength(boolean isAddChangeAddress,int txs_length, int sentToLength) {
        if(isAddChangeAddress){ // the change of address
            sentToLength++;
        }
        EstimatefeeBean feeBean = SharedPreferenceUtil.getInstance().getEstimatefee();
        int txSize = 148 * txs_length + 34 * sentToLength + 10;
        double estimateFee = (txSize + 20 + 4 + 34 + 4) / 1000.0 * Double.valueOf(feeBean.getData());
        return Math.round(estimateFee * Math.pow(10, 8));
    }

}
