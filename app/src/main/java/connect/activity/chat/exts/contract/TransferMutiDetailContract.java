package connect.activity.chat.exts.contract;

import connect.activity.base.contract.BasePresenter;
import connect.activity.base.contract.BaseView;

/**
 * Created by Administrator on 2017/8/11.
 */

public interface TransferMutiDetailContract {

    interface BView extends BaseView<TransferMutiDetailContract.Presenter> {

        void showTransferDetail(String sender,String[] receivers,String tips,long amount,int transferstate,long createtime);

            void showSenderInfo(String avatar,String name);
    }

    interface Presenter extends BasePresenter {

        void requestTransferDetail(String hashid);

        void requestSenderInfo(String address);

        String getTransferTxtid();
    }
}
