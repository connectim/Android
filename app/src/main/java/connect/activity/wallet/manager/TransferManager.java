package connect.activity.wallet.manager;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.protobuf.ByteString;

import java.util.ArrayList;

import connect.activity.home.bean.EstimatefeeBean;
import connect.activity.wallet.bean.SignRawBean;
import connect.activity.wallet.bean.WalletBean;
import connect.database.SharePreferenceUser;
import connect.database.SharedPreferenceUtil;
import connect.database.green.DaoHelper.CurrencyHelper;
import connect.database.green.DaoHelper.ParamManager;
import connect.database.green.bean.CurrencyEntity;
import connect.ui.activity.R;
import connect.utils.ToastEUtil;
import connect.utils.cryption.SupportKeyUril;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import connect.wallet.jni.AllNativeMethod;
import connect.widget.MdStyleProgress;
import connect.widget.payment.PaymentPwd;
import connect.widget.payment.PinTransferDialog;
import wallet_gateway.WalletOuterClass;

/**
 * Created by Administrator on 2017/7/12 0012.
 */

public class TransferManager {


    private PinTransferDialog pinTransferDialog;

    private OnResultCall onResultCall;
    private CurrencyType currencyType;
    private ArrayList<Integer> indexList;

    /**
     * 前置判断
     */
    public boolean baseCheckTransfer(Context context, Long outAmount) {
        Long avaAmount = 0L;
        // 判断余额
        long fee = ParamManager.getInstance().getPaySet().getFee();
        if (avaAmount < outAmount + fee) {
            ToastEUtil.makeText(context, R.string.Wallet_Insufficient_balance,ToastEUtil.TOAST_STATUS_FAILE).show();
            return false;
        }

        // 判断输出金额是否肮脏
        if (ishaveDustWithAmount(outAmount)) {
            ToastEUtil.makeText(context,R.string.Wallet_Amount_is_too_small,ToastEUtil.TOAST_STATUS_FAILE).show();
            return false;
        }
        return true;
    }

    /*###### 普通打款
    - uri: /wallet/v1/send
    - method: post
    - args:
        * to(数组:{address:amount,  address:amount,})
        * fee
        * currency
        * from:(数组：{address, address})
    - response
        * rowhex
        * tvs*/
    public void getTranferRow(final Activity activity, ArrayList<String> formList, ArrayList<Integer> indexList, ArrayList<String> toList,
                              CurrencyType currencyType, OnResultCall onResultCall){
        this.onResultCall = onResultCall;
        this.currencyType = currencyType;
        this.indexList = indexList;
        baseCheckTransfer(activity,0L);

        OkHttpUtil.getInstance().postEncrySelf("url", ByteString.copyFrom(new byte[]{}), new ResultCall() {
            @Override
            public void onResponse(Object response) {
                checkPin(activity, "rowhex", "tvs", "url");
            }

            @Override
            public void onError(Object response) {

            }
        });
    }

    /*###### 红包转账
    - uri: /wallet/v1/redPack/send
    - method: post
    - args:
        * total(个数)
        * fee（*2）
        * amount(金额)
        * type（等额或者随机，默认为0）
        * currency
        * category（外部红或者内部红包）
        * from:(数组：{address, address})
    - response
        * rowhex
        * tvs
        * url (外部红包的url)*/
    public void sendRedPack(final Activity activity, ArrayList<String> fromList, ArrayList<Integer> indexList, int total, Long amount, int category,
                            CurrencyType currencyType, OnResultCall onResultCall){
        this.onResultCall = onResultCall;
        this.currencyType = currencyType;
        this.indexList = indexList;
        baseCheckTransfer(activity,amount);

        OkHttpUtil.getInstance().postEncrySelf("url", ByteString.copyFrom(new byte[]{}), new ResultCall() {
            @Override
            public void onResponse(Object response) {
                checkPin(activity, "rowhex", "tvs", "url");
            }

            @Override
            public void onError(Object response) {

            }
        });
    }

