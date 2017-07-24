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
 * 具体业务层接口
 * Created by Administrator on 2017/7/18.
 */
public class BaseBusiness {

    private final Activity mActivity;
    private PinTransferDialog pinTransferDialog;
    private CurrencyEnum currencyEnum;

    private final int TRANSFER_SUUESS = 0; // 转账参数正确
    private final int FEETOSAMLL = 3000; // 手续费太小
    private final int FEEEMPTY = 3001; // 手续费为空
    private final int UNSPENTTOOLARGE = 3002; // 交易笔数太多
    private final int UNSPENTERROR = 3003;  //
    private final int UNSPENTNOTENOUGH = 3004; // 余额不足
    private final int OUTPUTDUST = 3005; // 输出金额太小（肮脏）
    private final int UNSPENTDUST = 3006; // 找零太小
    private final int AUTOMAX = 3007; // 自动计算手续费大于最大阈值
    private Dialog connectDialog;
    public BaseBusiness(Activity mActivity) {
        this.mActivity = mActivity;
    }

    public BaseBusiness(Activity mActivity, CurrencyEnum currencyEnum) {
        this.mActivity = mActivity;
        this.currencyEnum = currencyEnum;
    }

    /**
     * 组装输入Txin
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
     * 组装输出Txouts
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
     * 获取手续费
     * @return
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
     * 地址转账
     * @param outMap
     * @param listener
     */
    public void transferAddress(ArrayList<String> listAddress, HashMap<String,Long> outMap, final WalletListener listener){
        connectDialog = DialogUtil.showConnectPay(mActivity);
        WalletOuterClass.TransferRequest.Builder builder = WalletOuterClass.TransferRequest.newBuilder();
        WalletOuterClass.SpentCurrency.Builder builderSend = WalletOuterClass.SpentCurrency.newBuilder();
        // 组装输入
        WalletOuterClass.Txin txin = getTxin(listAddress);
        if(getTxin(listAddress) != null){
            builderSend.setTxin(txin);
        }
        builderSend.setCurrency(currencyEnum.getCode());
        builder.setSpentCurrency(builderSend.build());
        // 组装输出
        ArrayList<WalletOuterClass.Txout> txoutList = getTxOut(outMap);
        for(WalletOuterClass.Txout txout : txoutList){
            builder.addTxOut(txout);
        }
        // 手续费
        builder.setFee(getFee());
        builder.setTips("");
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.WALLET_V2_SERVICE_TRANSFER_ADDRESS, builder.build(), new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                dealTransferResult(mActivity, response, listener);
            }

