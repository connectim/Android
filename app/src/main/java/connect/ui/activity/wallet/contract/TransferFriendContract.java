package connect.ui.activity.wallet.contract;

import android.app.Activity;
import android.content.Intent;
import android.widget.AdapterView;
import android.widget.GridView;

import java.util.List;

import connect.db.green.bean.ContactEntity;
import connect.ui.base.BasePresenter;
import connect.ui.base.BaseView;
import connect.view.payment.PaymentPwd;
import connect.view.transferEdit.TransferEditView;

/**
 * Created by Administrator on 2017/4/18 0018.
 */

public interface TransferFriendContract {

    interface View extends BaseView<TransferFriendContract.Presenter> {
        Activity getActivity();

        void addTranferFriend();

        void setPayFee();

        String getCurrentBtc();

        void setBtnEnabled(boolean isEnable);
    }

    interface Presenter extends BasePresenter {
        AdapterView.OnItemClickListener getItemClickListener();

        TransferEditView.OnEditListener getOnEditListener();

        void horizontal_layout(GridView gridView);

        void setListData(List<ContactEntity> list);

        List<ContactEntity> getListFriend();

        void checkBtnEnable();

        void requestSend(long amount, String samValue ,String note,PaymentPwd paymentPwd);
    }

}
