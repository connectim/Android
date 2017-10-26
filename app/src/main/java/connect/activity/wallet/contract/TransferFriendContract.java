package connect.activity.wallet.contract;

import android.app.Activity;
import android.widget.AdapterView;
import android.widget.GridView;

import java.util.List;

import connect.database.green.bean.ContactEntity;
import connect.activity.base.contract.BasePresenter;
import connect.activity.base.contract.BaseView;
import connect.activity.wallet.view.TransferEditView;

public interface TransferFriendContract {

    interface View extends BaseView<TransferFriendContract.Presenter> {
        Activity getActivity();

        void addTransferFriend();

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

        void sendTransferMessage(String hashid, String address,String note);
    }

}
