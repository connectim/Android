package connect.activity.chat.exts.presenter;

import connect.activity.chat.exts.contract.PaymentDetailContract;

/**
 * Created by Administrator on 2017/8/11.
 */

public class PaymentDetailPresenter implements PaymentDetailContract.Presenter{

    private PaymentDetailContract.BView view;

    public PaymentDetailPresenter(PaymentDetailContract.BView view) {
        this.view = view;
        view.setPresenter(this);
    }

    @Override
    public void start() {

    }
}
