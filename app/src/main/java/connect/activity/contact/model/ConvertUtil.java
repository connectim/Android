package connect.activity.contact.model;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import connect.database.MemoryDataManager;
import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.bean.ContactEntity;
import connect.database.green.bean.FriendRequestEntity;
import connect.activity.contact.presenter.FriendAddPresenter;
import connect.activity.contact.bean.PhoneContactBean;
import connect.utils.cryption.DecryptionUtil;
import connect.utils.cryption.EncryptionUtil;
import connect.utils.cryption.SupportKeyUril;
import protos.Connect;

/**
 * Created by Administrator on 2016/12/29.
 */
public class ConvertUtil {

    public FriendRequestEntity convertFriendRequestEntity(Connect.ReceiveFriendRequest receiver) {
        if (receiver == null)
            return null;
        FriendRequestEntity requestEntity = new FriendRequestEntity();
        requestEntity.setSource(receiver.getSource());
        requestEntity.setAddress(receiver.getSender().getAddress());
        requestEntity.setAvatar(receiver.getSender().getAvatar());
        requestEntity.setUsername(receiver.getSender().getUsername());
        requestEntity.setPub_key(receiver.getSender().getPubKey());
        requestEntity.setStatus(1);
        requestEntity.setRead(0);
        byte[] tipsByte = DecryptionUtil.decodeAESGCM(EncryptionUtil.ExtendedECDH.NONE, MemoryDataManager.getInstance().getPriKey(),
                receiver.getSender().getPubKey(),receiver.getTips());
        String rusult = "";
        try {
            rusult = new String(tipsByte,"utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        requestEntity.setTips(rusult);
        return requestEntity;
    }

    /**
     * Query whether registered users to add
     *
     * @param localList
     * @param listUserInfo
     * @param handler
     */
    public void convertUserInfo(final List<PhoneContactBean> localList, final ArrayList<Connect.PhoneBookUserInfo> listUserInfo, final Handler handler) {
        new AsyncTask<Void, Void, HashMap<String,List<PhoneContactBean>>>() {
            @Override
            protected HashMap<String,List<PhoneContactBean>> doInBackground(Void... params) {
                HashMap<String,List<PhoneContactBean>> map = new HashMap<>();
                ArrayList<PhoneContactBean> arrayList = new ArrayList<>();
                ArrayList<PhoneContactBean> local = new ArrayList<>();

                for (Connect.PhoneBookUserInfo bookUserInfo : listUserInfo) {
                    Connect.UserInfo userInfo = bookUserInfo.getUser();
                    PhoneContactBean contactBean = new PhoneContactBean();
                    contactBean.setNickName(userInfo.getUsername());
                    contactBean.setAddress(userInfo.getAddress());
                    contactBean.setPubKey(userInfo.getPubKey());
                    contactBean.setAvater(userInfo.getAvatar());
                    contactBean.setPhone(bookUserInfo.getPhoneHash());

                    ContactEntity friendEntity = ContactHelper.getInstance().loadFriendEntity(userInfo.getPubKey());
                    if (friendEntity != null) {
                        // Check whether as a friend
                        contactBean.setStatus(2);
                    } else if (ContactHelper.getInstance().loadFriendRequest(userInfo.getAddress()) != null) {
                        // Check whether there have been a friend request
                        contactBean.setStatus(3);
                    } else {
                        // Registered, but didn't add
                        contactBean.setStatus(1);
                    }
                    if(!TextUtils.isEmpty(bookUserInfo.getPhoneHash())){
                        arrayList.add(contactBean);
                    }
                }

                for (PhoneContactBean contactBean : localList) {
                    String phoneHmac = SupportKeyUril.hmacSHA512(contactBean.getPhone(),SupportKeyUril.SaltHMAC);
                    boolean isAdd = true;
                    for(PhoneContactBean serverContactBean : arrayList){
                        if (serverContactBean.getPhone().equals(phoneHmac)) {
                            isAdd = false;
                            serverContactBean.setName(contactBean.getName());
                            break;
                        }
                    }
                    if(isAdd)
                        local.add(contactBean);
                }

                map.put("local",local);
                map.put("server",arrayList);
                return map;
            }

            @Override
            protected void onPostExecute(HashMap<String,List<PhoneContactBean>> map) {
                super.onPostExecute(map);
                Message message = new Message();
                message.what = FriendAddPresenter.UPDATE_CODE;
                message.obj = map;
                handler.sendMessage(message);
            }
        }.execute();
    }


}