            @Override
            public void onError(Connect.HttpResponse response) {
                ToastEUtil.makeText(mActivity,response.getMessage(),ToastEUtil.TOAST_STATUS_FAILE).show();
                connectDialog.dismiss();
            }
        });
    }

    /**
     * Connect用户转账
     * @param outMap
     * @param listener
     */
    public void transferConnectUser(ArrayList<String> listAddress, HashMap<String,Long> outMap, final WalletListener listener){
        connectDialog = DialogUtil.showConnectPay(mActivity);
        WalletOuterClass.ConnectTransferRequest.Builder builder = WalletOuterClass.ConnectTransferRequest.newBuilder();
        WalletOuterClass.SpentCurrency.Builder builderSend = WalletOuterClass.SpentCurrency.newBuilder();
        // 组装输入
        WalletOuterClass.Txin txin = getTxin(listAddress);
        if(getTxin(listAddress) != null){
            builderSend.setTxin(txin);
        }
        builderSend.setCurrency(currencyEnum.getCode());
        builder.setSpentCurrency(builderSend.build());
        // 组装输出
        for (HashMap.Entry<String, Long> entry : outMap.entrySet()) {
            System.out.println("key= " + entry.getKey() + " and value= " + entry.getValue());
            WalletOuterClass.ConnectTxout txout = WalletOuterClass.ConnectTxout.newBuilder()
                    .setUid(entry.getKey())
                    .setAmount(entry.getValue())
                    .build();
            builder.addTxOut(txout);
        }
        // 手续费
        builder.setFee(getFee());
        builder.setTips("");
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.WALLET_V2_SERVICE_TRANSFER, builder.build(), new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                dealTransferResult(mActivity, response, listener);
            }

            @Override
            public void onError(Connect.HttpResponse response) {
                ToastEUtil.makeText(mActivity,response.getMessage(),ToastEUtil.TOAST_STATUS_FAILE).show();
                connectDialog.dismiss();
            }
        });
    }

    /**
     * 单人收款
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
     * 支付
     * @param hash payment hash
     * @param type TransactionTypeBill = 1
     *             TransactionTypePayCrowding = 2
     *             TransactionTypeLuckypackage = 3
     *             TransactionTypeURLTransfer = 6
     */
    public void typePayment(String hash,int type, final WalletListener listener) {
        connectDialog = DialogUtil.showConnectPay(mActivity);
        WalletOuterClass.Payment payment = WalletOuterClass.Payment.newBuilder()
                .setHashId(hash)
                .setFee(0L)
                .setPayType(type).build();

        OkHttpUtil.getInstance().postEncrySelf(UriUtil.WALLET_V2_SERVICE_PAYMENT, payment, new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                dealTransferResult(mActivity, response, listener);
            }

            @Override
            public void onError(Connect.HttpResponse response) {
                ToastEUtil.makeText(mActivity,response.getMessage(),ToastEUtil.TOAST_STATUS_FAILE).show();
                connectDialog.dismiss();
            }
        });
    }

    /**
     * 众筹
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
     * 红包
     * @param listAddress
     * @param receiverIdentifier // group id or user pubkey
     * @param type // private group outer //0：内部 1：外部
     * @param category //0：个人 1：群主
     * @param size
     * @param amount
     * @param tips
     * @param listener
     */
    public void luckyPacket(ArrayList<String> listAddress, String receiverIdentifier, int type, int category,
                            int size, long amount, String tips, final WalletListener listener){
        connectDialog = DialogUtil.showConnectPay(mActivity);
        WalletOuterClass.LuckyPackageRequest.Builder builder = WalletOuterClass.LuckyPackageRequest.newBuilder();
        WalletOuterClass.SpentCurrency.Builder builderSend = WalletOuterClass.SpentCurrency.newBuilder();
        // 组装输入
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
                ToastEUtil.makeText(mActivity,response.getMessage(),ToastEUtil.TOAST_STATUS_FAILE).show();
                connectDialog.dismiss();
            }
        });
    }

    /**
     * 外部转账
     *
     * @param listAddress
     * @param amount
     * @param listener
     */
    public void outerTransfer(ArrayList<String> listAddress, long amount, final WalletListener listener){
        connectDialog = DialogUtil.showConnectPay(mActivity);
        WalletOuterClass.OutTransfer.Builder builder = WalletOuterClass.OutTransfer.newBuilder();
        WalletOuterClass.SpentCurrency.Builder builderSend = WalletOuterClass.SpentCurrency.newBuilder();
        // 组装输入
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
                ToastEUtil.makeText(mActivity,response.getMessage(),ToastEUtil.TOAST_STATUS_FAILE).show();
                connectDialog.dismiss();
            }
        });

    }

    /**
     * 广播交易
     */
    private void publishTransfer(String rawHex, final String hashId, final WalletListener listener){
        WalletOuterClass.PublishTransaction publishTransaction = WalletOuterClass.PublishTransaction.newBuilder()
                .setCurrency(CurrencyEnum.BTC.getCode())
                .setHashId(hashId)
                .setTxHex(rawHex)
                .build();
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.WALLET_V2_SERVICE_PUBLISH, publishTransaction, new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(final Connect.HttpResponse response) {
                try {
                    /*Connect.IMResponse imResponse = Connect.IMResponse.parseFrom(response.getBody().toByteArray());
                    Connect.StructData structData = DecryptionUtil.decodeAESGCMStructData(imResponse.getCipherData());
                    WalletOuterClass.ResponsePublish responsePublish = WalletOuterClass.ResponsePublish.parseFrom(structData.getPlainData());*/
                    listener.success(hashId);
                }catch (Exception e){
                    e.printStackTrace();
                }
                pinTransferDialog.closeStatusDialog(MdStyleProgress.Status.LoadSuccess);
            }

            @Override
            public void onError(Connect.HttpResponse response) {
                listener.fail(WalletListener.WalletError.NETError);
                pinTransferDialog.closeStatusDialog(MdStyleProgress.Status.LoadFail);
            }
        });
    }

    /**
     * 处理转账结果
     * @param activity
     * @param response
     * @param listener
     */
    private void dealTransferResult(final Activity activity, Connect.HttpResponse response, final WalletListener listener){
        try{
            Connect.IMResponse imResponse = Connect.IMResponse.parseFrom(response.getBody().toByteArray());
            Connect.StructData structData = DecryptionUtil.decodeAESGCMStructData(imResponse.getCipherData());
            WalletOuterClass.OriginalTransactionResponse originalResponse = WalletOuterClass.OriginalTransactionResponse.parseFrom(structData.getPlainData());
            final WalletOuterClass.OriginalTransaction originalTransaction = originalResponse.getData();
            String message = "";
            switch (originalResponse.getCode()){
                case TRANSFER_SUUESS:
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
                    message = activity.getString(R.string.Wallet_Auto_fees_is_greater_than_the_maximum_set_maximum_and_continue,
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
     * 检查交易密码,返回私钥数组
     */
    private void checkPin(final Activity activity, final WalletOuterClass.OriginalTransaction transaction, final WalletListener listener){
        final ArrayList<String> addressList = new ArrayList<>();
        for(String address : transaction.getAddressesList()){
            addressList.add(address);
        }
        if(addressList.size() == 0){
            return;
        }
        final WalletListener walletListener = new WalletListener<ArrayList<String>>() {
            @Override
            public void success(ArrayList<String> priKeyList) {
                String rawHex = NativeWallet.getInstance().initCurrency(currencyEnum).getSignRawTrans(priKeyList,
                        transaction.getVts(), transaction.getRawhex());
                if(TextUtils.isEmpty(rawHex)){
                    pinTransferDialog.closeStatusDialog(MdStyleProgress.Status.LoadFail);
                }else{
                    publishTransfer(rawHex, transaction.getHashId(), listener);
                }
            }

            @Override
            public void fail(WalletError error) {
                pinTransferDialog.closeStatusDialog(MdStyleProgress.Status.LoadFail);
            }
        };
        List<CurrencyAddressEntity> list = CurrencyHelper.getInstance().loadCurrencyAddress(addressList);
        if(addressList.size() != list.size()){
            NativeWallet.getInstance().initAccount(CurrencyEnum.BTC).requestAddressList(new WalletListener<String>() {
                @Override
                public void success(String list) {
                    decodePayload(activity, addressList, transaction, walletListener);
                }

                @Override
                public void fail(WalletError error) {}
            });
        }else{
            decodePayload(activity, addressList, transaction, walletListener);
        }
        if(connectDialog != null)
            connectDialog.dismiss();
    }

    /**
     * 解密私钥
     * @param activity
     * @param addressList
     * @param listener
     */
    private void decodePayload(Activity activity, final ArrayList<String> addressList, WalletOuterClass.OriginalTransaction transaction,
                               final WalletListener listener){
        String payload = "";
        final ArrayList<Integer> indexList = new ArrayList<>();
        /*for(String address : addressList){
            // 获取输入地址对应的index
            CurrencyAddressEntity addressEntity = CurrencyHelper.getInstance().loadCurrencyAddressFromAddress(address);
            indexList.add(addressEntity.getIndex());
        }*/
        List<CurrencyAddressEntity> list = CurrencyHelper.getInstance().loadCurrencyAddress(currencyEnum.getCode());
        indexList.add(list.get(0).getIndex());

        // 获取打款币种的加密payload
        final CurrencyEntity currencyEntity = CurrencyHelper.getInstance().loadCurrency(currencyEnum.getCode());
        if(currencyEntity.getCategory() == BaseCurrency.CATEGORY_PRIKEY || currencyEntity.getCategory() == BaseCurrency.CATEGORY_SALT_SEED){
            payload = currencyEntity.getPayload();
        }else if(currencyEntity.getCategory() == BaseCurrency.CATEGORY_BASESEED){
            WalletBean walletBean = SharePreferenceUser.getInstance().getWalletInfo();
            payload = walletBean.getPayload();
        }
        final ArrayList<String> priList = new ArrayList<>();
        pinTransferDialog = new PinTransferDialog();

        Long fee;
        if(ParamManager.getInstance().getPaySet().isAutoFee()){
            fee = transaction.getEstimateFee();
        }else{
            fee = transaction.getFee();
        }
        pinTransferDialog.showPaymentPwd(activity, transaction.getTxOutsList(), fee, transaction.getCurrency(),
                payload, new PaymentPwd.OnTrueListener() {
            @Override
            public void onTrue(String decodeStr) {
                if(currencyEntity.getCategory() == BaseCurrency.CATEGORY_PRIKEY){
                    // 纯私钥
                    String priKey = new String(StringUtil.hexStringToBytes(decodeStr));
                    priList.add(priKey);
                }else if(currencyEntity.getCategory() == BaseCurrency.CATEGORY_SALT_SEED){
                    // 导入第三方种子

                }if(currencyEntity.getCategory() == BaseCurrency.CATEGORY_BASESEED){
                    // 原始种子，生成货币种子，再生成对应的私钥
                    for(Integer index : indexList){
                        String priKey = NativeWallet.getInstance().initCurrency(currencyEnum).ceaterPriKey(decodeStr,currencyEntity.getSalt(),index);
                        priList.add(priKey);
                    }
                }
                listener.success(priList);
            }
        });
    }

}
