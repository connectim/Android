package connect.wallet.cwallet;

import android.app.Activity;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.widget.Toast;

import com.google.protobuf.ByteString;

import java.util.List;

import connect.activity.base.BaseApplication;
import connect.activity.wallet.bean.WalletBean;
import connect.database.SharePreferenceUser;
import connect.database.green.DaoHelper.CurrencyHelper;
import connect.database.green.bean.CurrencyAddressEntity;
import connect.database.green.bean.CurrencyEntity;
import connect.ui.activity.R;
import connect.utils.DialogUtil;
import connect.utils.StringUtil;
import connect.utils.ToastUtil;
import connect.utils.UriUtil;
import connect.utils.cryption.DecryptionUtil;
import connect.utils.cryption.EncoPinBean;
import connect.utils.cryption.SupportKeyUril;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import connect.wallet.cwallet.bean.CurrencyEnum;
import connect.wallet.cwallet.inter.WalletListener;
import protos.Connect;
import wallet_gateway.WalletOuterClass;

import static connect.wallet.cwallet.inter.WalletListener.WalletError.NETError;

/**
 * Created by Administrator on 2017/7/18.
 */

public class BaseWallet {

    private String payPass;

    /**
     * Set the wallet password, modify the wallet password, you need to update the wallet under the synchronization interface, re encryption baseSsed upload
     */
    public void showSetNewPin(Activity mActivity,WalletListener listener) {
        payPass = "";
        setPin(mActivity,listener);
    }

    private void setPin(final Activity mActivity, final WalletListener listener) {
        Integer title;
        if (TextUtils.isEmpty(payPass)) {
            title = R.string.Set_Enter_new_password;
        } else {
            title = R.string.Wallet_Confirm_PIN;
        }
        DialogUtil.showPayEditView(mActivity, title, R.string.Wallet_Enter_4_Digits, new DialogUtil.OnItemClickListener() {
            @Override
            public void confirm(String value) {
                if (TextUtils.isEmpty(payPass)) {
                    payPass = value;
                    setPin(mActivity,listener);
                } else if (payPass.equals(value)) {
                    //Set password complete
                    listener.success(value);
                } else {
                    ToastUtil.getInstance().showToast(R.string.Login_Password_incorrect);
                }
            }

            @Override
            public void cancel() {

            }
        });
    }

    /**
     * check pwd
     */
    public void checkPwd(Activity activity,final String payload, final WalletListener listener) {
        DialogUtil.showPayEditView(activity, R.string.Wallet_Enter_your_PIN, R.string.Wallet_Enter_4_Digits, new DialogUtil.OnItemClickListener() {
            @Override
            public void confirm(final String value) {
                new AsyncTask<Void,Void,String>(){
                    @Override
                    protected String doInBackground(Void... params) {
                        String baseSeed = SupportKeyUril.decodePinDefult(payload, value);
                        if (TextUtils.isEmpty(baseSeed)) {
                            return "";
                        } else {
                            return baseSeed;
                        }
                    }

                    @Override
                    protected void onPostExecute(String baseSeed) {
                        super.onPostExecute(baseSeed);
                        if (TextUtils.isEmpty(baseSeed)) {
                            ToastUtil.getInstance().showToast(R.string.Login_Password_incorrect);
                        } else {
                            listener.success(baseSeed);
                        }
                    }
                }.execute();


            }

            @Override
            public void cancel() {

            }
        });
    }

