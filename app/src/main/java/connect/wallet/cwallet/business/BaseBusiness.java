package connect.wallet.cwallet.business;

import android.app.Activity;
import android.app.Dialog;
import android.text.TextUtils;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import connect.activity.base.BaseApplication;
import connect.activity.set.bean.PaySetBean;
import connect.activity.wallet.bean.WalletBean;
import connect.database.SharePreferenceUser;
import connect.database.green.DaoHelper.CurrencyHelper;
import connect.database.green.DaoHelper.ParamManager;
import connect.database.green.bean.CurrencyAddressEntity;
import connect.database.green.bean.CurrencyEntity;
import connect.ui.activity.R;
import connect.utils.DialogUtil;
import connect.utils.ProtoBufUtil;
import connect.utils.StringUtil;
import connect.utils.ToastEUtil;
import connect.utils.UriUtil;
import connect.utils.cryption.DecryptionUtil;
import connect.utils.cryption.SupportKeyUril;
import connect.utils.data.RateFormatUtil;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import connect.wallet.cwallet.NativeWallet;
import connect.wallet.cwallet.bean.CurrencyEnum;
import connect.wallet.cwallet.currency.BaseCurrency;
import connect.wallet.cwallet.inter.WalletListener;
import connect.widget.MdStyleProgress;
import connect.widget.payment.PaymentPwd;
import connect.widget.payment.PinTransferDialog;
import protos.Connect;
import wallet_gateway.WalletOuterClass;

/**
 * Specific business layer interface
 * Created by Administrator on 2017/7/18.
 * Connect the transfer business management
 */
public class BaseBusiness {

    private final Activity mActivity;
    private PinTransferDialog pinTransferDialog;
    private Dialog connectDialog;
    private CurrencyEnum currencyEnum;
    private TransferType transactionType;

    public BaseBusiness(Activity mActivity, CurrencyEnum currencyEnum) {
        this.mActivity = mActivity;
        this.currencyEnum = currencyEnum;
    }

    /**
<<<<<<< HEAD
     * Assembly input Txin
=======
     * Assemble input Txin
     *
>>>>>>> f02794c34f11533204f3e74bd6e7273a261dbf96
     * @param listAddress
     * @return
     */
    private WalletOuterClass.Txin getTxin(ArrayList<String> listAddress){
        if(listAddress != null && listAddress.size() > 0){
            WalletOuterClass.Txin.Builder txin = WalletOuterClass.Txin.newBuilder();
            for(String address : listAddress){
                txin.addAddresses(address);
            }
            return txin.build();
        }
        return null;
    }

    /**
<<<<<<< HEAD
     * Assembly output Txouts
=======
     * Assemble the output Txouts
     *
>>>>>>> f02794c34f11533204f3e74bd6e7273a261dbf96
     * @param outMap
     * @return
     */
    private ArrayList<WalletOuterClass.Txout> getTxOut(HashMap<String,Long> outMap){
        ArrayList<WalletOuterClass.Txout> list = new ArrayList<>();
        for (HashMap.Entry<String, Long> entry : outMap.entrySet()) {
            System.out.println("key= " + entry.getKey() + " and value= " + entry.getValue());
            WalletOuterClass.Txout txout = WalletOuterClass.Txout.newBuilder()
                    .setAddress(entry.getKey())
                    .setAmount(entry.getValue())
                    .build();
            list.add(txout);
        }
        return list;
    }

    /**
<<<<<<< HEAD
     * @return
=======
     * Get a fee
     *
     * @return fee
>>>>>>> f02794c34f11533204f3e74bd6e7273a261dbf96
     */
    private Long getFee(){
        Long fee = 0L;
        PaySetBean paySetBean = ParamManager.getInstance().getPaySet();
        if(!paySetBean.isAutoFee()){
            fee = paySetBean.getFee();
        }
        return fee;
    }

    /**
     * Address transfer
<<<<<<< HEAD
=======
     *
>>>>>>> f02794c34f11533204f3e74bd6e7273a261dbf96
     * @param outMap <address,amount>
     * @param listener
     */
    public void transferAddress(ArrayList<String> listAddress, HashMap<String,Long> outMap, final WalletListener<String> listener){
        transactionType = TransferType.TransactionTypeBill;
        connectDialog = DialogUtil.showConnectPay(mActivity);
        WalletOuterClass.TransferRequest.Builder builder = WalletOuterClass.TransferRequest.newBuilder();
        WalletOuterClass.SpentCurrency.Builder builderSend = WalletOuterClass.SpentCurrency.newBuilder();

        WalletOuterClass.Txin txin = getTxin(listAddress);
        if(getTxin(listAddress) != null){
            builderSend.setTxin(txin);
        }
        builderSend.setCurrency(currencyEnum.getCode());
        builder.setSpentCurrency(builderSend.build());

        ArrayList<WalletOuterClass.Txout> txoutList = getTxOut(outMap);
        for(WalletOuterClass.Txout txout : txoutList){
            builder.addTxOut(txout);
        }

        builder.setFee(getFee());
        builder.setTips("");
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.WALLET_V2_SERVICE_TRANSFER_ADDRESS, builder.build(), new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                dealTransferResult(mActivity, response, listener);
            }

