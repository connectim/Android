package connect.wallet.cwallet.account;

import java.util.List;

import connect.database.green.DaoHelper.CurrencyHelper;
import connect.utils.UriUtil;
import connect.utils.cryption.DecryptionUtil;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import connect.wallet.cwallet.bean.CurrencyEnum;
import connect.wallet.cwallet.inter.WalletListener;
import protos.Connect;
import wallet_gateway.WalletOuterClass;

/**
 * BTC Currency account
 */
public class BtcCoinAccount implements CoinAccount {

    @Override
    public void balance() {}

    @Override
    public void hideAddress(String address) {}

    @Override
    public void requestAddressList(final WalletListener listener) {
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

}
