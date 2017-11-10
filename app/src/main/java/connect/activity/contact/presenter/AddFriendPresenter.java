package connect.activity.contact.presenter;

import android.os.AsyncTask;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import java.util.ArrayList;

import connect.activity.contact.bean.ContactNotice;
import connect.activity.contact.contract.AddFriendContract;
import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.bean.FriendRequestEntity;
import connect.utils.UriUtil;
import connect.utils.cryption.DecryptionUtil;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import protos.Connect;

public class AddFriendPresenter implements AddFriendContract.Presenter{

    private AddFriendContract.View mView;
    private ArrayList<FriendRequestEntity> listRecommend = new ArrayList<>();

    public AddFriendPresenter(AddFriendContract.View mView) {
        this.mView = mView;
        mView.setPresenter(this);
    }

    @Override
    public void start() {}

    /**
     * Get recommended friends
     */
    @Override
    public void requestRecommendUser() {
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.CONNEXT_V1_USERS_RECOMMEND, ByteString.copyFrom(new byte[]{}),
                new ResultCall<Connect.HttpResponse>() {
                    @Override
                    public void onResponse(Connect.HttpResponse response) {
                        try {
                            Connect.IMResponse imResponse = Connect.IMResponse.parseFrom(response.getBody().toByteArray());
                            Connect.StructData structData = DecryptionUtil.decodeAESGCMStructData(imResponse.getCipherData());
                            if(structData != null){
                                Connect.UsersInfoBase usersInfoBase = Connect.UsersInfoBase.parseFrom(structData.getPlainData());
                                for(Connect.UserInfoBase userInfoBase : usersInfoBase.getUsersList()){
                                    FriendRequestEntity requestEntity = new FriendRequestEntity();
                                    requestEntity.setUid(userInfoBase.getUid());
                                    requestEntity.setAvatar(userInfoBase.getAvatar());
                                    requestEntity.setUsername(userInfoBase.getUsername());
                                    requestEntity.setStatus(4);
                                    listRecommend.add(requestEntity);
                                    if(listRecommend.size() == 4)
                                        break;
                                }
                            }
                            queryFriend();
                        } catch (InvalidProtocolBufferException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Connect.HttpResponse response) {}
                });
    }

    /**
     * Get local recommended buddy and buddy request data
     */
    @Override
    public void queryFriend() {
        new AsyncTask<Void, Void, ArrayList<FriendRequestEntity>>() {
            @Override
            protected ArrayList<FriendRequestEntity> doInBackground(Void... params) {
                ArrayList<FriendRequestEntity> listFinal = new ArrayList<>();
                listFinal.addAll(listRecommend);
                listFinal.addAll(ContactHelper.getInstance().loadFriendRequest());
                return listFinal;
            }

            @Override
            protected void onPostExecute(ArrayList<FriendRequestEntity> list) {
                super.onPostExecute(list);
                mView.notifyData(listRecommend.size() >= 4 ? true : false, list);
            }
        }.execute();
    }

    @Override
    public void requestNoInterest(final String uid) {
        Connect.NOInterest noInterest = Connect.NOInterest.newBuilder()
                .setUid(uid)
                .build();
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.CONNEXT_V1_USERS_DISINCLINE, noInterest, new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                for(FriendRequestEntity entity : listRecommend){
                    if(entity.getUid().equals(uid)){
                        listRecommend.remove(entity);
                        break;
                    }
                }
                queryFriend();
            }

            @Override
            public void onError(Connect.HttpResponse response) {}
        });
    }

    /**
     * Set all buddy requests not read
     */
    @Override
    public void updateRequestListStatus() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                ContactHelper.getInstance().updataFriendRequestList();
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                ContactNotice.receiverAddFriend();
            }
        }.execute();
    }

}
