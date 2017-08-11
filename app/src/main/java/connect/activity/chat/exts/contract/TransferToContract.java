package connect.activity.chat.exts.contract;

import connect.activity.base.contract.BasePresenter;
import connect.activity.base.contract.BaseView;
import connect.activity.chat.exts.TransferToActivity;
import connect.database.green.bean.ContactEntity;

/**
 * Created by Administrator on 2017/8/10.
 */

public interface TransferToContract {

    interface BView extends BaseView<TransferToContract.Presenter> {

        String getPubkey();

        String getTransferAddress();

        void showTransferInfo(String avatar,String tansferinfo);

        long getCurrentAmount();

        String getTransferNote();

        TransferToActivity.TransferType getTransType();
    }

    interface Presenter extends BasePresenter {

        void requestSingleTransfer(Long currentlong);

    }
}
