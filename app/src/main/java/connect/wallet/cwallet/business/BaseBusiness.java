package connect.wallet.cwallet.business;

import android.app.Activity;
import android.widget.Toast;

import com.google.protobuf.ProtocolStringList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import connect.activity.base.BaseApplication;
import connect.activity.set.bean.PaySetBean;
import connect.activity.wallet.PacketSendActivity;
import connect.activity.wallet.bean.WalletBean;
import connect.database.SharePreferenceUser;
import connect.database.green.DaoHelper.CurrencyHelper;
import connect.database.green.DaoHelper.ParamManager;
import connect.database.green.bean.CurrencyAddressEntity;
import connect.database.green.bean.CurrencyEntity;
import connect.ui.activity.R;
import connect.utils.DialogUtil;
import connect.utils.UriUtil;
import connect.utils.cryption.DecryptionUtil;
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

    public BaseBusiness(Activity mActivity, CurrencyEnum currencyEnum) {
        this.mActivity = mActivity;
        this.currencyEnum = currencyEnum;
    }

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
    public void transferAddress(ArrayList<String> listAddress, HashMap<String,Long> outMap, WalletListener listener){
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
                dealTransferResutl(mActivity, response, new WalletListener<WalletOuterClass.ResponsePublish>() {
                    @Override
                    public void success(WalletOuterClass.ResponsePublish responsePublish) {
                        int a = 1;
                    }

                    @Override
                    public void fail(WalletError error) {
                        int a = 1;
                    }
                });
            }

            @Override
            public void onError(Connect.HttpResponse response) {
                Toast.makeText(BaseApplication.getInstance().getAppContext(),response.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Connect用户转账
     * @param outMap
     * @param listener
     */
    public void transferConnectUser(ArrayList<String> listAddress, HashMap<String,Long> outMap, WalletListener listener){
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
                dealTransferResutl(mActivity, response, new WalletListener<WalletOuterClass.ResponsePublish>() {
                    @Override
                    public void success(WalletOuterClass.ResponsePublish responsePublish) {
                        int a = 1;
                    }

                    @Override
                    public void fail(WalletError error) {
                        int a = 1;
                    }
                });
            }

            @Override
            public void onError(Connect.HttpResponse response) {
                Toast.makeText(BaseApplication.getInstance().getAppContext(),response.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 红包
     * @param listAddress
     * @param receiverIdentifier // group id or user pubkey(address)
     * @param type // private group outer //0：内部 1：外部
     * @param category //0：个人 1：群主
     * @param size
     * @param amount
     * @param tips
     * @param listener
     */
    public void luckyPacket(ArrayList<String> listAddress, String receiverIdentifier,int type, int category,
                            int size, long amount, String tips,WalletListener listener){
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
                dealTransferResutl(mActivity, response, new WalletListener<WalletOuterClass.ResponsePublish>() {
                    @Override
                    public void success(WalletOuterClass.ResponsePublish responsePublish) {
                        int a = 1;
                    }

                    @Override
                    public void fail(WalletError error) {
                        int a = 1;
                    }
                });
            }

            @Override
            public void onError(Connect.HttpResponse response) {
                Toast.makeText(BaseApplication.getInstance().getAppContext(),response.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 外部转账
     *
     * @param fromlist
     * @param indexList
     * @param amount
     */
    public void outerTransfer(List<String> fromlist, List<Integer> indexList, long amount,
                              WalletListener listener){

    }

    /**
     * 支付
     *
     * @param hashId
     * @param payType
     * @param listener
     */
    public void payment(String hashId, int payType, WalletListener listener){

    }

    /**
     * 广播交易
     */
    private void publishTransfer(String rawHex, String hashId, final WalletListener listener){
        WalletOuterClass.PublishTransaction publishTransaction = WalletOuterClass.PublishTransaction.newBuilder()
                .setCurrency(CurrencyEnum.BTC.getCode())
                .setHashId(hashId)
                .setTxHex(rawHex)
                .build();
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.WALLET_V2_SERVICE_PUBLISH, publishTransaction, new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(final Connect.HttpResponse response) {
                try {
                    Connect.IMResponse imResponse = Connect.IMResponse.parseFrom(response.getBody().toByteArray());
                    Connect.StructData structData = DecryptionUtil.decodeAESGCMStructData(imResponse.getCipherData());
                    WalletOuterClass.ResponsePublish responsePublish = WalletOuterClass.ResponsePublish.parseFrom(structData.getPlainData());
                    responsePublish.getTxid();
                    listener.success(responsePublish);
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
    private void dealTransferResutl(Activity activity, Connect.HttpResponse response, final WalletListener listener){
        try{
            Connect.IMResponse imResponse = Connect.IMResponse.parseFrom(response.getBody().toByteArray());
            Connect.StructData structData = DecryptionUtil.decodeAESGCMStructData(imResponse.getCipherData());
            WalletOuterClass.OriginalTransactionResponse originalResponse = WalletOuterClass.OriginalTransactionResponse.parseFrom(structData.getPlainData());
            final WalletOuterClass.OriginalTransaction originalTransaction = originalResponse.getData();
            switch (originalResponse.getCode()){
                case 0:
                    ArrayList<String> addressList = new ArrayList<>();
                    for(String address : originalTransaction.getAddressesList()){
                        addressList.add(address);
                    }
                    checkPin(activity, addressList, new WalletListener<ArrayList<String>>() {
                        @Override
                        public void success(ArrayList<String> priKeyList) {
                            String rawHex = NativeWallet.getInstance().initCurrency(currencyEnum).getSignRawTrans(priKeyList,
                                    originalTransaction.getVts(), originalTransaction.getRawhex());
                            publishTransfer(rawHex, originalTransaction.getHashId(), listener);
                        }

                        @Override
                        public void fail(WalletError error) {
                            pinTransferDialog.closeStatusDialog(MdStyleProgress.Status.LoadFail);
                        }
                    });
                    break;
                case 1:// 手续费过小
                    DialogUtil.showAlertTextView(activity, activity.getString(R.string.Set_tip_title),
                            activity.getString(R.string.Wallet_Transaction_fee_too_low_Continue),
                            "", "", false, new DialogUtil.OnItemClickListener() {
                                @Override
                                public void confirm(String value) {
                                    //checkDust(orderResponse);
                                }

                                @Override
                                public void cancel() {
                                    //connectDialog.dismiss();
                                }
                            });
                    break;
                case 2://手续费为空
                    break;
                case 3://unspent太大
                    break;
                default:
                    break;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 检查交易密码,返回私钥数组
     */
    private void checkPin(final Activity activity, final ArrayList<String> addressList, final WalletListener listener){
        if(addressList.size() == 0){
            return;
        }
        List<CurrencyAddressEntity> list = CurrencyHelper.getInstance().loadCurrencyAddress(addressList);
        if(addressList.size() != list.size()){
            NativeWallet.getInstance().initAccount(CurrencyEnum.BTC).requestAddressList(new WalletListener<String>() {
                @Override
                public void success(String list) {
                    decodePayload(activity,addressList,listener);
                }

                @Override
                public void fail(WalletError error) {

                }
            });
        }else{
            decodePayload(activity,addressList,listener);
        }
    }

    /**
     * 解密私钥
     * @param activity
     * @param addressList
     * @param listener
     */
    private void decodePayload(Activity activity, final ArrayList<String> addressList,final WalletListener listener){
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
        final ArrayList<String> priList = new ArrayList<String>();
        pinTransferDialog = new PinTransferDialog();
        pinTransferDialog.showPaymentPwd(activity, payload, new PaymentPwd.OnTrueListener() {
            @Override
            public void onTrue(String decodeStr) {
                if(currencyEntity.getCategory() == BaseCurrency.CATEGORY_PRIKEY){
                    // 纯私钥
                    String priKey = decodeStr;
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