    /**
     * create wallet
     */
    public void createWallet(String baseseed, String pwd, final WalletListener listener) {
        WalletOuterClass.RequestWalletInfo.Builder builder = WalletOuterClass.RequestWalletInfo.newBuilder();
        final EncoPinBean encoPinBean = SupportKeyUril.encoPinDefult(baseseed, pwd);
        final String checkSum = StringUtil.cdHash256(SupportKeyUril.PIN_VERSION + "" + encoPinBean.getPayload());
        builder.setPayload(encoPinBean.getPayload());
        builder.setCheckSum(checkSum);
        builder.setVer(SupportKeyUril.PIN_VERSION);

        OkHttpUtil.getInstance().postEncrySelf(UriUtil.WALLET_V2_CREATE, builder.build(), new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                WalletBean walletBean = new WalletBean();
                walletBean.setVer(SupportKeyUril.PIN_VERSION);
                walletBean.setPayload(encoPinBean.getPayload());
                walletBean.setCheckSum(checkSum);
                walletBean.setVersion(1);
                SharePreferenceUser.getInstance().putWalletInfo(walletBean);
                listener.success(walletBean);
            }

            @Override
            public void onError(Connect.HttpResponse response) {
                listener.fail(NETError);
            }
        });
    }

    /**
     * Update Wallet
     */
    public void updateWallet(final WalletBean walletBean, final WalletListener listener) {
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
                listener.success(walletBean);
            }

            @Override
            public void onError(Connect.HttpResponse response) {
                listener.fail(NETError);
            }
        });
    }

    /**
     * Get user status
     */
    public void requestUserStatus(final WalletListener listener) {
        WalletOuterClass.RequestUserInfo requestUserInfo = WalletOuterClass.RequestUserInfo.newBuilder()
                .setCurrency(CurrencyEnum.BTC.getCode())
                .build();
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.WALLET_V2_SERVICE_USER_STATUS, requestUserInfo, new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                try {
                    Connect.IMResponse imResponse = Connect.IMResponse.parseFrom(response.getBody().toByteArray());
                    Connect.StructData structData = DecryptionUtil.decodeAESGCMStructData(imResponse.getCipherData());
                    WalletOuterClass.CoinsDetail coinsDetail = WalletOuterClass.CoinsDetail.parseFrom(structData.getPlainData());
                    int category = coinsDetail.getCoin().getCategory();
                    listener.success(category);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Connect.HttpResponse response) {
                listener.fail(NETError);
                Toast.makeText(BaseApplication.getInstance().getAppContext(), response.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    /**
     * Update wallet information
     */
    public void syncWallet(final WalletListener listener) {
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.WALLET_V2_SYNC, ByteString.copyFrom(new byte[]{}), new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                try {
                    Connect.IMResponse imResponse = Connect.IMResponse.parseFrom(response.getBody().toByteArray());
                    Connect.StructData structData = DecryptionUtil.decodeAESGCMStructData(imResponse.getCipherData());
                    WalletOuterClass.RespSyncWallet respSyncWallet = WalletOuterClass.RespSyncWallet.parseFrom(structData.getPlainData());
                    if (respSyncWallet.getCoinsList() != null && respSyncWallet.getCoinsList().size() > 0) {
                        // Update wallet data
                        WalletOuterClass.Wallet wallet = respSyncWallet.getWallet();
                        WalletBean walletBean = new WalletBean(wallet.getPayLoad(), wallet.getVer(),
                                wallet.getVersion(), wallet.getCheckSum());
                        //Save wallet account information
                        SharePreferenceUser.getInstance().putWalletInfo(walletBean);
                        List<WalletOuterClass.Coin> list = respSyncWallet.getCoinsList();
                        //Save currency information
                        CurrencyHelper.getInstance().insertCurrencyListCoin(list);
                        listener.success(list);
                    } else {
                        listener.success(null);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Connect.HttpResponse response) {
                listener.fail(NETError);
                Toast.makeText(BaseApplication.getInstance().getAppContext(),
                        R.string.Wallet_synchronization_data_failed,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * create wallet
     * @param category 1:Pure private key，2:baseSeed，3:salt+seed
     */
    public void createCurrency(final CurrencyEnum currencyEnum, final String payload, final String salt,
                               final int category, final String masterAddress, final WalletListener listener){
        WalletOuterClass.CreateCoinRequest.Builder builder = WalletOuterClass.CreateCoinRequest.newBuilder();
        builder.setSalt(salt);
        builder.setCurrency(currencyEnum.getCode());
        builder.setCategory(category);
        builder.setPayload(payload);
        builder.setMasterAddress(masterAddress);
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.WALLET_V2_COINS_CREATE, builder.build(), new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                // Save currency information
                CurrencyEntity currencyEntity = new CurrencyEntity();
                currencyEntity.setSalt(salt);
                currencyEntity.setBalance(0L);
                currencyEntity.setCurrency(currencyEnum.getCode());
                currencyEntity.setCategory(category);
                currencyEntity.setMasterAddress(masterAddress);
                currencyEntity.setPayload(payload);
                currencyEntity.setStatus(1);
                currencyEntity.setAmount(0L);
                CurrencyHelper.getInstance().insertCurrency(currencyEntity);

                // Save default currency address information
                CurrencyAddressEntity addressEntity = new CurrencyAddressEntity();
                addressEntity.setBalance(0L);
                addressEntity.setStatus(1);
                addressEntity.setCurrency(currencyEnum.getCode());
                addressEntity.setAddress(masterAddress);
                addressEntity.setIndex(0);
                currencyEntity.setAmount(0L);
                addressEntity.setLabel("");
                CurrencyHelper.getInstance().insertCurrencyAddress(addressEntity);

                listener.success(currencyEntity);
            }

            @Override
            public void onError(Connect.HttpResponse response) {
                listener.fail(WalletListener.WalletError.NETError);
            }
        });
    }

}
