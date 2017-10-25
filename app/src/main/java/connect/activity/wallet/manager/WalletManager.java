package connect.activity.wallet.manager;

import android.text.TextUtils;
import android.widget.Toast;

import com.google.protobuf.ByteString;
import com.wallet.NativeWallet;
import com.wallet.bean.CurrencyBean;
import com.wallet.bean.CurrencyEnum;
import com.wallet.bean.EncryptionPinBean;
import com.wallet.currency.BaseCurrency;
import com.wallet.inter.WalletListener;

import java.text.DecimalFormat;
import java.util.List;

import connect.activity.base.BaseApplication;
import connect.activity.wallet.bean.WalletBean;
import connect.database.SharePreferenceUser;
import connect.database.SharedPreferenceUtil;
import connect.database.green.DaoHelper.CurrencyHelper;
import connect.database.green.bean.CurrencyAddressEntity;
import connect.database.green.bean.CurrencyEntity;
import connect.ui.activity.R;
import connect.utils.StringUtil;
import connect.utils.UriUtil;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import instant.utils.cryption.DecryptionUtil;
import instant.utils.cryption.SupportKeyUril;
import protos.Connect;
import wallet_gateway.WalletOuterClass;

/**
 * Wallet business layer management class
 */

public class WalletManager {

    private static boolean isShowWallet = true;
    private static WalletManager walletManager;
    public final double BTC_TO_LONG = Math.pow(10,8);
    public final String PATTERN_BTC = "##0.00000000";

    public static WalletManager getInstance() {
        if (walletManager == null) {
            synchronized (WalletManager.class) {
                if (walletManager == null) {
                    walletManager = new WalletManager();
                }
            }
        }
        return walletManager;
    }

    /**
     * According to create the wallet dialog
     * @param activity
     * @param status
     */
    /*public void showCreateWalletDialog(final Activity activity, final int status) {
        String massage = "";
        String okMassage = "";
        if (status == 1) {
            massage = activity.getString(R.string.Wallet_not_update_wallet);
            okMassage = activity.getString(R.string.Wallet_Immediately_update);
        } else if (status == 2) {
            massage = activity.getString(R.string.Wallet_not_create_wallet);
            okMassage = activity.getString(R.string.Wallet_Immediately_create);
        }
        DialogUtil.showAlertTextView(activity, activity.getString(R.string.Set_tip_title),
                massage, "", okMassage, false, new DialogUtil.OnItemClickListener() {
                    @Override
                    public void confirm(String value) {
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("type", CurrencyEnum.BTC);
                        bundle.putInt("status",status);
                        RandomVoiceActivity.startActivity(activity,bundle);
                    }

                    @Override
                    public void cancel() {
                        ActivityUtil.goBack(activity);
                    }
                });
    }*/

    /**
     * Synchronous wallet information
     * @param listener
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
                        listener.success(0);
                    } else {
                        // Users do not have to create a currency
                        requestUserStatus(listener);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Connect.HttpResponse response) {
                listener.fail(WalletListener.WalletError.NETError);
                Toast.makeText(BaseApplication.getInstance().getAppContext(), R.string.Wallet_synchronization_data_failed, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Get the user state
     * @param listener
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
                listener.fail(WalletListener.WalletError.NETError);
                Toast.makeText(BaseApplication.getInstance().getAppContext(), response.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Get voice after the random number seed, create the wallet and money
     * @param baseSend
     * @param pin
     * @param status
     * @param listener
     */
    public void createWallet(final String baseSend, final String pin, final int status, final WalletListener listener){
        requestWallet(baseSend, pin, new WalletListener<WalletBean>() {
            @Override
            public void success(WalletBean walletBean) {
                int category = 0;
                String value = "";
                if (status == 1) {
                    category = BaseCurrency.CATEGORY_PRIKEY;
                    value = SharedPreferenceUtil.getInstance().getUser().getPriKey();
                } else if (status == 2) {
                    category = BaseCurrency.CATEGORY_BASESEED;
                    value = baseSend;
                }
                NativeWallet.getInstance().createCurrency(CurrencyEnum.BTC, category, value, new WalletListener<CurrencyBean>() {
                    @Override
                    public void success(CurrencyBean currencyBean) {
                        String payload = "";
                        if(status == 1){
                            com.wallet.bean.EncryptionPinBean pinBean = NativeWallet.getInstance().encryptionPin(currencyBean.getType(),
                                    currencyBean.getPriKey(), pin);
                            payload = pinBean.getPayload();
                        }
                        createCurrency(CurrencyEnum.BTC, payload, currencyBean.getSalt(), currencyBean.getType(),
                                currencyBean.getMasterAddress(), new WalletListener<CurrencyEntity>() {
                                    @Override
                                    public void success(CurrencyEntity entity) {
                                        listener.success(entity);
                                    }

                                    @Override
                                    public void fail(WalletError error) {
                                        listener.fail(error);
                                    }
                                });
                    }

                    @Override
                    public void fail(WalletError error) {
                        listener.fail(error);
                    }
                });
            }

            @Override
            public void fail(WalletError error) {
                listener.fail(error);
            }
        });
    }

