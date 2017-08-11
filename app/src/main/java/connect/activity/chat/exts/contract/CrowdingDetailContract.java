package connect.activity.chat.exts.contract;

import java.util.List;

import connect.activity.base.contract.BasePresenter;
import connect.activity.base.contract.BaseView;
import protos.Connect;

/**
 * Created by Administrator on 2017/8/11.
 */

public interface CrowdingDetailContract {

    interface BView extends BaseView<CrowdingDetailContract.Presenter> {
void senderInfo(String avatar,String name);

        void showBalance(long balance);

        void showTips(String tips);

        void showCrowdingInfo(long total,long each,int state,String address);

        void showPaidInfo(String info);

        String getMessageId();

        void showCrowdingRecords(List<Connect.CrowdfundingRecord> records, boolean state);
    }

    interface Presenter extends BasePresenter {

        void requestCrowdingDetail(String hashid);

        void requestCrowdingPay(String hashid);
    }
}
