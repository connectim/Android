package connect.utils.transfer;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import connect.db.SharedPreferenceUtil;
import connect.db.green.DaoHelper.ParamManager;
import connect.ui.activity.R;
import connect.ui.activity.home.bean.EstimatefeeBean;
import connect.ui.activity.set.bean.PaySetBean;
import connect.ui.activity.wallet.bean.SignRawBean;
import connect.ui.activity.wallet.bean.TranAddressBean;
import connect.utils.DialogUtil;
import connect.utils.ProtoBufUtil;
import connect.utils.data.RateFormatUtil;
import connect.utils.ToastEUtil;
import connect.utils.ToastUtil;
import connect.utils.UriUtil;
import connect.utils.okhttp.HttpRequest;
import connect.utils.okhttp.ResultCall;
import connect.wallet.jni.AllNativeMethod;
import protos.Connect;

/**
 * Created by Administrator on 2016/12/18.
 */
public class TransferUtil {

    private Activity activity;
    private OnResultCall onResultCall;
    /** transfer money to the middle of the address (Y: need to add a fee) (external red envelopes, external transfers) */
    private boolean isPendding;
    private PaySetBean paySetBean;
    private String address;
    private List<TranAddressBean> outputList;
    private boolean isAddChangeAddress;
    private Dialog connectDialog;

    /**
     * Automatic calculation fee
     */
    public static long getAutoFeeWithUnspentLength(boolean isAddChangeAddress,int txs_length, int sentToLength) {
        if(isAddChangeAddress){ // the change of address
            sentToLength++;
        }
        EstimatefeeBean feeBean = SharedPreferenceUtil.getInstance().getEstimatefee();
        int txSize = 148 * txs_length + 34 * sentToLength + 10;
        double estimateFee = (txSize + 20 + 4 + 34 + 4) / 1000.0 * Double.valueOf(feeBean.getData());
        return Math.round(estimateFee * Math.pow(10, 8));
    }

    /**
     * whether is dusty transaction
     */
    public static boolean ishaveDustWithAmount(long amount) {
        EstimatefeeBean feeBean = SharedPreferenceUtil.getInstance().getEstimatefee();
        if(feeBean == null || TextUtils.isEmpty(feeBean.getData())){
            return false;
        }else{
            return (amount * 1000 / (3 * 182)) < Double.valueOf(feeBean.getData()) * Math.pow(10, 8) / 10;
        }
    }

    /**
     * Transfer front safety judgment
     */
    public static boolean checkTransfer(Context context,long avaliableAmount, long amount) {
        long fee = ParamManager.getInstance().getPaySet().getFee();
        //Check account balance
        if (avaliableAmount < amount + fee) {
            ToastEUtil.makeText(context,R.string.Wallet_Insufficient_balance,ToastEUtil.TOAST_STATUS_FAILE).show();
            return false;
        }

        if (TransferUtil.ishaveDustWithAmount(amount)) {
            ToastEUtil.makeText(context,R.string.Wallet_Amount_is_too_small,ToastEUtil.TOAST_STATUS_FAILE).show();
            return false;
        }
        return true;
    }

    /**
     * Request transfer transaction(single address)
     */
    public void getOutputTran(final Activity activity, final String address, boolean isPendding,
                              final String outAddress, long avaliableAmount, long amount, OnResultCall onResultCall) {
        ArrayList<TranAddressBean> arrayList = new ArrayList<>();
        arrayList.add(new TranAddressBean(outAddress, amount));
        getOutputTran(activity,address,isPendding,arrayList,avaliableAmount,amount,onResultCall);
    }

    /**
     * Request transfer transaction(more address)
     */
    public void getOutputTran(final Activity activity, final String address, boolean isPendding,
                              final List<TranAddressBean> outputList, long avaliableAmount, long amount, OnResultCall onResultCall) {
        if (!TransferUtil.checkTransfer(activity,avaliableAmount,amount)){
            return;
        }
        this.onResultCall = onResultCall;
        this.isPendding = isPendding;
        this.address = address;
        this.outputList = outputList;
        this.activity = activity;
        paySetBean = ParamManager.getInstance().getPaySet();
        connectDialog = DialogUtil.showConnectPay(activity);
        getTranUnspent();
    }

