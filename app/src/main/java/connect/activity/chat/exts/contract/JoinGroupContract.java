package connect.activity.chat.exts.contract;

import connect.activity.base.contract.BasePresenter;
import connect.activity.base.contract.BaseView;
import protos.Connect;

/**
 * Created by puin on 17-8-11.
 */

public interface JoinGroupContract {

    interface BView extends BaseView<JoinGroupContract.Presenter> {

        void showTokenInfo(String groupkey, Connect.GroupInfoBase infoBase);

        void showGroupkeyInfo(Connect.GroupInfoBase infoBase);

        void showLinkInfo(Connect.GroupInfoBase infoBase);

        void showFailInfo();

    }

    interface Presenter extends BasePresenter {

        void requestByToken(String token);

        void requestByGroupkey(String groupkey);

        void requestByLink(String groupkey,String hash);

        void requestJoinByInvite(String groupkey,String inviteby,String tips,String token);

        void requestJoinByLink(String groupkey,String hash,String tips,int source);
    }
}
