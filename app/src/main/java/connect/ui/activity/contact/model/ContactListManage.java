package connect.ui.activity.contact.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import connect.db.green.DaoHelper.ContactHelper;
import connect.db.green.bean.ContactEntity;
import connect.db.green.bean.FriendRequestEntity;
import connect.db.green.bean.GroupEntity;
import connect.ui.activity.chat.model.FriendCompara;

/**
 * Created by Administrator on 2017/1/10.
 */
public class ContactListManage {

    private FriendCompara friendCompara = new FriendCompara();

    /**
     * friend request list
     * @return
     */
    public List<ContactEntity> getContactRequest(){
        ArrayList<ContactEntity> listRequest = new ArrayList<>();
        List<FriendRequestEntity> requestList = ContactHelper.getInstance().loadFriendRequestNew();
        if(requestList != null && requestList.size() > 0){
            ArrayList<ContactEntity> listNew = new ArrayList<>();
            ContactEntity friendEntity = new ContactEntity();
            friendEntity.setAvatar(requestList.get(requestList.size()-1).getAvatar());
            friendEntity.setUsername(requestList.get(requestList.size()-1).getUsername());
            friendEntity.setRemark(requestList.get(requestList.size()-1).getTips());
            friendEntity.setSource(requestList.size());
            listNew.add(friendEntity);
            listRequest.addAll(0,listNew);
        }else{
            listRequest.add(0,new ContactEntity());
        }
        return listRequest;
    }

    /**
     * group list
     * @return
     */
    public List<ContactEntity> getGroupList(){
        List<GroupEntity> localGroup = ContactHelper.getInstance().loadGroupCommonAll();
        ArrayList<ContactEntity> groupList = new ArrayList<>();
        for(GroupEntity groupEntity : localGroup){
            ContactEntity friendEntity = new ContactEntity();
            friendEntity.setUsername(groupEntity.getName());
            friendEntity.setPub_key(groupEntity.getIdentifier());
            friendEntity.setAvatar(groupEntity.getAvatar());
            groupList.add(friendEntity);
        }
        return groupList;
    }

    /**
     * friend list
     * @return
     */
    public HashMap<String,List<ContactEntity>> getFriendList(){
        List<ContactEntity> loacalFriend = ContactHelper.getInstance().loadAll();
        return sortFriend("",loacalFriend);
    }

    public HashMap<String,List<ContactEntity>> getFriendListNoSys(String pubKeyExc){
        List<ContactEntity> loacalFriend = ContactHelper.getInstance().loadFriend();
        return sortFriend(pubKeyExc,loacalFriend);
    }

    public HashMap<String,List<ContactEntity>> getFriendListNoSys(){
        List<ContactEntity> loacalFriend = ContactHelper.getInstance().loadFriend();
        return sortFriend("",loacalFriend);
    }

    private HashMap<String,List<ContactEntity>> sortFriend(String pubKeyExc ,List<ContactEntity> loacalFriend){
        Collections.sort(loacalFriend, friendCompara);
        ArrayList<ContactEntity> favoritesList = new ArrayList<>();
        ArrayList<ContactEntity> friendList = new ArrayList<>();
        for(ContactEntity friendEntity : loacalFriend){
            if(friendEntity.getPub_key().equals(pubKeyExc)){
                continue;
            }

            if(friendEntity.getCommon() != null && friendEntity.getCommon()==1){
                favoritesList.add(friendEntity);
            }else{
                friendList.add(friendEntity);
            }
        }
        HashMap<String,List<ContactEntity>> friendMap = new HashMap<>();
        friendMap.put("favorite",favoritesList);
        friendMap.put("friend",friendList);
        return friendMap;
    }
}
