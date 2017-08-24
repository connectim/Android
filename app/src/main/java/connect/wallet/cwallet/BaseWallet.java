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
import connect.database.SharedPreferenceUtil;
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
import connect.wallet.cwallet.bean.PinBean;
import connect.wallet.cwallet.currency.BaseCurrency;
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
     * Set the wallet password
     */
    public void showSetNewPin(Activity mActivity,WalletListener listener) {
        payPass = "";
        setPin(mActivity,listener);
    }

    private void setPin(final Activity mActivity, final WalletListener listener) {
        Integer title;
        if (TextUtils.isEmpty(payPass)) {
            title = R.string.Set_Set_Payment_Password;
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
                    showSetNewPin(mActivity,listener);
                    ToastUtil.getInstance().showToast(R.string.Wallet_Payment_Password_do_not_match);
                }
            }

            @Override
            public void cancel() {

            }
        });
    }

    /**
     * Check the password
     */
    public void checkPwd(Activity activity, final String payload, final WalletListener listener) {
        DialogUtil.showPayEditView(activity, R.string.Wallet_Enter_your_PIN, R.string.Wallet_Enter_4_Digits, new DialogUtil.OnItemClickListener() {
            @Override
            public void confirm(final String value) {
                new AsyncTask<Void,Void,PinBean>(){
                    @Override
                    protected PinBean doInBackground(Void... params) {
                        String baseSeed = SupportKeyUril.decodePinDefult(BaseCurrency.CATEGORY_BASESEED,payload, value);
                        if (TextUtils.isEmpty(baseSeed)) {
                            return null;
                        } else {
                            PinBean pinBean = new PinBean();
                            pinBean.setPin(value);
                            pinBean.setBaseSeed(baseSeed);
                            return pinBean;
                        }
                    }

                    @Override
                    protected void onPostExecute(PinBean pinBean) {
                        super.onPostExecute(pinBean);
                        if (pinBean == null) {
                            ToastUtil.getInstance().showToast(R.string.Login_Password_incorrect);
                        } else {
                            listener.success(pinBean);
                        }
                    }
                }.execute();
            }
            @Override
            public void cancel() {}
        });
    }

    /**
     * create wallet
     */
    public void createWallet(String baseSeed, String pwd, final WalletListener listener) {
        WalletOuterClass.RequestWalletInfo.Builder builder = WalletOuterClass.RequestWalletInfo.newBuilder();
        final EncoPinBean encoPinBean = SupportKeyUril.encoPinDefult(BaseCurrency.CATEGORY_BASESEED,baseSeed, pwd);
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
     * Update the wallet
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
     * Get the user identity status
     */
    public void requestUserStatus(final WalletListener listener) {
        WalletOuterClass.RequestUserInfo requestUserInfo = WalletOuterClass.RequestUserInfo.newBuilder()
                .setCurrency(CurrencyEnum.BTC.getCode())
                .setUid(SharedPreferenceUtil.getInstance().getUser().getPubKey())
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
                        // Save the wallet information
                        WalletOuterClass.Wallet wallet = respSyncWallet.getWallet();
                        WalletBean walletBean = new WalletBean(wallet.getPayload(), wallet.getVer(),
                                wallet.getVersion(), wallet.getCheckSum());
                        // Save the currency information
                        SharePreferenceUser.getInstance().putWalletInfo(walletBean);
                        List<WalletOuterClass.Coin> list = respSyncWallet.getCoinsList();
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
     * Create a currency
     *
     * @param currencyEnum
     * @param payload
     * @param salt
     * @param category 1:prikey，2:baseSeed，3:salt+seed
     * @param masterAddress
     * @param listener
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
                // Save the currency information
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

                // Save the address information
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
