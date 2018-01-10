package connect.activity.contact.model;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.bean.ContactEntity;
import connect.database.green.bean.FriendRequestEntity;
import connect.database.green.bean.GroupEntity;
import connect.activity.base.compare.FriendCompara;
import connect.activity.home.bean.ContactBean;
import connect.utils.PinyinUtil;

/**
 * Created by Administrator on 2017/1/10.
 */
public class ContactListManage {

    private FriendCompara friendCompara = new FriendCompara();

    /**
     * friend request list
     * @return
     */
    public List<ContactBean> getContactRequest(){
        ArrayList<ContactBean> listRequest = new ArrayList<>();
        List<FriendRequestEntity> requestList = ContactHelper.getInstance().loadFriendRequestNew();
        if(requestList != null && requestList.size() > 0){
            ContactBean contactBean = new ContactBean();
            contactBean.setAvatar(requestList.get(requestList.size()-1).getAvatar());
            contactBean.setName(requestList.get(requestList.size()-1).getUsername());
            contactBean.setTips(requestList.get(requestList.size()-1).getTips());
            contactBean.setCount(requestList.size());
            contactBean.setStatus(1);
            listRequest.add(contactBean);
        }else{
            ContactBean contactBean = new ContactBean();
            contactBean.setStatus(1);
            listRequest.add(contactBean);
        }
        return listRequest;
    }

    /**
     * group list
     * @return
     */
    public List<ContactBean> getGroupData(){
        List<GroupEntity> localGroup = ContactHelper.getInstance().loadGroupCommonAll();
        ArrayList<ContactBean> groupList = new ArrayList<>();
        for(GroupEntity groupEntity : localGroup){
            ContactBean contactBean = new ContactBean();
            contactBean.setName(groupEntity.getName());
            contactBean.setUid(groupEntity.getIdentifier());
            contactBean.setAvatar(groupEntity.getAvatar());
            contactBean.setStatus(2);
            groupList.add(contactBean);
        }
        return groupList;
    }

    /**
     * friend list
     * @return
     */
    public HashMap<String,List<ContactBean>> getFriendList(){
        List<ContactEntity> loacalFriend = ContactHelper.getInstance().loadAll();
        return sortContactFriend("",loacalFriend);
    }

    public HashMap<String,List<ContactBean>> getFriendListExcludeSys(String pubKeyExc){
        List<ContactEntity> loacalFriend = ContactHelper.getInstance().loadFriend();
        return sortContactFriend(pubKeyExc,loacalFriend);
    }

    private HashMap<String,List<ContactBean>> sortContactFriend(String pubKeyExc ,List<ContactEntity> loacalFriend){
        Collections.sort(loacalFriend, friendCompara);
        ArrayList<ContactBean> favoritesList = new ArrayList<>();
        ArrayList<ContactBean> friendList = new ArrayList<>();
        for(ContactEntity friendEntity : loacalFriend){
            if(friendEntity.getUid().equals(pubKeyExc)) continue;

            ContactBean contactBean = new ContactBean();
            String name = TextUtils.isEmpty(friendEntity.getRemark()) ? friendEntity.getName() : friendEntity.getRemark();
            contactBean.setName(name);
            contactBean.setAvatar(friendEntity.getAvatar());
            contactBean.setUid(friendEntity.getUid());
            contactBean.setOu(friendEntity.getOu());
            if(TextUtils.isEmpty(friendEntity.getPublicKey())){
                contactBean.setStatus(6);
                friendList.add(contactBean);
            }/*else if(friendEntity.getCommon() != null && friendEntity.getCommon()==1){
                contactBean.setStatus(3);
                favoritesList.add(contactBean);
            }*/else{
                contactBean.setStatus(4);
                friendList.add(contactBean);
            }
        }
        HashMap<String,List<ContactBean>> friendMap = new HashMap<>();
        friendMap.put("favorite",favoritesList);
        friendMap.put("friend",friendList);
        return friendMap;
    }

    public String checkShowFriendTop(ContactBean currBean,ContactBean lastBean){
        char curFirstChar = TextUtils.isEmpty(currBean.getName()) ? '#' : currBean.getName().charAt(0);
        if(lastBean == null){
            if(currBean.getStatus() == 2 || currBean.getStatus() == 3){
                return "show";
            }else if(currBean.getStatus() == 4 || currBean.getStatus() == 6){
                return PinyinUtil.chatToPinyin(curFirstChar);
            }else{
                return "";
            }
        }
        char lastFirstChar = TextUtils.isEmpty(lastBean.getName()) ? '#' : lastBean.getName().charAt(0);

        if(currBean.getStatus() == 2 || currBean.getStatus() == 3){ // Group and Frequent contacts
            if(lastBean.getStatus() != currBean.getStatus()){
                return "show";
            }else{
                return "";
            }
        }else{ // Friend
            if(lastBean.getStatus() != 4 && lastBean.getStatus() != 6){
                return PinyinUtil.chatToPinyin(curFirstChar);
            }else{
                String currLetter = PinyinUtil.chatToPinyin(curFirstChar);
                String lastLetter = PinyinUtil.chatToPinyin(lastFirstChar);
                if(currLetter.equals(lastLetter)){
                    return "";
                }else{
                    return currLetter;
                }
            }
        }
    }

}
