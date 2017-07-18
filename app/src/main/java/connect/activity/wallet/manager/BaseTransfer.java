package connect.activity.wallet.manager;

import android.app.Activity;

import com.google.protobuf.ByteString;
import com.google.protobuf.ProtocolStringList;

import java.util.ArrayList;

import connect.activity.wallet.bean.WalletBean;
import connect.database.SharePreferenceUser;
import connect.database.green.DaoHelper.CurrencyHelper;
import connect.database.green.bean.CurrencyAddressEntity;
import connect.database.green.bean.CurrencyEntity;
import connect.utils.cryption.SupportKeyUril;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import connect.wallet.jni.AllNativeMethod;
import connect.widget.MdStyleProgress;
import connect.widget.payment.PaymentPwd;
import connect.widget.payment.PinTransferDialog;

/**
 * 针对所有币种的BaseTransfer
 */

abstract class BaseTransfer {

    private PinTransferDialog pinTransferDialog;
    protected CurrencyType currencyType;


    public BaseTransfer(CurrencyType currencyType) {
        this.currencyType = currencyType;
    }

    /**
     * 检查交易密码,返回私钥数组
     */
    protected ArrayList<String> checkPin(Activity activity, ProtocolStringList addressList){
        pinTransferDialog = new PinTransferDialog();
        String payload = "";
        final ArrayList<Integer> indexList = new ArrayList<>();
        for(String address : addressList){
            // 获取输入地址对应的index
            CurrencyAddressEntity addressEntity = CurrencyHelper.getInstance().loadCurrencyAddressFromAddress(address);
            indexList.add(addressEntity.getIndex());
        }
        // 获取打款币种的加密payload
        final CurrencyEntity currencyEntity = CurrencyHelper.getInstance().loadCurrency(currencyType.getCode());
        if(currencyEntity.getCategory() == CurrencyManage.WALLET_CATEGORY_PRI || currencyEntity.getCategory() == CurrencyManage.WALLET_CATEGORY_SEED){
            payload = currencyEntity.getPayload();
        }else if(currencyEntity.getCategory() == CurrencyManage.WALLET_CATEGORY_BASE){
            WalletBean walletBean = SharePreferenceUser.getInstance().getWalletInfo();
            payload = walletBean.getPayload();
        }
        final ArrayList<String> priList = new ArrayList<String>();
        pinTransferDialog.showPaymentPwd(activity, payload, new PaymentPwd.OnTrueListener() {
            @Override
            public void onTrue(String decodeStr) {

                if(currencyEntity.getCategory() == CurrencyManage.WALLET_CATEGORY_PRI ){
                    // 纯私钥
                    String priKey = decodeStr;
                    priList.add(priKey);
                }else if(currencyEntity.getCategory() == CurrencyManage.WALLET_CATEGORY_SEED){
                    // 导入第三方种子

                }if(currencyEntity.getCategory() == CurrencyManage.WALLET_CATEGORY_BASE){
                    // 原始种子，生成货币种子，再生成对应的私钥
                    for(Integer index : indexList){
                        String currencySeend = SupportKeyUril.xor(decodeStr, currencyEntity.getSalt(), 64);
                        String priKey = AllNativeMethod.cdGetPrivKeyFromSeedBIP44(currencySeend,44,0,0,0,index);
                        priList.add(priKey);
                    }
                }
            }
        });
        return priList;
    }

    /**
     * 广播交易
     */
    protected void publishTransfer(String tansferType, String rawHex, String hashId, final OnBaseResultCall onResultCall){
        OkHttpUtil.getInstance().postEncrySelf("url", ByteString.copyFrom(new byte[]{}), new ResultCall() {
            @Override
            public void onResponse(Object response) {
                pinTransferDialog.closeStatusDialog(MdStyleProgress.Status.LoadSuccess, new PaymentPwd.OnAnimationListener() {
                    @Override
                    public void onComplete() {
                        onResultCall.success();
                    }
                });
            }

            @Override
            public void onError(Object response) {
                pinTransferDialog.closeStatusDialog(MdStyleProgress.Status.LoadFail);
            }
        });
    }

    public interface OnBaseResultCall {
        void success();
    }

}
