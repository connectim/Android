package connect.activity.wallet.manager;

import android.app.Activity;
import android.os.Bundle;

import com.google.protobuf.ByteString;

import java.util.List;

import connect.activity.wallet.bean.WalletBean;
import connect.database.MemoryDataManager;
import connect.database.SharePreferenceUser;
import connect.database.green.DaoHelper.CurrencyHelper;
import connect.database.green.bean.CurrencyEntity;
import connect.ui.activity.R;
import connect.utils.DialogUtil;
import connect.utils.StringUtil;
import connect.utils.ToastEUtil;
import connect.utils.UriUtil;
import connect.utils.cryption.DecryptionUtil;
import connect.utils.cryption.EncoPinBean;
import connect.utils.cryption.SupportKeyUril;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import connect.widget.random.RandomVoiceActivity;
import protos.Connect;
import wallet_gateway.WalletOuterClass;

import static connect.activity.wallet.manager.CurrencyType.BTC;

/**
 * Created by Administrator on 2017/7/11 0011.
 */

public class WalletManager {

    private OnWalletListener onWalletListener;
    private Activity mActivity;

    public WalletManager(Activity mActivity) {
        this.mActivity = mActivity;
    }

    public void checkAccount(OnWalletListener onWalletListener){
        this.onWalletListener = onWalletListener;
        requestWalletBase();
        /*final WalletBean walletBean = SharePreferenceUser.getInstance().getWalletInfo();
        if(walletBean == null){
            // 用户没有钱包数据
            requestWalletBase();
        }else{
            // 用户已经拉取过钱包数据
            List<CurrencyEntity> list = CurrencyHelper.getInstance().loadCurrencyList();
            if(list == null || list.size() == 0){
                // 用户已经创建过钱包到时没有创建币种
                DialogUtil.showAlertTextView(mActivity, mActivity.getString(R.string.Set_tip_title),
                        "你还没有为钱包创建币种",
                        "", "立即创建", true, new DialogUtil.OnItemClickListener() {
                            @Override
                            public void confirm(String value) {
                                CurrencyManage currencyManage = new CurrencyManage();
                                currencyManage.createCurrencyBaseSeed(mActivity, CurrencyManage.CURRENCY_DEFAULT, new CurrencyManage.OnCreateCurrencyListener() {
                                    @Override
                                    public void success(CurrencyEntity currencyEntity) {

                                    }

                                    @Override
                                    public void fail(String message) {

                                    }
                                });
                            }

                            @Override
                            public void cancel() {

                            }
                        });
            }
        }*/
    }

