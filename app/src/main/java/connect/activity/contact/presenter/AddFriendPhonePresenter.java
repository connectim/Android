package connect.activity.contact.presenter;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import connect.database.green.DaoHelper.ParamManager;
import connect.ui.activity.R;
import connect.activity.contact.contract.AddFriendPhoneContract;
import connect.activity.contact.model.ConvertUtil;
import connect.activity.contact.model.PhoneListComparatorSort;
import connect.activity.set.bean.PrivateSetBean;
import connect.utils.ProgressUtil;
import connect.utils.ProtoBufUtil;
import connect.utils.StringUtil;
import connect.utils.system.SystemDataUtil;
import connect.utils.TimeUtil;
import connect.utils.ToastEUtil;
import connect.utils.UriUtil;
import connect.activity.contact.bean.PhoneContactBean;
import instant.utils.cryption.DecryptionUtil;
import instant.utils.cryption.SupportKeyUril;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import protos.Connect;

public class AddFriendPhonePresenter implements AddFriendPhoneContract.Presenter{

    private AddFriendPhoneContract.View mView;
    private List<PhoneContactBean> localList;
    private Connect.PhoneBookUsersInfo usersInfo;
    public static final int UPDATE_CODE = 151;
    private ArrayList<PhoneContactBean> listData = new ArrayList<>();
    private PhoneListComparatorSort comp = new PhoneListComparatorSort();

    public AddFriendPhonePresenter(AddFriendPhoneContract.View mView) {
        this.mView = mView;
        mView.setPresenter(this);
    }

    @Override
    public void start() {}

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            ProgressUtil.getInstance().dismissProgress();
            switch (msg.what) {
                case UPDATE_CODE:
                    listData.clear();
                    HashMap<String, List<PhoneContactBean>> map = (HashMap<String, List<PhoneContactBean>>) msg.obj;
                    if(map == null){
                        return;
                    }

                    List<PhoneContactBean> local = map.get("local");
                    List<PhoneContactBean> server = map.get("server");

                    Collections.sort(server, comp);
                    if(server.size() > 0)
                        server.add(0,new PhoneContactBean());
                    listData.addAll(server);
                    Collections.sort(local, comp);
                    listData.addAll(local);
                    mView.updateView(server.size(),listData);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void requestContact() {
        ProgressUtil.getInstance().showProgress(mView.getActivity());
        new AsyncTask<Void, Void, Connect.PhoneBook>() {
            @Override
            protected Connect.PhoneBook doInBackground(Void... params) {
                return getPhoneHmacBook();
            }

            @Override
            protected void onPostExecute(Connect.PhoneBook phoneBook) {
                super.onPostExecute(phoneBook);
                if(phoneBook == null){
                    ProgressUtil.getInstance().dismissProgress();
                    ToastEUtil.makeText(mView.getActivity(),R.string.Link_contact_loading_failed_check_the_contact_pression,ToastEUtil.TOAST_STATUS_FAILE).show();
                    return;
                }
                syncPhone(phoneBook);
            }
        }.execute();
    }

    /**
     * Synchronize the encrypted phone number
     *
     * @param phoneBook phone data
     */
    private void syncPhone(Connect.PhoneBook phoneBook){
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.SETTING_PHONE_SYNC, phoneBook, new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                PrivateSetBean privateSetBean = ParamManager.getInstance().getPrivateSet();
                if(null != privateSetBean){
                    privateSetBean.setUpdateTime(TimeUtil.getCurrentTimeInString(TimeUtil.DEFAULT_DATE_FORMAT));
                    ParamManager.getInstance().putPrivateSet(privateSetBean);
                }
                getServeFriend();
            }

            @Override
            public void onError(Connect.HttpResponse response) {
                ProgressUtil.getInstance().dismissProgress();
            }
        });
    }

    /**
     * Get register your friends for your phone contacts
     */
    private void getServeFriend() {
        Connect.RequestNotEncrypt notEncrypt = Connect.RequestNotEncrypt.newBuilder().build();
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.CONNEXT_V1_USERS_PHONEBOOK, notEncrypt, new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                try {
                    Connect.IMResponse imResponse = Connect.IMResponse.parseFrom(response.getBody().toByteArray());
                    Connect.StructData structData = DecryptionUtil.decodeAESGCMStructData(imResponse.getCipherData());
                    usersInfo = Connect.PhoneBookUsersInfo.parseFrom(structData.getPlainData());
                    ArrayList<Connect.PhoneBookUserInfo> listCheck = new ArrayList<>();
                    for(Connect.PhoneBookUserInfo userInfo : usersInfo.getUsersList()){
                        if(ProtoBufUtil.getInstance().checkProtoBuf(userInfo)){
                            listCheck.add(userInfo);
                        }
                    }
                    ConvertUtil convertUtil = new ConvertUtil();
                    convertUtil.convertUserInfo(localList, listCheck, handler);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Connect.HttpResponse response) {
                handler.sendEmptyMessage(UPDATE_CODE);
            }
        });
    }

    /**
     * Access to local contacts, and to do Hmac phone number
     *
     * @return Phone number after I finish Hmac the PhoneBook
     */
    private Connect.PhoneBook getPhoneHmacBook(){
        // For local contacts
       localList = SystemDataUtil.getLoadAddresSbook(mView.getActivity());
        if (null == localList || localList.size() == 0) {
            return null;
        }
        // Do the Hmac to telephone number
        Connect.PhoneBook.Builder builder = Connect.PhoneBook.newBuilder();
        for (int i = 0; i < localList.size(); i++) {
            String phone = StringUtil.filterNumber(localList.get(i).getPhone());
            String phoneHmac = SupportKeyUril.hmacSHA512(phone, SupportKeyUril.SaltHMAC);
            Connect.PhoneInfo phoneInfo = Connect.PhoneInfo.newBuilder()
                    .setMobile(phoneHmac)
                    .build();
            builder.addMobiles(phoneInfo);
        }
        Connect.PhoneBook phoneBook = builder.build();
        return phoneBook;
    }

}
