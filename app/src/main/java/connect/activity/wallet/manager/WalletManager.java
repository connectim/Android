package connect.activity.wallet.manager;

import android.app.Activity;
import android.os.Bundle;

import com.google.protobuf.ByteString;

import java.security.SecureRandom;
import java.util.ArrayList;

import connect.activity.wallet.bean.WalletBean;
import connect.database.MemoryDataManager;
import connect.database.SharePreferenceUser;
import connect.database.green.DaoHelper.CurrencyHelper;
import connect.database.green.bean.CurrencyEntity;
import connect.ui.activity.R;
import connect.utils.DialogUtil;
import connect.utils.StringUtil;
import connect.utils.cryption.SupportKeyUril;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import connect.wallet.jni.AllNativeMethod;
import connect.widget.random.RandomVoiceActivity;

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

    private void checkAccount(OnWalletListener onWalletListener){
        this.onWalletListener = onWalletListener;
        WalletBean walletBean = SharePreferenceUser.getInstance().getWalletInfo();
        if(walletBean == null){
            // 用户没有钱包数据
            requestWalletBase();
        }else{
            // 用户已经拉取过钱包数据

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
        OkHttpUtil.getInstance().postEncrySelf("url", ByteString.copyFrom(new byte[]{}), new ResultCall() {
            @Override
            public void onResponse(Object response) {
                int a = 0;
                switch (a){
                    case 1:
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
                        CurrencyManage currencyManage = new CurrencyManage();
                        currencyManage.createCurrencyPri(MemoryDataManager.getInstance().getAddress(),
                                CurrencyType.BTC, new CurrencyManage.OnCreateCurrencyListener() {
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
            }

            @Override
            public void onError(Object response) {

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
    public void createWallet(final String baseSend, final CurrencyType type, String pass){
        final String baseSalt = AllNativeMethod.cdGetHash256(StringUtil.bytesToHexString(SecureRandom.getSeed(64)));
        final int n = 17;
        final String encoBaseSalt = SupportKeyUril.encodePri(baseSend,baseSalt,pass,n);
        OkHttpUtil.getInstance().postEncrySelf("url", ByteString.copyFrom(new byte[]{}), new ResultCall() {
            @Override
            public void onResponse(Object response) {
                WalletBean walletBean = new WalletBean();
                walletBean.setSalt(baseSalt);
                walletBean.setN(n);
                walletBean.setPayload(encoBaseSalt);

                SharePreferenceUser.getInstance().putWalletInfo(walletBean);

                CurrencyManage currencyManage = new CurrencyManage();
                currencyManage.createCurrency(baseSend, type, new CurrencyManage.OnCreateCurrencyListener() {
                    @Override
                    public void success(CurrencyEntity currencyEntity) {
                        onWalletListener.complete();
                    }

                    @Override
                    public void fail(String message) {

                    }
                });
            }

            @Override
            public void onError(Object response) {

            }
        });
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
    private void requestWalletInfo(String payload, String salt, int n, final OnWalletListener onWalletListener){
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