    /**
     * 同步钱包账户
     */
    private void requestWalletBase(){
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.WALLET_V2_SYNC, ByteString.copyFrom(new byte[]{}), new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                try {
                    Connect.IMResponse imResponse = Connect.IMResponse.parseFrom(response.getBody().toByteArray());
                    Connect.StructData structData = DecryptionUtil.decodeAESGCMStructData(imResponse.getCipherData());
                    WalletOuterClass.RespSyncWallet respSyncWallet = WalletOuterClass.RespSyncWallet.parseFrom(structData.getPlainData());
                    switch (respSyncWallet.getStatus()) {
                        case 0:
                            // 用户没有钱包数据 ，需要创建（新用户）
                            collectSeed();
                            break;
                        case 1:
                            // 更新钱包数据
                            WalletOuterClass.Wallet wallet = respSyncWallet.getWallet();
                            WalletBean walletBean = new WalletBean(wallet.getPayLoad(),wallet.getVer(),
                                    wallet.getVersion(),wallet.getCheckSum());
                            //保存钱包账户信息
                            SharePreferenceUser.getInstance().putWalletInfo(walletBean);
                            List<WalletOuterClass.Coin> list = respSyncWallet.getCoinsList();
                            //保存货币信息
                            CurrencyHelper.getInstance().insertCurrencyListCoin(list);
                            onWalletListener.complete();
                            break;
                        case 3:
                            // 用户为老用户需要迁移
                            CurrencyManage currencyManage = new CurrencyManage();
                            currencyManage.createCurrencyPin(mActivity,
                                    MemoryDataManager.getInstance().getPriKey(),
                                    MemoryDataManager.getInstance().getAddress(),
                                    CurrencyManage.CURRENCY_DEFAULT,
                                    CurrencyManage.WALLET_CATEGORY_PRI,
                                    new CurrencyManage.OnCreateCurrencyListener() {
                                        @Override
                                        public void success(CurrencyEntity currencyEntity) {
                                            onWalletListener.complete();
                                        }

                                        @Override
                                        public void fail(String message) {

                                        }
                                    });
                            break;
                        default:
                            break;
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Connect.HttpResponse response) {
                onWalletListener.fail(response.getMessage());
                ToastEUtil.makeText(mActivity,R.string.Wallet_synchronization_data_failed,ToastEUtil.TOAST_STATUS_FAILE).show();
            }
        });
    }



    /**
     * 收集baseSeed
     */
    private void collectSeed(){
        DialogUtil.showAlertTextView(mActivity, mActivity.getString(R.string.Set_tip_title),
                "你还没有钱包",
                "", "立即创建", true, new DialogUtil.OnItemClickListener() {
                    @Override
                    public void confirm(String value) {
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("type", BTC);
                        RandomVoiceActivity.startActivity(mActivity,bundle);
                    }

                    @Override
                    public void cancel() {

                    }
                });
    }

    /**
     * 创建钱包账户
     */
    public void createWallet(final String seed, String pin, final CurrencyType type){
        WalletOuterClass.RequestWalletInfo.Builder builder = WalletOuterClass.RequestWalletInfo.newBuilder();
        final EncoPinBean encoPinBean = SupportKeyUril.encoPinDefult(seed,pin);
        final String checkSum = StringUtil.cdHash256(SupportKeyUril.PIN_VERSION + "" + encoPinBean.getPayload());
        builder.setPayload(encoPinBean.getPayload());
        builder.setCheckSum(checkSum);
        builder.setVer(SupportKeyUril.PIN_VERSION);

        OkHttpUtil.getInstance().postEncrySelf(UriUtil.WALLET_V2_CREATE,builder.build(), new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                WalletBean walletBean = new WalletBean();
                walletBean.setVer(SupportKeyUril.PIN_VERSION);
                walletBean.setPayload(encoPinBean.getPayload());
                walletBean.setCheckSum(checkSum);
                walletBean.setVersion(1);
                SharePreferenceUser.getInstance().putWalletInfo(walletBean);
                new CurrencyManage().createCurrencyBaseSeed(seed, type, new CurrencyManage.OnCreateCurrencyListener() {
                    @Override
                    public void success(CurrencyEntity currencyEntity) {

                    }

                    @Override
                    public void fail(String message) {

                    }
                });
            }
            @Override
            public void onError(Connect.HttpResponse response) {
                onWalletListener.fail(response.getMessage());
                ToastEUtil.makeText(mActivity,R.string.Wallet_Create_wallet_failed,ToastEUtil.TOAST_STATUS_FAILE).show();
            }
        });
    }

    /**
     * 更新钱包账户信息
     */
    public void updateWalletInfo(final WalletBean walletBean, final OnWalletListener onWalletListener){
        WalletOuterClass.RequestWalletInfo.Builder builder = WalletOuterClass.RequestWalletInfo.newBuilder();
        final String checkSum = StringUtil.cdHash256(walletBean.getVer() + "" + walletBean.getPayload());
        builder.setPayload(walletBean.getPayload());
        builder.setCheckSum(checkSum);
        builder.setVer(walletBean.getVer());
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.WALLET_V2_UPDATA, builder.build(), new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                walletBean.setCheckSum(checkSum);
                walletBean.setVersion(walletBean.getVersion() + 1);
                SharePreferenceUser.getInstance().putWalletInfo(walletBean);
                onWalletListener.complete();
            }

            @Override
            public void onError(Connect.HttpResponse response) {
                onWalletListener.fail(response.getMessage());
            }
        });

    }

    public interface OnWalletListener {
        void complete();

        void fail(String message);
    }

}