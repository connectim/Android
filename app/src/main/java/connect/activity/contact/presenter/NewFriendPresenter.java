package connect.activity.contact.presenter;

import android.os.AsyncTask;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import java.util.ArrayList;
import java.util.List;

import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.bean.FriendRequestEntity;
import connect.database.green.bean.RecommandFriendEntity;
import connect.ui.activity.R;
import connect.activity.contact.bean.ContactNotice;
import connect.activity.contact.contract.NewFriendContract;
import connect.activity.home.bean.WalletMenuBean;
import connect.activity.wallet.adapter.WalletMenuAdapter;
import connect.utils.ProtoBufUtil;
import connect.utils.UriUtil;
import connect.utils.cryption.DecryptionUtil;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import protos.Connect;

/**
 * Created by Administrator on 2017/4/19 0019.
 */

public class NewFriendPresenter implements NewFriendContract.Presenter{

    private NewFriendContract.View mView;
    private ArrayList<FriendRequestEntity> listRecommend = new ArrayList<>();
    private ArrayList<FriendRequestEntity> listRuquest = new ArrayList<>();
    private final int MAX_RECOMMEND_COUNT = 4;

    public NewFriendPresenter(NewFriendContract.View mView) {
        this.mView = mView;
        mView.setPresenter(this);
    }

    @Override
    public void start() {

    }

    @Override
    public void initGrid(RecyclerView recycler) {
        ArrayList<WalletMenuBean> menuList = new ArrayList<>();
        menuList.add(new WalletMenuBean(R.mipmap.contract_add_scan3x, R.string.Link_Scan));
        menuList.add(new WalletMenuBean(R.mipmap.contract_add_contacts3x, R.string.Link_Contacts));
        menuList.add(new WalletMenuBean(R.mipmap.contract_add_more3x, R.string.Link_More));

        WalletMenuAdapter walletMenuAdapter = new WalletMenuAdapter(menuList, mView.getActivity());
        recycler.setLayoutManager(new GridLayoutManager(mView.getActivity(), 3));
        recycler.setAdapter(walletMenuAdapter);
        walletMenuAdapter.setOnItemClickListence(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int tag = (Integer) v.getTag();
                mView.itemClick(tag);
            }
        });
    }

    @Override
    public void updataRequestListRead() {
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

    @Override
    public void updataFriendRequest(final FriendRequestEntity entity) {
        entity.setRead(1);
        entity.setStatus(2);
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
            }
        }.execute();
    }

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
                                Connect.UsersInfo usersInfo = Connect.UsersInfo.parseFrom(structData.getPlainData());
                                ArrayList<Connect.UserInfo> list = new ArrayList<>();
                                for(Connect.UserInfo userInfo : usersInfo.getUsersList()){
                                    if(ProtoBufUtil.getInstance().checkProtoBuf(userInfo)){
                                        list.add(userInfo);
                                    }
                                }
                                ContactHelper.getInstance().inserRecommendEntity(list);
                            }
                            queryFriend();
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
    public void queryFriend() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                listRecommend.clear();
                List<RecommandFriendEntity> list = ContactHelper.getInstance().loadRecommendEntity(1,MAX_RECOMMEND_COUNT);
                for(RecommandFriendEntity recommendEntity : list){
                    FriendRequestEntity requestEntity = new FriendRequestEntity();
                    requestEntity.setPub_key(recommendEntity.getPub_key());
                    requestEntity.setAvatar(recommendEntity.getAvatar());
                    requestEntity.setAddress(recommendEntity.getAddress());
                    requestEntity.setUsername(recommendEntity.getUsername());
                    requestEntity.setStatus(4);
                    if(!TextUtils.isEmpty(recommendEntity.getUsername()) && !TextUtils.isEmpty(recommendEntity.getAddress())){
                        listRecommend.add(requestEntity);
                    }
                }
                listRuquest.clear();
                listRuquest.addAll(ContactHelper.getInstance().loadFriendRequest());
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                ArrayList<FriendRequestEntity> listFinal = new ArrayList<>();
                listFinal.addAll(listRecommend);
                listFinal.addAll(listRuquest);
                mView.notifyData(listRecommend.size(),listFinal);
            }
        }.execute();
    }

}
