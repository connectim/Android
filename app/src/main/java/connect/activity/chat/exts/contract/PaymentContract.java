package connect.activity.chat.exts.contract;


import connect.activity.base.contract.BasePresenter;
import connect.activity.base.contract.BaseView;
import protos.Connect;

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
    }

}
