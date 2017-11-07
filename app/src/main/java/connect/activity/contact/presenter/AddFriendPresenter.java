package connect.activity.contact.presenter;

import android.os.AsyncTask;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import java.util.ArrayList;

import connect.activity.contact.bean.ContactNotice;
import connect.activity.contact.contract.AddFriendContract;
import connect.activity.home.bean.WalletMenuBean;
import connect.activity.wallet.adapter.WalletMenuAdapter;
import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.bean.FriendRequestEntity;
import connect.ui.activity.R;
import connect.utils.UriUtil;
import connect.utils.cryption.DecryptionUtil;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import protos.Connect;

public class AddFriendPresenter implements AddFriendContract.Presenter{

    private AddFriendContract.View mView;
    private Connect.UsersInfoBase usersInfoBase;

    public AddFriendPresenter(AddFriendContract.View mView) {
        this.mView = mView;
        mView.setPresenter(this);
    }

    @Override
    public void start() {}

    @Override
    public void initGrid(RecyclerView recycler) {
        ArrayList<WalletMenuBean> menuList = new ArrayList<>();
        menuList.add(new WalletMenuBean(R.mipmap.contract_add_scan3x, R.string.Link_Scan));
        menuList.add(new WalletMenuBean(R.mipmap.contract_add_contacts3x, R.string.Link_Contacts));
        menuList.add(new WalletMenuBean(R.mipmap.contract_add_more3x, R.string.Link_More));

        WalletMenuAdapter walletMenuAdapter = new WalletMenuAdapter(menuList, mView.getActivity());
        recycler.setLayoutManager(new GridLayoutManager(mView.getActivity(), 3));
        recycler.setAdapter(walletMenuAdapter);
        walletMenuAdapter.setOnItemClickListener(new WalletMenuAdapter.OnItemClickListener() {
            @Override
            public void itemClick(int position) {
                mView.itemClick(position);
            }
        });
    }

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
                                usersInfoBase = Connect.UsersInfoBase.parseFrom(structData.getPlainData());
                                /*ArrayList<Connect.UserInfoBase> list = new ArrayList<>();
                                for(Connect.UserInfo userInfo : usersInfo.getUsersList()){
                                    if(ProtoBufUtil.getInstance().checkProtoBuf(userInfo)){
                                        list.add(userInfo);
                                    }
                                }
                                ContactHelper.getInstance().inserRecommendEntity(list);*/
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
                if(usersInfoBase != null){
                    for(Connect.UserInfoBase userInfoBase : usersInfoBase.getUsersList()){
                        FriendRequestEntity requestEntity = new FriendRequestEntity();
                        requestEntity.setUid(userInfoBase.getUid());
                        requestEntity.setAvatar(userInfoBase.getAvatar());
                        requestEntity.setUsername(userInfoBase.getUsername());
                        requestEntity.setStatus(4);
                        listFinal.add(requestEntity);
                        if(listFinal.size() == 4)
                            break;
                    }
                }
                listFinal.addAll(ContactHelper.getInstance().loadFriendRequest());
                return listFinal;
            }

            @Override
            protected void onPostExecute(ArrayList<FriendRequestEntity> list) {
                super.onPostExecute(list);
                boolean isShowMoreRecommend = false;
                if(usersInfoBase != null){
                    isShowMoreRecommend = usersInfoBase.getUsersList().size() > 4 ? true : false;
                }
                mView.notifyData(isShowMoreRecommend, list);
            }
        }.execute();
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

    /**
     * Add friends success, update the database
     */
    /*@Override
    public void updateRequestStatus(final FriendRequestEntity entity, int status) {
        entity.setRead(1);
        entity.setStatus(status);
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                ContactHelper.getInstance().inserFriendQuestEntity(entity);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                queryFriend();
                ContactNotice.receiverAddFriend();
            }
        }.execute();
    }*/

}