    /*###### 外部转账
    - uri: /wallet/v1/outTransfer/send
    - method: post
    - args:
        * amount(金额)
        * fee（*2）
        * currency
        * from:(数组：{address, address})s
    - response
        * rowhex
        * tvs*/
    private void seedOutTransfer(final Activity activity, ArrayList<String> fromList, ArrayList<Integer> indexList, Long amount,
                                 CurrencyType currencyType, OnResultCall onResultCall){
        this.onResultCall = onResultCall;
        this.currencyType = currencyType;
        this.indexList = indexList;
        baseCheckTransfer(activity,amount);

        OkHttpUtil.getInstance().postEncrySelf("url", ByteString.copyFrom(new byte[]{}), new ResultCall() {
            @Override
            public void onResponse(Object response) {
                checkPin(activity, "rowhex", "tvs", "url");
            }

            @Override
            public void onError(Object response) {

            }
        });
    }


    /**
     * 检查交易密码
     */
    private void checkPin(Activity activity, final String rowhex, final String tvs, final String url){
        pinTransferDialog = new PinTransferDialog();
        String payload = "";
        final CurrencyEntity currencyEntity = CurrencyHelper.getInstance().loadCurrency(currencyType.getCode());
        if(currencyEntity.getCategory() == CurrencyManage.WALLET_CATEGORY_PRI){
            payload = currencyEntity.getPayload();
        }else if(currencyEntity.getCategory() == CurrencyManage.WALLET_CATEGORY_BASE){
            WalletBean walletBean = SharePreferenceUser.getInstance().getWalletInfo();
            payload = walletBean.getPayload();
        }

        pinTransferDialog.showPaymentPwd(activity, payload, new PaymentPwd.OnTrueListener() {
            @Override
            public void onTrue(String decodeStr) {
                String priKey = "";
                if(currencyEntity.getCategory() == CurrencyManage.WALLET_CATEGORY_PRI){
                    // 纯私钥
                    priKey = decodeStr;
                }else if(currencyEntity.getCategory() == CurrencyManage.WALLET_CATEGORY_BASE){
                    // 原始种子，生成货币种子，再生成对应的私钥
                    CurrencyEntity currencyEntity = CurrencyHelper.getInstance().loadCurrency(currencyType.getCode());
                    String currencySeend = SupportKeyUril.xor(decodeStr, currencyEntity.getSalt(), 64);
                    priKey = AllNativeMethod.cdGetPrivKeyFromSeedBIP44(currencySeend,44,0,0,0,indexList.get(0));
                }
                String rawHex = getSignRawTrans(priKey, tvs, rowhex);
                publishTransfer(rawHex, url);
            }
        });
    }


    /*###### 广播交易
    - uri: /wallet/v1/publish
    - method: post
    - args:
        * rawHex
    - response*/
    private void publishTransfer(String rawHex, final String value){
        OkHttpUtil.getInstance().postEncrySelf("url", ByteString.copyFrom(new byte[]{}), new ResultCall() {
            @Override
            public void onResponse(Object response) {
                pinTransferDialog.closeStatusDialog(MdStyleProgress.Status.LoadSuccess, new PaymentPwd.OnAnimationListener() {
                    @Override
                    public void onComplete() {
                        onResultCall.result(value);
                    }
                });
            }

            @Override
            public void onError(Object response) {
                pinTransferDialog.closeStatusDialog(MdStyleProgress.Status.LoadFail);
            }
        });
    }


    /**
     * 签名交易
     */
    private String getSignRawTrans(String priKey, String tvs, String rowhex) {
        ArrayList arrayList = new ArrayList<>();
        arrayList.add(priKey);
        String signTransfer = rowhex + " " + tvs + " " + new Gson().toJson(arrayList);
        String signRawTransfer = AllNativeMethod.cdSignRawTranscation(signTransfer);
        SignRawBean signRawBean = new Gson().fromJson(signRawTransfer, SignRawBean.class);
        return signRawBean.getHex();
    }


    /**
     * 判断金额是否肮脏
     */
    private static boolean ishaveDustWithAmount(long amount) {
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

    public interface OnResultCall {
        void result(String value);
    }

}
