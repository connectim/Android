package connect.activity.contact.presenter;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import connect.activity.contact.bean.PhoneContactBean;
import connect.activity.contact.contract.AddFriendPhoneContract;
import connect.activity.contact.model.ConvertUtil;
import connect.activity.contact.model.PhoneListComparatorSort;
import connect.database.SharedPreferenceUtil;
import connect.ui.activity.R;
import connect.utils.ProgressUtil;
import connect.utils.ProtoBufUtil;
import connect.utils.StringUtil;
import connect.utils.ToastEUtil;
import connect.utils.UriUtil;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import connect.utils.system.SystemDataUtil;
import connect.utils.system.SystemUtil;
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

    @Override
    public void requestContact() {
        ProgressUtil.getInstance().showProgress(mView.getActivity());
        new AsyncTask<Void, Void, Connect.PhoneBook>() {
            @Override
            protected Connect.PhoneBook doInBackground(Void... params) {
                localList = SystemDataUtil.getLocalAddressBook(mView.getActivity());
                if (null == localList || localList.size() == 0) return null;

                Connect.PhoneBook.Builder builder = Connect.PhoneBook.newBuilder();
                for (int i = 0; i < localList.size(); i++) {
                    String phone = StringUtil.filterNumber(localList.get(i).getPhone());
                   // String phoneHmac = SupportKeyUril.hmacSHA512(phone, SupportKeyUril.SaltHMAC);
                    String phoneHmac = "";
                    Connect.PhoneInfo phoneInfo = Connect.PhoneInfo.newBuilder()
                            .setMobile(phoneHmac)
                            .build();
                    builder.addMobiles(phoneInfo);
                }
                Connect.PhoneBook phoneBook = builder.build();
                return phoneBook;
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
     */
    private void syncPhone(Connect.PhoneBook phoneBook){
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.SETTING_PHONE_SYNC, phoneBook, new ResultCall<Connect.HttpNotSignResponse>() {
            @Override
            public void onResponse(Connect.HttpNotSignResponse response) {
                getServeFriend();
            }

            @Override
            public void onError(Connect.HttpNotSignResponse response) {
                ProgressUtil.getInstance().dismissProgress();
            }
        });
    }

    /**
     * Get register your friends for your phone contacts
     */
    private void getServeFriend() {
        Connect.RequestNotEncrypt notEncrypt = Connect.RequestNotEncrypt.newBuilder().build();
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.CONNEXT_V1_USERS_PHONEBOOK, notEncrypt, new ResultCall<Connect.HttpNotSignResponse>() {
            @Override
            public void onResponse(Connect.HttpNotSignResponse response) {
                try {
                    Connect.StructData structData = Connect.StructData.parseFrom(response.getBody());
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
            public void onError(Connect.HttpNotSignResponse response) {
                handler.sendEmptyMessage(UPDATE_CODE);
            }
        });
    }

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
    public void sendMessage(List<PhoneContactBean> list) {
        String numbers = "";
        for (PhoneContactBean contactBean : list) {
            numbers = numbers + contactBean.getPhone() + ";";
        }
        SystemUtil.sendPhoneSMS(mView.getActivity(), numbers, mView.getActivity().getString(R.string.Link_invite_encrypted_chat_with_APP_Download,
                SharedPreferenceUtil.getInstance().getUser().getName()));
    }

}
