package connect.activity.contact.contract;

import android.app.Activity;

import java.util.ArrayList;

import connect.activity.base.contract.BasePresenter;
import connect.activity.base.contract.BaseView;
import connect.database.green.bean.FriendRequestEntity;

public interface AddFriendContract {

    interface View extends BaseView<AddFriendContract.Presenter> {
        Activity getActivity();

        void notifyData(boolean isShowMoreRecommend, ArrayList<FriendRequestEntity> listFina);
    }

    interface Presenter extends BasePresenter {
        void updateRequestListStatus();

        void queryFriend();

        void requestRecommendUser();

        void requestNoInterest(String uid);
    }

}
