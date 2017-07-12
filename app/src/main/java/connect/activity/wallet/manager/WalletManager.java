package connect.activity.wallet.manager;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.URLUtil;

import com.google.protobuf.ByteString;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import connect.activity.wallet.bean.WalletBean;
import connect.database.MemoryDataManager;
import connect.database.SharePreferenceUser;
import connect.database.green.DaoHelper.CurrencyHelper;
import connect.database.green.bean.CurrencyEntity;
import connect.ui.activity.R;
import connect.utils.DialogUtil;
import connect.utils.StringUtil;
import connect.utils.UriUtil;
import connect.utils.cryption.DecryptionUtil;
import connect.utils.cryption.SupportKeyUril;
import connect.utils.okhttp.HttpRequest;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import connect.wallet.jni.AllNativeMethod;
import connect.widget.random.RandomVoiceActivity;
import protos.Connect;
import wallet_gateway.WalletOuterClass;

import static connect.activity.wallet.manager.CurrencyType.*;

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
        WalletBean walletBean = SharePreferenceUser.getInstance().getWalletInfo();
        if(walletBean == null){
            // 用户没有钱包数据
            requestWalletBase();
        }else{
            // 用户已经拉取过钱包数据
            List<CurrencyEntity> list = CurrencyHelper.getInstance().loadCurrencyList();
            if(list == null || list.size() == 0){

                new PinManager().showCheckPin(mActivity, new PinManager.OnPinListener() {
                    @Override
                    public void success(String value) {

                        new CurrencyManage().createCurrencyBaseSeed(value, CurrencyType.BTC, new CurrencyManage.OnCreateCurrencyListener() {
                            @Override
                            public void success(CurrencyEntity currencyEntity) {

                            }

                            @Override
                            public void fail(String message) {

                            }
                        });

                    }
                });

            }
        }
    }

    /*###### 同步钱包账户
    - uri: /wallet/v1/sync
    - method: post
    - response
        * payload
        * salt
        * n
        * version
        * wid
        * currencyArray(币种信息)
        * status(同步状态)*/
    private void requestWalletBase(){
        // 测试新用户流程

        OkHttpUtil.getInstance().postEncrySelf(UriUtil.WALLET_V2_SYNC, ByteString.copyFrom(new byte[]{}),
                new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                try {
                    Connect.IMResponse imResponse = Connect.IMResponse.parseFrom(response.getBody().toByteArray());
                    Connect.StructData structData = DecryptionUtil.decodeAESGCMStructData(imResponse.getCipherData());
                    WalletOuterClass.RespSyncWallet respSyncWallet = WalletOuterClass.RespSyncWallet.parseFrom(structData.getPlainData());
                    switch (respSyncWallet.getStatus()){
                        case 0:
                            // 用户没有钱包数据 ，需要创建（新用户）
                            collectSeed();
                            break;
                        case 2:
                            // 用户有钱包数据，保存到本地
                            SharePreferenceUser.getInstance().putWalletInfo(new WalletBean());
                            CurrencyHelper.getInstance().insertCurrencyList(new ArrayList<CurrencyEntity>());
                            onWalletListener.complete();
                            break;
                        case 3:
                            // 用户为老用户需要迁移
                            new PinManager().showSetNewPin(mActivity, new PinManager.OnPinListener() {
                                @Override
                                public void success(final String pass) {
                                    String salt = AllNativeMethod.cdGetHash256(StringUtil.bytesToHexString(SecureRandom.getSeed(64)));
                                    String payload = SupportKeyUril.encodePri(MemoryDataManager.getInstance().getPriKey(),salt,pass);

                                    WalletBean walletBean = new WalletBean();
                                    walletBean.setPayload(payload);
                                    walletBean.setN(17);
                                    walletBean.setSalt(salt);
                                    walletBean.setVersion(0);
                                    //createWallet(walletBean);
                                }
                            });

                            CurrencyManage currencyManage = new CurrencyManage();
                        /*currencyManage.createCurrencyPri(MemoryDataManager.getInstance().getAddress(),
                                CurrencyType.BTC, new CurrencyManage.OnCreateCurrencyListener() {
                                    @Override
                                    public void success(CurrencyEntity currencyEntity) {
                                        String salt = AllNativeMethod.cdGetHash256(StringUtil.bytesToHexString(SecureRandom.getSeed(64)));
                                        String payload = SupportKeyUril.encodePri(MemoryDataManager.getInstance().getPriKey(),salt,pass);

                                        WalletBean walletBean = new WalletBean();
                                        walletBean.setPayload(payload);
                                        walletBean.setN(17);
                                        walletBean.setSalt(salt);
                                        walletBean.setVersion(0);
                                        createWallet(walletBean);
                                        onWalletListener.complete();
                                    }

                                    @Override
                                    public void fail(String message) {

                                    }
                                });*/
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

    /*###### 创建钱包账户
    - uri: /wallet/v1/base
    - method: post
    - args:
        * checksum
        * payload
        * salt
        * n
    - response
        * wid*/
    /**
     *
     * @param seed
     * @param type 1:种子，2:纯私钥
     */
    public void createWallet(final String seed, String pin, final CurrencyType type){

        WalletOuterClass.RequestWalletInfo.Builder builder = WalletOuterClass.RequestWalletInfo.newBuilder();
        int n = 17;
        final String payload = AllNativeMethod.connectWalletKeyEncrypt(seed,pin,n,1);
        String check_sum = StringUtil.cdHash256(n + "" + payload);
        builder.setPayload(payload);
        builder.setCheckSum(check_sum);
        builder.setN(17);
        builder.setSalt("");


        OkHttpUtil.getInstance().postEncrySelf(UriUtil.WALLET_V2_CREATE,builder.build(), new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                WalletBean walletBean = new WalletBean();
                walletBean.setN(17);
                walletBean.setPayload(payload);
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
                int a = 1;
            }
        });

        /*WalletBean walletBean;
        String baseSalt = AllNativeMethod.cdGetHash256(StringUtil.bytesToHexString(SecureRandom.getSeed(64)));
        int n = 17;
        String encoBaseSalt = SupportKeyUril.encodePri(seed,baseSalt,pin);
        walletBean = new WalletBean();
        walletBean.setSalt(baseSalt);
        walletBean.setN(n);
        walletBean.setPayload(encoBaseSalt);
        walletBean.setVersion(0);*/



        //

        //WalletOuterClass.RequestWalletInfo
        //OkHttpUtil.getInstance().postEncrySelf();


       /* WalletBean walletBean;
        switch (type){
            case 1:
                String baseSalt = AllNativeMethod.cdGetHash256(StringUtil.bytesToHexString(SecureRandom.getSeed(64)));
                int n = 17;
                String encoBaseSalt = SupportKeyUril.encodePri(seed,baseSalt,pin);
                walletBean = new WalletBean();
                walletBean.setSalt(baseSalt);
                walletBean.setN(n);
                walletBean.setPayload(encoBaseSalt);
                walletBean.setVersion(0);
                break;
            case 2:
                break;
            default:
                break;
        }*/


    }

    /*###### 更新钱包账户信息
    - uri: /wallet/v1/update
    - method: post
    - args
        * checksum
        * payload
        * salt
        * n
    - response
        * version*/
    public void requestWalletInfo(WalletBean walletBean, final OnWalletListener onWalletListener){
        OkHttpUtil.getInstance().postEncrySelf("url", ByteString.copyFrom(new byte[]{}), new ResultCall() {
            @Override
            public void onResponse(Object response) {
                SharePreferenceUser.getInstance().putWalletInfo(new WalletBean());
                onWalletListener.complete();
            }

            @Override
            public void onError(Object response) {

            }
        });

    }

    public interface OnWalletListener {

        void complete();

    }

}
