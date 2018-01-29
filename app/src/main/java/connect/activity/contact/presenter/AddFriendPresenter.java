package connect.activity.contact.presenter;

import android.os.AsyncTask;

import java.util.ArrayList;

import connect.activity.contact.bean.ContactNotice;
import connect.activity.contact.contract.AddFriendContract;
import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.bean.FriendRequestEntity;

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
        queryFriend();
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
                listFinal.addAll(ContactHelper.getInstance().loadFriendRequest());
                return listFinal;
            }

            @Override
            protected void onPostExecute(ArrayList<FriendRequestEntity> list) {
                super.onPostExecute(list);
                mView.notifyData(false, list);
            }
        }.execute();
    }

    @Override
    public void requestNoInterest(final String uid) {
        /*Connect.NOInterest noInterest = Connect.NOInterest.newBuilder()
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
        });*/
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