    /**
     * Create a wallet
     * @param baseSend
     * @param pin
     * @param listener
     */
    public void requestWallet(final String baseSend, final String pin, final WalletListener listener){
        WalletOuterClass.RequestWalletInfo.Builder builder = WalletOuterClass.RequestWalletInfo.newBuilder();
        final EncryptionPinBean encoPinBean = NativeWallet.getInstance().encryptionPin(BaseCurrency.CATEGORY_BASESEED,baseSend, pin);
        final String checkSum = StringUtil.cdHash256(SupportKeyUril.PIN_VERSION + "" + encoPinBean.getPayload());
        builder.setPayload(encoPinBean.getPayload());
        builder.setCheckSum(checkSum);
        builder.setVer(SupportKeyUril.PIN_VERSION);

        OkHttpUtil.getInstance().postEncrySelf(UriUtil.WALLET_V2_CREATE, builder.build(), new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                // Save wallet data
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
                listener.fail(WalletListener.WalletError.NETError);
            }
        });
    }

    /**
     * Add the currency
     * @param currencyEnum
     * @param payload
     * @param salt
     * @param category
     * @param masterAddress
     * @param listener
     */
    public void createCurrency(final CurrencyEnum currencyEnum, final String payload, final String salt,
                               final int category, final String masterAddress, final com.wallet.inter.WalletListener listener) {
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
                addressEntity.setAmount(0L);
                addressEntity.setLabel("");
                CurrencyHelper.getInstance().insertCurrencyAddress(addressEntity);

                listener.success(currencyEntity);
            }

            @Override
            public void onError(Connect.HttpResponse response) {
                listener.fail(com.wallet.inter.WalletListener.WalletError.NETError);
            }
        });
    }

    /**
     * Currency information
     * @param listener
     */
    public void requestCoinInfo(CurrencyEnum currencyEnum ,final WalletListener listener) {
        WalletOuterClass.Coin coin = WalletOuterClass.Coin.newBuilder()
                .setCurrency(currencyEnum.getCode())
                .build();
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.WALLET_V2_COINS_INFO, coin, new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                try {
                    Connect.IMResponse imResponse = Connect.IMResponse.parseFrom(response.getBody().toByteArray());
                    Connect.StructData structData = DecryptionUtil.decodeAESGCMStructData(imResponse.getCipherData());
                    WalletOuterClass.CoinsDetail coinsDetail = WalletOuterClass.CoinsDetail.parseFrom(structData.getPlainData());
                    CurrencyHelper.getInstance().insertCurrencyCoin(coinsDetail.getCoin());
                    listener.success(coinsDetail.getCoin());
                } catch (Exception e) {
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
     * To obtain the address list
     */
    public void requestAddress(CurrencyEnum currencyEnum, final WalletListener listener){
        WalletOuterClass.Coin.Builder builder = WalletOuterClass.Coin.newBuilder();
        builder.setCurrency(currencyEnum.getCode());
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.WALLET_V2_COINS_ADDRESS_LIST, builder.build(), new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                try {
                    Connect.IMResponse imResponse = Connect.IMResponse.parseFrom(response.getBody().toByteArray());
                    Connect.StructData structData = DecryptionUtil.decodeAESGCMStructData(imResponse.getCipherData());
                    WalletOuterClass.CoinsDetail coinsDetail = WalletOuterClass.CoinsDetail.parseFrom(structData.getPlainData());
                    List<WalletOuterClass.CoinInfo> list = coinsDetail.getCoinInfosList();
                    CurrencyHelper.getInstance().insertCurrencyAddressListCoinInfo(list, CurrencyEnum.BTC.getCode());
                    listener.success(list);
                } catch (Exception e) {
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
     * Set the currency information
     * @param payload
     * @param status
     * @param listener
     */
    public void setCurrencyInfo(String payload, Integer status, final WalletListener listener){
        WalletOuterClass.Coin.Builder builder = WalletOuterClass.Coin.newBuilder();
        if (!TextUtils.isEmpty(payload)) {
            builder.setPayload(payload);
        }
        if (status != null) {
            builder.setStatus(status);
        }
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.WALLET_V2_COINS_UPDATA, builder.build(), new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                try {
                    Connect.IMResponse imResponse = Connect.IMResponse.parseFrom(response.getBody().toByteArray());
                    Connect.StructData structData = DecryptionUtil.decodeAESGCMStructData(imResponse.getCipherData());
                    WalletOuterClass.CoinsDetail coinsDetail = WalletOuterClass.CoinsDetail.parseFrom(structData.getPlainData());
                    WalletOuterClass.Coin coin = coinsDetail.getCoin();
                    CurrencyHelper.getInstance().insertCurrencyCoin(coin);
                    listener.success(coin);
                } catch (Exception e) {
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
     * Update the wallet information
     * @param payload
     * @param listener
     */
    public void updateWallet(String payload, int ver, final WalletListener listener) {
        final WalletBean walletBean =  SharePreferenceUser.getInstance().getWalletInfo();
        walletBean.setPayload(payload);
        walletBean.setVer(ver);

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
                listener.fail(WalletListener.WalletError.NETError);
            }
        });
    }

    /**
     * Into a Long
     * @param value
     * @return
     */
    public long doubleToLongCurrency(double value){
        return Math.round(value * BTC_TO_LONG);
    }

    /**
     * Converted to Double type
     * @param value
     * @return
     */
    public String longToDoubleCurrency(long value) {
        DecimalFormat myFormat = new DecimalFormat(PATTERN_BTC);
        String format = myFormat.format(value / BTC_TO_LONG);
        return format.replace(",", ".");
    }

    /**
     * Determine whether to create the wallet
     * @return
     */
    public boolean isCreateWallet(){
        boolean isCreate;
        List<CurrencyEntity> currencyList = CurrencyHelper.getInstance().loadCurrencyList();
        if (currencyList == null || currencyList.size() == 0) {
            isCreate = false;
        } else {
            isCreate = true;
        }
        return isShowWallet && isCreate;
    }

    public static boolean isShowWallet(){
        return isShowWallet;
    }

}
