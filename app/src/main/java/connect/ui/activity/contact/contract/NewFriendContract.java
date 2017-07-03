package connect.ui.activity.contact.contract;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;

import connect.db.green.bean.FriendRequestEntity;
import connect.ui.base.BasePresenter;
import connect.ui.base.BaseView;

/**
 * Created by Administrator on 2017/4/19 0019.
 */

public interface NewFriendContract {

    interface View extends BaseView<NewFriendContract.Presenter> {
        Activity getActivity();

        void itemClick(int tag);

        void notifyData(int sizeRecommend,ArrayList<FriendRequestEntity> listFina);
    }

    interface Presenter extends BasePresenter {
        void initGrid(RecyclerView recycler);

        void updataRequestListRead();

        void queryFriend();

        void updataFriendRequest(FriendRequestEntity entity);

        void requestRecommendUser();
    }

}
