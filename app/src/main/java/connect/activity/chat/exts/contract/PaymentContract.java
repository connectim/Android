package connect.activity.chat.exts.contract;

import connect.activity.base.contract.BasePresenter;
import connect.activity.base.contract.BaseView;
import connect.wallet.cwallet.bean.CurrencyEnum;

/**
 * Created by puin on 17-8-11.
 */

public interface PaymentContract {

    interface BView extends BaseView<Presenter> {

        String getPubkey();

        void showPayment(String avatar,String name);

        void showCrowding(int count);
    }

    interface Presenter extends BasePresenter {

        void loadPayment(String pubkey);

        void loadCrowding(String pubkey);

        void requestPayment(CurrencyEnum currencyEnum,long amount,String tips);

        void requestCrowding(CurrencyEnum currencyEnum,long amount,int size,String tips);
    }

}