            @Override
            public void onError(Connect.HttpResponse response) {
                showErrorToast(response);
            }
        });
    }

    /**
     * Connect user transfer
     * @param outMap <pubKey,amount>
     * @param listener
     */
    public void transferConnectUser(ArrayList<String> listAddress, HashMap<String,Long> outMap, final WalletListener<String> listener){
        transactionType = TransferType.TransactionTypeBill;
        connectDialog = DialogUtil.showConnectPay(mActivity);
        WalletOuterClass.ConnectTransferRequest.Builder builder = WalletOuterClass.ConnectTransferRequest.newBuilder();
        WalletOuterClass.SpentCurrency.Builder builderSend = WalletOuterClass.SpentCurrency.newBuilder();
        WalletOuterClass.Txin txin = getTxin(listAddress);
        if(getTxin(listAddress) != null){
            builderSend.setTxin(txin);
        }
        builderSend.setCurrency(currencyEnum.getCode());
        builder.setSpentCurrency(builderSend.build());
        for (HashMap.Entry<String, Long> entry : outMap.entrySet()) {
            WalletOuterClass.ConnectTxout txout = WalletOuterClass.ConnectTxout.newBuilder()
                    .setUid(entry.getKey())
                    .setAmount(entry.getValue())
                    .build();
            builder.addTxOut(txout);
        }
        builder.setFee(getFee());
        builder.setTips("");
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.WALLET_V2_SERVICE_TRANSFER, builder.build(), new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                dealTransferResult(mActivity, response, listener);
            }

            @Override
            public void onError(Connect.HttpResponse response) {
                showErrorToast(response);
            }
        });
    }

    /**
     * Individual collection
     * Single payment
     *
     * @param amount
     * @param senderaddress
     * @param tips
     * @param listener
     */
    public void friendReceiver(long amount, String senderaddress, String tips, final WalletListener listener) {
        WalletOuterClass.ReceiveRequest receiveRequest = WalletOuterClass.ReceiveRequest.newBuilder()
                .setCurrency(currencyEnum.getCode())
                .setAmount(amount)
                .setSender(senderaddress)
                .setTips(tips).build();

        OkHttpUtil.getInstance().postEncrySelf(UriUtil.WALLET_V2_SERVICE_RECEIVE, receiveRequest, new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                try {
                    Connect.IMResponse imResponse = Connect.IMResponse.parseFrom(response.getBody().toByteArray());
                    //check sign
                    if (!SupportKeyUril.verifySign(imResponse.getSign(), imResponse.getCipherData().toByteArray())) {
                        throw new Exception("Validation fails");
                    }

                    Connect.StructData structData = DecryptionUtil.decodeAESGCMStructData(imResponse.getCipherData());
                    Connect.Bill bill = Connect.Bill.parseFrom(structData.getPlainData());
                    if (ProtoBufUtil.getInstance().checkProtoBuf(bill)) {
                        listener.success(bill);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Connect.HttpResponse response) {
                Toast.makeText(BaseApplication.getInstance().getAppContext(),response.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
<<<<<<< HEAD
     * payment
=======
     * Pay the payment
     *
>>>>>>> f02794c34f11533204f3e74bd6e7273a261dbf96
     * @param hash payment hash
     * @param type TransferType
     */
    public void typePayment(String hash,int type, final WalletListener<String> listener) {
        transactionType = TransferType.getType(type);
        connectDialog = DialogUtil.showConnectPay(mActivity);
        WalletOuterClass.Payment payment = WalletOuterClass.Payment.newBuilder()
                .setHashId(hash)
                .setFee(getFee())
                .setPayType(transactionType.getType()).build();

        OkHttpUtil.getInstance().postEncrySelf(UriUtil.WALLET_V2_SERVICE_PAYMENT, payment, new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                dealTransferResult(mActivity,response,listener);
            }

            @Override
            public void onError(Connect.HttpResponse response) {
                showErrorToast(response);
            }
        });
    }

    /**
<<<<<<< HEAD
=======
     * All the money raised
     *
>>>>>>> f02794c34f11533204f3e74bd6e7273a261dbf96
     * @param groupkey
     * @param amount
     * @param size
     * @param tips
     */
    public void crowdFuning(String groupkey, long amount, int size, String tips, final WalletListener listener) {
        WalletOuterClass.CrowdfundingRequest crowdfundingRequest = WalletOuterClass.CrowdfundingRequest.newBuilder()
                .setCurrency(currencyEnum.getCode())
                .setGroupIdentifier(groupkey)
                .setAmount(amount)
                .setSize(size)
                .setTips(tips).build();

        OkHttpUtil.getInstance().postEncrySelf(UriUtil.WALLET_V2_SERVICE_CROWDFUNING, crowdfundingRequest, new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                try {
                    Connect.IMResponse imResponse = Connect.IMResponse.parseFrom(response.getBody().toByteArray());
                    //check sign
                    if (!SupportKeyUril.verifySign(imResponse.getSign(), imResponse.getCipherData().toByteArray())) {
                        throw new Exception("Validation fails");
                    }

                    Connect.StructData structData = DecryptionUtil.decodeAESGCMStructData(imResponse.getCipherData());
                    Connect.Crowdfunding crowdfunding = Connect.Crowdfunding.parseFrom(structData.getPlainData());
                    if (ProtoBufUtil.getInstance().checkProtoBuf(crowdfunding)) {
                        listener.success(crowdfunding);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Connect.HttpResponse response) {
                Toast.makeText(BaseApplication.getInstance().getAppContext(), response.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
<<<<<<< HEAD
     * @param listAddress
     * @param receiverIdentifier // group id or user pubkey
     * @param type // private group outer //0：inner 1：outer
     * @param category //0：persional 1：group
=======
     * Red envelopes
     *
     * @param listAddress
     * @param receiverIdentifier group id or user pubkey
     * @param type private group outer 0:in 1:out
     * @param category 0:personal 1:group
>>>>>>> f02794c34f11533204f3e74bd6e7273a261dbf96
     * @param size
     * @param amount
     * @param tips
     * @param listener
     */
    public void luckyPacket(ArrayList<String> listAddress, String receiverIdentifier, int type, int category,
                            int size, long amount, String tips, final WalletListener<String> listener){
        transactionType = TransferType.TransactionTypeLuckypackage;
        connectDialog = DialogUtil.showConnectPay(mActivity);
        WalletOuterClass.LuckyPackageRequest.Builder builder = WalletOuterClass.LuckyPackageRequest.newBuilder();
        WalletOuterClass.SpentCurrency.Builder builderSend = WalletOuterClass.SpentCurrency.newBuilder();
        WalletOuterClass.Txin txin = getTxin(listAddress);
        if(getTxin(listAddress) != null){
            builderSend.setTxin(txin);
        }
        builderSend.setCurrency(currencyEnum.getCode());
        builder.setSpentCurrency(builderSend.build());
        builder.setReceiverIdentifier(receiverIdentifier);
        builder.setTyp(type);
        builder.setCategory(category);
        builder.setSize(size);
        builder.setAmount(amount);
        builder.setFee(getFee());
        builder.setTips(tips);
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.WALLET_V2_SERVICE_LUCKPACKAGE, builder.build(), new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                dealTransferResult(mActivity, response, listener);
            }

            @Override
            public void onError(Connect.HttpResponse response) {
                showErrorToast(response);
            }
        });
    }

    /**
<<<<<<< HEAD
     * external transfer
=======
     * External transfer
>>>>>>> f02794c34f11533204f3e74bd6e7273a261dbf96
     *
     * @param listAddress
     * @param amount
     * @param listener
     */
    public void outerTransfer(ArrayList<String> listAddress, long amount, final WalletListener<String> listener){
        transactionType = TransferType.TransactionTypeURLTransfer;
        connectDialog = DialogUtil.showConnectPay(mActivity);
        WalletOuterClass.OutTransfer.Builder builder = WalletOuterClass.OutTransfer.newBuilder();
        WalletOuterClass.SpentCurrency.Builder builderSend = WalletOuterClass.SpentCurrency.newBuilder();
        WalletOuterClass.Txin txin = getTxin(listAddress);
        if(getTxin(listAddress) != null){
            builderSend.setTxin(txin);
        }
        builder.setSpentCurrency(builderSend);
        builder.setAmount(amount);
        builder.setFee(getFee());
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.WALLET_V2_SERVICE_EXTERNAL, builder.build(), new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                dealTransferResult(mActivity, response, listener);
            }

            @Override
            public void onError(Connect.HttpResponse response) {
                showErrorToast(response);
            }
        });

    }

    /**
<<<<<<< HEAD
     * Broadcast Trading
=======
     * Broadcast transfer transaction
>>>>>>> f02794c34f11533204f3e74bd6e7273a261dbf96
     */
    private void publishTransfer(String rawHex, final String hashId, final WalletListener<String> listener){
        WalletOuterClass.PublishTransaction publishTransaction = WalletOuterClass.PublishTransaction.newBuilder()
                .setCurrency(CurrencyEnum.BTC.getCode())
                .setHashId(hashId)
                .setTxHex(rawHex)
                .setTransactionType(transactionType.getType())
                .build();
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.WALLET_V2_SERVICE_PUBLISH, publishTransaction, new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(final Connect.HttpResponse response) {
                pinTransferDialog.closeStatusDialog(MdStyleProgress.Status.LoadSuccess, new PaymentPwd.OnAnimationListener() {
                    @Override
                    public void onComplete() {
                        listener.success(hashId);
                    }
                });
            }

            @Override
            public void onError(Connect.HttpResponse response) {
                listener.fail(WalletListener.WalletError.NETError);
                pinTransferDialog.closeStatusDialog(MdStyleProgress.Status.LoadFail);
            }
        });
    }


    /**
     * Handling transfer results
     * @param response
     */
    private void showErrorToast(Connect.HttpResponse response){
        if(response.getCode() == 2400){
            ToastEUtil.makeText(mActivity,R.string.Wallet_Amount_is_too_small,ToastEUtil.TOAST_STATUS_FAILE).show();
        }else{
            ToastEUtil.makeText(mActivity,response.getMessage(),ToastEUtil.TOAST_STATUS_FAILE).show();
        }
        connectDialog.dismiss();
    }

    private void dealTransferResult(final Activity activity, Connect.HttpResponse response, final WalletListener listener){
        try{
            Connect.IMResponse imResponse = Connect.IMResponse.parseFrom(response.getBody().toByteArray());
            Connect.StructData structData = DecryptionUtil.decodeAESGCMStructData(imResponse.getCipherData());
            WalletOuterClass.OriginalTransactionResponse originalResponse = WalletOuterClass.OriginalTransactionResponse.parseFrom(structData.getPlainData());
            final WalletOuterClass.OriginalTransaction originalTransaction = originalResponse.getData();
            String message = "";
            switch (TransferResultCode.getTransferCode(originalResponse.getCode())){
                case TRANSFER_SUCCESS:
                    checkPin(activity, originalTransaction, listener);
                    return;
                case FEEEMPTY:
                    if(connectDialog != null)
                        connectDialog.dismiss();
                    return;
                case UNSPENTTOOLARGE:
                    if(connectDialog != null)
                        connectDialog.dismiss();
                    ToastEUtil.makeText(activity,R.string.Wallet_Too_much_transaction_can_not_generated,ToastEUtil.TOAST_STATUS_FAILE).show();
                    return;
                case UNSPENTERROR:
                    if(connectDialog != null)
                        connectDialog.dismiss();
                    ToastEUtil.makeText(activity,R.string.Wallet_Insufficient_balance,ToastEUtil.TOAST_STATUS_FAILE).show();
                    return;
                case UNSPENTNOTENOUGH:
                    if(connectDialog != null)
                        connectDialog.dismiss();
                    ToastEUtil.makeText(activity,R.string.Wallet_Insufficient_balance,ToastEUtil.TOAST_STATUS_FAILE).show();
                    return;
                case OUTPUTDUST:
                    if(connectDialog != null)
                        connectDialog.dismiss();
                    ToastEUtil.makeText(activity,R.string.Wallet_Amount_is_too_small,ToastEUtil.TOAST_STATUS_FAILE).show();
                    return;
                case FEETOSAMLL:
                    message = activity.getString(R.string.Wallet_Transaction_fee_too_low_Continue);
                    break;
                case UNSPENTDUST:
                    message = activity.getString(R.string.Wallet_Charge_small_calculate_to_the_poundage,
                            RateFormatUtil.longToDoubleBtc(originalTransaction.getOddChange()));
                    break;
                case AUTOMAX:
                    message = activity.getString(R.string.Wallet_Auto_fees_greater_than_the_maximum_continue,
                            RateFormatUtil.longToDoubleBtc(originalTransaction.getEstimateFee()));
                    break;
                default:
                    break;
            }

            DialogUtil.showAlertTextView(activity, activity.getString(R.string.Set_tip_title),message, "", "", false,
                    new DialogUtil.OnItemClickListener() {
                        @Override
                        public void confirm(String value) {
                            checkPin(activity, originalTransaction, listener);
                        }

                        @Override
                        public void cancel() {
                            if(connectDialog != null)
                                connectDialog.dismiss();
                        }
                    });
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    /**
     * Check the transaction password and return the private key array
     *
     * @param activity
     * @param transaction
     * @param listener
     */
    private void checkPin(final Activity activity, final WalletOuterClass.OriginalTransaction transaction, final WalletListener listener){
        final ArrayList<String> addressList = new ArrayList<>();
        for(String address : transaction.getAddressesList()){
            addressList.add(address);
        }
        if(addressList.size() == 0){
            return;
        }
        final WalletListener pinListener = new WalletListener<ArrayList<String>>() {
            @Override
            public void success(ArrayList<String> priKeyList) {
                String rawHex = NativeWallet.getInstance().initCurrency(currencyEnum).getSignRawTrans(priKeyList,
                        transaction.getVts(), transaction.getRawhex());
                if (TextUtils.isEmpty(rawHex)) {
                    pinTransferDialog.closeStatusDialog(MdStyleProgress.Status.LoadFail);
                } else {
                    publishTransfer(rawHex, transaction.getHashId(), listener);
                }
            }
            @Override
            public void fail(WalletError error) {}
        };

        List<CurrencyAddressEntity> list = CurrencyHelper.getInstance().loadCurrencyAddress(addressList);
        if(addressList.size() != list.size()){
            NativeWallet.getInstance().initAccount(CurrencyEnum.BTC).requestAddressList(new WalletListener<WalletOuterClass.Coin>() {
                @Override
                public void success(WalletOuterClass.Coin coin) {
                    decodePayload(activity, addressList, transaction, pinListener);
                }

                @Override
                public void fail(WalletError error) {}
            });
        }else{
            decodePayload(activity, addressList, transaction, pinListener);
        }

        if(connectDialog != null)
            connectDialog.dismiss();
    }

    /**
     * Decrypt the private key
     *
     * @param activity
     * @param addressList
     * @param listener
     */
    private void decodePayload(Activity activity, final ArrayList<String> addressList, WalletOuterClass.OriginalTransaction transaction,
                               final WalletListener listener){
        String payload = "";
        final CurrencyEntity currencyEntity = CurrencyHelper.getInstance().loadCurrency(currencyEnum.getCode());
        if(currencyEntity.getCategory() == BaseCurrency.CATEGORY_PRIKEY || currencyEntity.getCategory() == BaseCurrency.CATEGORY_SALT_SEED){
            payload = currencyEntity.getPayload();
        }else if(currencyEntity.getCategory() == BaseCurrency.CATEGORY_BASESEED){
            WalletBean walletBean = SharePreferenceUser.getInstance().getWalletInfo();
            payload = walletBean.getPayload();
        }

        pinTransferDialog = new PinTransferDialog();
        pinTransferDialog.showPaymentPwd(activity, addressList,transaction.getTxOutsList(), transaction.getFee(), transaction.getFixedFee(), transaction.getCurrency(),
                payload, new PaymentPwd.OnTrueListener() {
            @Override
            public void onTrue(String decodeStr) {
                ArrayList<String> priList = new ArrayList<>();
                if(currencyEntity.getCategory() == BaseCurrency.CATEGORY_PRIKEY){
                    // private key
                    String priKey = new String(StringUtil.hexStringToBytes(decodeStr));
                    priList.add(priKey);
                }else if(currencyEntity.getCategory() == BaseCurrency.CATEGORY_SALT_SEED){
                    // Import third-party seeds

                }if(currencyEntity.getCategory() == BaseCurrency.CATEGORY_BASESEED){
                    // Gets the index corresponding to the input address
                    ArrayList<Integer> indexList = new ArrayList<>();
                    for(String address : addressList){
                        CurrencyAddressEntity addressEntity = CurrencyHelper.getInstance().loadCurrencyAddressFromAddress(address);
                        indexList.add(addressEntity.getIndex());
                    }
                    for(Integer index : indexList){
                        String priKey = NativeWallet.getInstance().initCurrency(currencyEnum).createPriKey(decodeStr,currencyEntity.getSalt(),index);
                        priList.add(priKey);
                    }
                }
                listener.success(priList);
            }
        });
    }



}
