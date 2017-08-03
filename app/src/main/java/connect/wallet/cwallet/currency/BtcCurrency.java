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
import connect.wallet.cwallet.bean.CurrencyEnum;
import connect.wallet.cwallet.inter.WalletListener;
import connect.wallet.jni.AllNativeMethod;
import protos.Connect;
import wallet_gateway.WalletOuterClass;

/**
 * BTC Currency management
 * Created by Administrator on 2017/7/18.
 */

public class BtcCurrency extends BaseCurrency {

    /** BTC (decimal turn o tLong) */
    public static final double BTC_TO_LONG = Math.pow(10,8);
    /** Bitcoin input format */
    public static final String PATTERN_BTC = "##0.00000000";

    @Override
    public void fee() {

    }

    @Override
    public CurrencyEntity getCurrencyData(){
        return CurrencyHelper.getInstance().loadCurrency(CurrencyEnum.BTC.getCode());
    }

    @Override
    public String createAddress(String currencySeed){
        String pubKey = AllNativeMethod.cdGetPubKeyFromSeedBIP44(currencySeed,44,0,0,0,0);
        return AllNativeMethod.cdGetBTCAddrFromPubKey(pubKey);
    }

    @Override
    public String createPriKey(String baseSeed, String salt,int index){
        String currencySeend = SupportKeyUril.xor(baseSeed, salt);
        String priKey = AllNativeMethod.cdGetPrivKeyFromSeedBIP44(currencySeend,44,0,0,0,index);
        return priKey;
    }

    /**
     * Set the currency information
     */
    @Override
    public void setCurrencyInfo(String payload, Integer status, final WalletListener listener){
        WalletOuterClass.Coin.Builder builder = WalletOuterClass.Coin.newBuilder();
        if(!TextUtils.isEmpty(payload)){
            builder.setPayload(payload);
        }
        if(status != null){
            builder.setStatus(status);
        }
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.WALLET_V2_COINS_UPDATA, builder.build(), new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                try {
                    Connect.IMResponse imResponse = Connect.IMResponse.parseFrom(response.getBody().toByteArray());
                    Connect.StructData structData = DecryptionUtil.decodeAESGCMStructData(imResponse.getCipherData());
                    WalletOuterClass.CoinsDetail coinsDetail = WalletOuterClass.CoinsDetail.parseFrom(structData.getPlainData());
                    WalletOuterClass.Coin coin = coinsDetail.getCoin();
                    CurrencyHelper.getInstance().insertCurrencyCoin(coin);
                    listener.success(coin);
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
     * Signature transaction
     * @param priList PriKey array
     * @param tvs input string
     * @param rowhex The original transaction
     * @return
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
     * Determine whether the amount is dirty
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
     * Automatic calculation fee
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
