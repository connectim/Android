package connect.ui.activity.wallet.contract;

import android.app.Activity;
import android.text.TextWatcher;

import connect.ui.activity.wallet.bean.SendOutBean;
import connect.ui.base.BasePresenter;
import connect.ui.base.BaseView;
import connect.view.payment.PaymentPwd;
import connect.utils.transfer.TransferEditView;
import protos.Connect;

/**
 * Created by Administrator on 2017/4/18 0018.
 */

public interface PacketContract {

    interface View extends BaseView<PacketContract.Presenter> {
        Activity getActivity();

        String getCurrentBtc();

        void setPayBtnEnable(boolean isEnable);

        String getPacketNumber();

        void setPayFee();

        void goinPacketSend(SendOutBean sendOutBean);
    }

    interface Presenter extends BasePresenter {
        TextWatcher getNumberWatcher();

        TransferEditView.OnEditListener getEditListener();

        void sendPacket(long amount, String siginRaw, String note, PaymentPwd paymentPwd);

        Connect.PendingRedPackage getPendingPackage();
    }

}
