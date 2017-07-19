package connect.wallet.cwallet.business;

import android.app.Activity;
import android.widget.Toast;

import com.google.protobuf.ProtocolStringList;

import java.util.ArrayList;
import java.util.List;

import connect.activity.base.BaseApplication;
import connect.activity.chat.bean.GatherBean;
import connect.activity.chat.bean.MsgSend;
import connect.activity.set.bean.PaySetBean;
import connect.activity.wallet.bean.WalletBean;
import connect.database.SharePreferenceUser;
import connect.database.green.DaoHelper.CurrencyHelper;
import connect.database.green.DaoHelper.ParamManager;
import connect.database.green.bean.CurrencyAddressEntity;
import connect.database.green.bean.CurrencyEntity;
import connect.im.bean.MsgType;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.DialogUtil;
import connect.utils.ProtoBufUtil;
import connect.utils.UriUtil;
import connect.utils.cryption.DecryptionUtil;
import connect.utils.cryption.SupportKeyUril;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import connect.wallet.cwallet.NativeWallet;
import connect.wallet.cwallet.bean.CurrencyEnum;
import connect.wallet.cwallet.currency.BaseCurrency;
import connect.wallet.cwallet.inter.WalletListener;
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

    public BaseBusiness(Activity mActivity) {
        this.mActivity = mActivity;
    }

    public BaseBusiness(Activity mActivity, CurrencyEnum currencyEnum) {
        this.mActivity = mActivity;
        this.currencyEnum = currencyEnum;
    }

    /**
     * 转账
     * @param txin
     * @param outPuts
     * @param listener
     */
    public void transferAddress(WalletOuterClass.Txin txin, List<WalletOuterClass.Txout> outPuts, WalletListener listener){
        PaySetBean paySetBean = ParamManager.getInstance().getPaySet();
        WalletOuterClass.TransferRequest.Builder builder = WalletOuterClass.TransferRequest.newBuilder();
        WalletOuterClass.SpentCurrency.Builder builderSend = WalletOuterClass.SpentCurrency.newBuilder();
        builderSend.setCurrency(currencyEnum.getCode());
        if(txin != null){
            builderSend.setTxin(txin);
        }
        builder.setSpentCurrency(builderSend.build());

        if(paySetBean.isAutoFee()){
            builder.setFee(0L);
        }else{
            builder.setFee(paySetBean.getFee());
        }
        for(WalletOuterClass.Txout txout : outPuts){
            builder.addTxOut(txout);
        }
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
     * 单人收款
     */
    public void friendReceiver(long amount, String senderaddress, String tips, final WalletListener listener) {
        WalletOuterClass.ReceiveRequest receiveRequest = WalletOuterClass.ReceiveRequest.newBuilder()
                .setCurrency(getCurrency())
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
        WalletOuterClass.Payment payment = WalletOuterClass.Payment.newBuilder()
                .setHashId(hash)
                .setFee(0L)
                .setPayType(type).build();

        OkHttpUtil.getInstance().postEncrySelf(UriUtil.WALLET_V2_SERVICE_PAYMENT, payment, new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                try {
                    Connect.IMResponse imResponse = Connect.IMResponse.parseFrom(response.getBody().toByteArray());
                    //check sign
                    if (!SupportKeyUril.verifySign(imResponse.getSign(), imResponse.getCipherData().toByteArray())) {
                        throw new Exception("Validation fails");
                    }

                    Connect.StructData structData = DecryptionUtil.decodeAESGCMStructData(imResponse.getCipherData());
                    WalletOuterClass.OriginalTransactionResponse originalTransactionResponse = WalletOuterClass.OriginalTransactionResponse.parseFrom(structData.getPlainData());
                    if (ProtoBufUtil.getInstance().checkProtoBuf(originalTransactionResponse)) {
                        listener.success(originalTransactionResponse);
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
     * 众筹
     * @param groupkey
     * @param amount
     * @param size
     * @param tips
     */
    public void crowdFuning(String groupkey, long amount, int size, String tips, final WalletListener listener) {
        WalletOuterClass.CrowdfundingRequest crowdfundingRequest = WalletOuterClass.CrowdfundingRequest.newBuilder()
                .setCurrency(getCurrency())
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
     * @param txin
     * @param reciverId
     * @param type
     * @param size
     * @param amount
     * @param category
     * @param listener
     */
    public void luckyPacket(WalletOuterClass.Txin txin, String reciverId, int type, int size,
                     long amount, int category, WalletListener listener){

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
            public void onResponse(Connect.HttpResponse response) {
                try {
                    Connect.IMResponse imResponse = Connect.IMResponse.parseFrom(response.getBody().toByteArray());
                    Connect.StructData structData = DecryptionUtil.decodeAESGCMStructData(imResponse.getCipherData());
                    WalletOuterClass.ResponsePublish responsePublish = WalletOuterClass.ResponsePublish.parseFrom(structData.getPlainData());
                    responsePublish.getTxid();
                    listener.success(responsePublish);

                }catch (Exception e){
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
                    checkPin(activity, originalTransaction.getAddressesList(), new WalletListener<ArrayList<String>>() {
                        @Override
                        public void success(ArrayList<String> priKeyList) {
                            String rawHex = NativeWallet.getInstance().initCurrency(currencyEnum).getSignRawTrans(priKeyList,
                                    originalTransaction.getVts(), originalTransaction.getRawhex());
                            publishTransfer(rawHex, originalTransaction.getHashId(), listener);
                        }

                        @Override
                        public void fail(WalletError error) {

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
    private void checkPin(Activity activity, ProtocolStringList addressList, final WalletListener listener){
        if(addressList.size() == 0){
            return;
        }
        pinTransferDialog = new PinTransferDialog();
        String payload = "";
        final ArrayList<Integer> indexList = new ArrayList<>();
        for(String address : addressList){
            // 获取输入地址对应的index
            CurrencyAddressEntity addressEntity = CurrencyHelper.getInstance().loadCurrencyAddressFromAddress(address);
            indexList.add(addressEntity.getIndex());
        }
        // 获取打款币种的加密payload
        final CurrencyEntity currencyEntity = CurrencyHelper.getInstance().loadCurrency(currencyEnum.getCode());
        if(currencyEntity.getCategory() == BaseCurrency.CATEGORY_PRIKEY || currencyEntity.getCategory() == BaseCurrency.CATEGORY_SALT_SEED){
            payload = currencyEntity.getPayload();
        }else if(currencyEntity.getCategory() == BaseCurrency.CATEGORY_BASESEED){
            WalletBean walletBean = SharePreferenceUser.getInstance().getWalletInfo();
            payload = walletBean.getPayload();
        }
        final ArrayList<String> priList = new ArrayList<String>();
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

    /**
     * 获取币种
     * @return
     */
    public int getCurrency() {
        if (currencyEnum == null) {
            throw new NumberFormatException("currencyEnum is null");
        }
        return currencyEnum.getCode();
    }
}
