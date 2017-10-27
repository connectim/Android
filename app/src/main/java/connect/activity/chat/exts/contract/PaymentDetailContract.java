package connect.activity.chat.exts.contract;

import connect.activity.base.contract.BasePresenter;
import connect.activity.base.contract.BaseView;
import protos.Connect;

/**
 * Created by Administrator on 2017/8/11.
 */

public interface PaymentDetailContract {

    interface BView extends BaseView<PaymentDetailContract.Presenter> {

        void showPaymentDetail(Connect.Bill bill);
    }

    interface Presenter extends BasePresenter {

        void requestPaymentDetail(String hashid);
    }
}
