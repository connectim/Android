package connect.activity.chat.exts.contract;

import connect.activity.base.contract.BasePresenter;
import connect.activity.base.contract.BaseView;

/**
 * Created by Administrator on 2017/8/11.
 */

public interface HandleGroupRequestContract {

    interface BView extends BaseView<HandleGroupRequestContract.Presenter> {

        String getPubKey();

        void showGroupInfo(String avatar,String name,String summary,int member);

        void updateGroupRequest(int state);
    }

    interface Presenter extends BasePresenter {

        void requestGroupInfo();

        void agreeRequest(String caPublicKey,String code,String applyUid);

        void rejectRequest(String code,String applyUid);

        void groupChat();
    }
}