    private void getTranUnspent() {
        Connect.UnspentOrder.Builder builder = Connect.UnspentOrder.newBuilder();
        builder.setSendToLength(outputList.size());

        long amount = 0;
        for (TranAddressBean tranAddressBean : outputList) {
            amount = amount + tranAddressBean.getAmount();
        }

        // If you have intermediate transfer address, set up the second fee
        if (isPendding) {
            if (paySetBean.isAutoFee()){
                long autoFee = getAutoFeeWithUnspentLength(true, 1, outputList.size());
                if(autoFee >= paySetBean.getAutoMaxFee()){
                    builder.setAmount(amount + paySetBean.getAutoMaxFee());
                }else{
                    builder.setAmount(amount + autoFee);
                }
            }else{
                builder.setAmount(amount + paySetBean.getFee());
            }
        } else {
            builder.setAmount(amount);
        }

        // Setting up the commission parameters
        if (paySetBean.isAutoFee()) {
            builder.setFee(0);
        }else{
            builder.setFee(paySetBean.getFee());
        }
        String url = String.format(UriUtil.BLOCKCHAIN_UNSPENT_OEDER, address);
        HttpRequest.getInstance().post(url, builder.build(), new ResultCall<Connect.HttpNotSignResponse>() {
            @Override
            public void onResponse(Connect.HttpNotSignResponse response) {
                try {
                    Connect.UnspentOrderResponse orderResponse = Connect.UnspentOrderResponse.parseFrom(response.getBody());
                    if(ProtoBufUtil.getInstance().checkProtoBuf(orderResponse)){
                        checkUnspend(orderResponse);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Connect.HttpNotSignResponse response) {

            }
        });
    }

    private void checkUnspend(final Connect.UnspentOrderResponse orderResponse) {
        if (!orderResponse.getCompleted()) {
            ToastEUtil.makeText(activity,R.string.Wallet_Insufficient_balance,ToastEUtil.TOAST_STATUS_FAILE).show();
            connectDialog.dismiss();
            return;
        }

        if (!orderResponse.getPackage()) {
            ToastEUtil.makeText(activity,R.string.Wallet_Too_much_transaction_can_not_generated,ToastEUtil.TOAST_STATUS_FAILE).show();
            connectDialog.dismiss();
            return;
        }
        checkFee(orderResponse);
    }

    /**
     * Check the handling charge
     * @param orderResponse
     */
    private void checkFee(final Connect.UnspentOrderResponse orderResponse) {
        if (paySetBean.isAutoFee()) {
            if (orderResponse.getFee() > paySetBean.getAutoMaxFee()) {
                DialogUtil.showAlertTextView(activity, activity.getString(R.string.Set_tip_title),
                        activity.getString(R.string.Wallet_Auto_fees_is_greater_than_the_maximum_set_maximum_and_continue,
                                RateFormatUtil.longToDoubleBtc(orderResponse.getFee())),
                        "", "", false, new DialogUtil.OnItemClickListener() {
                            @Override
                            public void confirm(String value) {
                                Connect.UnspentOrderResponse response = orderResponse.toBuilder().setFee(paySetBean.getAutoMaxFee()).build();
                                checkDust(response);
                            }

                            @Override
                            public void cancel() {
                                connectDialog.dismiss();
                            }
                        });
            } else {
                checkDust(orderResponse);
            }
        } else {
            long autoFee = getAutoFeeWithUnspentLength(isAddChangeAddress, orderResponse.getUnspentsList().size(), outputList.size());
            if (autoFee > orderResponse.getFee() || orderResponse.getDust()) {
                DialogUtil.showAlertTextView(activity, activity.getString(R.string.Set_tip_title),
                        activity.getString(R.string.Wallet_Transaction_fee_too_low_Continue),
                        "", "", false, new DialogUtil.OnItemClickListener() {
                            @Override
                            public void confirm(String value) {
                                checkDust(orderResponse);
                            }

                            @Override
                            public void cancel() {
                                connectDialog.dismiss();
                            }
                        });
            } else {
                checkDust(orderResponse);
            }
        }
    }

    /**
     * Check the change
     * @param orderResponse
     */
    private void checkDust(final Connect.UnspentOrderResponse orderResponse){
        isAddChangeAddress = true;
        long change = orderResponse.getUnspentAmount() - (orderResponse.getAmount() + orderResponse.getFee());
        if (change < 0) {
            ToastUtil.getInstance().showToast(R.string.Wallet_Insufficient_balance);
            connectDialog.dismiss();
            return;
        }
        if(change == 0){
            isAddChangeAddress = false;
            getArrayValue(orderResponse.getFee(), orderResponse.getAmount(), orderResponse.getUnspentAmount(), orderResponse.getUnspentsList());
            return;
        }
        if (ishaveDustWithAmount(change)) {
            DialogUtil.showAlertTextView(activity, activity.getResources().getString(R.string.Set_tip_title),
                    activity.getString(R.string.Wallet_Charge_small_calculate_to_the_poundage, RateFormatUtil.longToDoubleBtc(change)),
                    "", "", false, new DialogUtil.OnItemClickListener() {
                        @Override
                        public void confirm(String value) {
                            isAddChangeAddress = false;
                            getArrayValue(orderResponse.getFee(), orderResponse.getAmount(), orderResponse.getUnspentAmount(), orderResponse.getUnspentsList());
                        }

                        @Override
                        public void cancel() {
                            connectDialog.dismiss();
                        }
                    });
        } else {
            getArrayValue(orderResponse.getFee(), orderResponse.getAmount(), orderResponse.getUnspentAmount(), orderResponse.getUnspentsList());
        }
    }

    /**
     * Assembly input and output transactions
     */
    private void getArrayValue(long fee, long amount,long UnspentAmount, List<Connect.Unspent> list) {
        ArrayList inputArray = new ArrayList<>();
        for (Connect.Unspent unspent : list) {
            HashMap hashMap = new HashMap<>();
            hashMap.put("vout", unspent.getTxOutputN());
            hashMap.put("txid", unspent.getTxHash());
            hashMap.put("scriptPubKey", unspent.getScriptpubkey());
            inputArray.add(hashMap);
        }

        HashMap<String, Double> outputMap = new HashMap<>();
        //Determine whether the change
        long change = UnspentAmount - (amount + fee);
        if(change < 0){
            return;
        }else if(change == 0){

        }else if(change > 0){
            if(ishaveDustWithAmount(change)){
                if(isAddChangeAddress){
                    return;
                }else{

                }
            }else{
                outputMap.put(address, Double.valueOf(RateFormatUtil.longToDoubleBtc(change)));
            }
        }

        if(isPendding){
            for (TranAddressBean tranAddressBean : outputList) {
                outputMap.put(tranAddressBean.getAddress(), Double.valueOf(RateFormatUtil.longToDoubleBtc(amount)));
            }
        }else{
            for (TranAddressBean tranAddressBean : outputList) {
                if(outputMap.containsKey(tranAddressBean.getAddress())){
                    Double value = outputMap.get(tranAddressBean.getAddress());
                    outputMap.remove(tranAddressBean.getAddress());
                    outputMap.put(tranAddressBean.getAddress(),value + Double.valueOf(RateFormatUtil.longToDoubleBtc(tranAddressBean.getAmount())));
                }else{
                    outputMap.put(tranAddressBean.getAddress(), Double.valueOf(RateFormatUtil.longToDoubleBtc(tranAddressBean.getAmount())));
                }
            }
        }
        final String inputStrings = new Gson().toJson(inputArray);
        final String outputStrings = new Gson().toJson(outputMap);
        connectDialog.dismiss();
        onResultCall.result(inputStrings, outputStrings);
    }

    /**
     * Deal with the private key signature
     */
    public String getSignRawTrans(String priKey, String inputStrings, String outputStrings) {
        String rawTranscation = AllNativeMethod.cdCreateRawTranscation(inputStrings + " " + outputStrings);
        ArrayList arrayList = new ArrayList<>();
        arrayList.add(priKey);
        String aa = rawTranscation + " " +
                inputStrings + " " +
                new Gson().toJson(arrayList);
        String signRawTranscation = AllNativeMethod.cdSignRawTranscation(aa);
        SignRawBean signRawBean = new Gson().fromJson(signRawTranscation, SignRawBean.class);
        return signRawBean.getHex();
    }

    public interface OnResultCall {

        void result(String inputString, String outputString);

    }

}
