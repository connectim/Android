package connect.ui.activity.contact.presenter;

import com.google.protobuf.InvalidProtocolBufferException;

import connect.db.MemoryDataManager;
import connect.db.SharedPreferenceUtil;
import connect.db.green.DaoHelper.ContactHelper;
import connect.db.green.DaoHelper.ConversionHelper;
import connect.db.green.bean.ContactEntity;
import connect.db.green.bean.ConversionEntity;
import connect.ui.activity.R;
import connect.ui.activity.contact.bean.ContactNotice;
import connect.ui.activity.contact.bean.MsgSendBean;
import connect.ui.activity.contact.contract.FriendInfoContract;
import connect.ui.activity.home.bean.MsgFragmReceiver;
import connect.ui.activity.home.bean.MsgNoticeBean;
import connect.utils.ActivityUtil;
import connect.utils.ProtoBufUtil;
import connect.utils.ToastEUtil;
import connect.utils.ToastUtil;
import connect.utils.UriUtil;
import connect.utils.cryption.DecryptionUtil;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import protos.Connect;

/**
 * Created by Administrator on 2017/4/19 0019.
 */

public class FriendInfoPresenter implements FriendInfoContract.Presenter {

    private FriendInfoContract.View mView;

    public FriendInfoPresenter(FriendInfoContract.View mView) {
        this.mView = mView;
    }

    @Override
    public void start() {

    }

    @Override
    public void checkOnEvent(MsgNoticeBean notice) {
        Object[] objs = null;
        if (notice.object != null) {
            objs = (Object[]) notice.object;
        }
        MsgSendBean sendBean = (MsgSendBean) objs[0];
        switch (notice.ntEnum) {
            case MSG_SEND_SUCCESS:
                if (sendBean.getType() == MsgSendBean.SendType.TypeAddFavorites) {
                    mView.setCommon(sendBean.getCommon());
                } else if (sendBean.getType() == MsgSendBean.SendType.TypeDeleteFriend) {
                    ContactHelper.getInstance().deleteEntity(sendBean.getAddress());
                    ContactHelper.getInstance().deleteRequestEntity(sendBean.getPubkey());
                    ContactHelper.getInstance().removeFriend(sendBean.getPubkey());
                    ContactNotice.receiverContact();
                    ToastEUtil.makeText(mView.getActivity(), R.string.Link_Delete_Successful).show();
                    ActivityUtil.goBack(mView.getActivity());
                }
                break;
            case MSG_SEND_FAIL:
                Integer errorCode = (Integer) objs[1];
                if (sendBean.getType() == MsgSendBean.SendType.TypeDeleteFriend) {
                    ToastEUtil.makeText(mView.getActivity(),R.string.Link_Delete_Failed,ToastEUtil.TOAST_STATUS_FAILE).show();
                    ContactHelper.getInstance().deleteEntity(sendBean.getAddress());
                    ContactHelper.getInstance().deleteRequestEntity(sendBean.getPubkey());
                    ContactHelper.getInstance().removeFriend(sendBean.getPubkey());
                    ContactNotice.receiverContact();
                    ActivityUtil.goBack(mView.getActivity());
                } else {
                    ToastUtil.getInstance().showToast(errorCode + "");
                }
                break;
        }
    }

    @Override
    public void requestUserInfo(String address, final ContactEntity friendEntity) {
        final Connect.SearchUser searchUser = Connect.SearchUser.newBuilder()
                .setCriteria(address)
                .build();
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.CONNECT_V1_USER_SEARCH, searchUser, new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                try {
                    Connect.IMResponse imResponse = Connect.IMResponse.parseFrom(response.getBody().toByteArray());
                    Connect.StructData structData = DecryptionUtil.decodeAESGCMStructData(imResponse.getCipherData());
                    Connect.UserInfo userInfo = Connect.UserInfo.parseFrom(structData.getPlainData());
                    if(!ProtoBufUtil.getInstance().checkProtoBuf(userInfo)){
                        return;
                    }
                    if (friendEntity.getAvatar().equals(userInfo.getAvatar()) && friendEntity.getUsername().equals(userInfo.getUsername())) {
                        return;
                    }
                    friendEntity.setUsername(userInfo.getUsername());
                    friendEntity.setAvatar(userInfo.getAvatar());
                    mView.updataView(friendEntity);
                    ContactHelper.getInstance().insertContact(friendEntity);
                    ConversionEntity roomEntity = ConversionHelper.getInstance().loadRoomEnitity(friendEntity.getPub_key());
                    if (roomEntity != null) {
                        MsgFragmReceiver.refreshRoom();
                    }
                } catch (InvalidProtocolBufferException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Connect.HttpResponse response) {

            }
        });
    }

    @Override
    public void requestBlock(final boolean block,String address) {
        Connect.UserIdentifier userIdentifier = Connect.UserIdentifier.newBuilder()
                .setAddress(address)
                .build();
        String url;
        if (block) {
            url = UriUtil.CONNEXT_V1_BLACKLIST;
        } else {
            url = UriUtil.CONNEXT_V1_BLACKLIST_REMOVE;
        }
        OkHttpUtil.getInstance().postEncrySelf(url, userIdentifier, new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                mView.setBlock(block);
            }

            @Override
            public void onError(Connect.HttpResponse response) {

            }
        });
    }
}
