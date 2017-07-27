package connect.wallet.cwallet.account;

import com.google.protobuf.GeneratedMessageV3;

import java.util.List;

import connect.database.green.DaoHelper.CurrencyHelper;
import connect.database.green.bean.CurrencyEntity;
import connect.utils.UriUtil;
import connect.utils.cryption.DecryptionUtil;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import connect.wallet.cwallet.bean.CurrencyEnum;
import connect.wallet.cwallet.inter.WalletListener;
import protos.Connect;
import wallet_gateway.WalletOuterClass;

/**
 * BTC 币种账户
 * Created by Administrator on 2017/7/18.
 */

public class BtcCoinAccount implements CoinAccount {

    @Override
    public void balance() {

    }

    @Override
    public void hideAddress(String address) {

    }

    @Override
    public void requestAddressList(final WalletListener listener) {
        CurrencyEntity currencyEntity = CurrencyHelper.getInstance().loadCurrency(CurrencyEnum.BTC.getCode());
        if (currencyEntity == null) {
            return;
        }

        WalletOuterClass.Coin.Builder builder = WalletOuterClass.Coin.newBuilder();
        builder.setCurrency(CurrencyEnum.BTC.getCode());
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.WALLET_V2_COINS_ADDRESS_LIST, builder.build(), new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                try {
                    Connect.IMResponse imResponse = Connect.IMResponse.parseFrom(response.getBody().toByteArray());
                    Connect.StructData structData = DecryptionUtil.decodeAESGCMStructData(imResponse.getCipherData());
                    WalletOuterClass.CoinsDetail coinsDetail = WalletOuterClass.CoinsDetail.parseFrom(structData.getPlainData());
                    List<WalletOuterClass.CoinInfo> list = coinsDetail.getCoinInfosList();
                    CurrencyHelper.getInstance().insertCurrencyAddressListCoinInfo(list, CurrencyEnum.BTC.getCode());

                    WalletOuterClass.Coin.Builder coinBuilder = WalletOuterClass.Coin.newBuilder();
                    coinBuilder.setSalt(coinsDetail.getCoin().getSalt());
                    coinBuilder.setCurrency(coinsDetail.getCoin().getCurrency());
                    coinBuilder.setCategory(coinsDetail.getCoin().getCategory());
                    coinBuilder.setPayload(coinsDetail.getCoin().getPayload());
                    coinBuilder.setStatus(coinsDetail.getCoin().getStatus());
                    for (WalletOuterClass.CoinInfo coinInfo : list) {
                        coinBuilder.setAmount(coinBuilder.getAmount() + coinInfo.getAmount());
                        coinBuilder.setBalance(coinBuilder.getBalance() + coinInfo.getBalance());
                    }

                    WalletOuterClass.Coin localCoin = coinBuilder.build();
                    CurrencyHelper.getInstance().insertCurrencyCoin(localCoin);
                    listener.success(localCoin);
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

    @Override
    public void transfer(String url, GeneratedMessageV3 body, WalletListener listener) {

    }
}
