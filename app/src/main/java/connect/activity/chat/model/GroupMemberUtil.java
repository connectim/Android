package connect.activity.chat.model;

import com.google.protobuf.InvalidProtocolBufferException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import connect.activity.base.BaseListener;
import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.bean.GroupMemberEntity;
import connect.utils.ProtoBufUtil;
import connect.utils.UriUtil;
import connect.utils.cryption.DecryptionUtil;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import protos.Connect;

/**
 * Created by gtq on 2016/12/5.
 */
public class GroupMemberUtil {

    private static String TAG = "_ChatMsgUtil";

    public static GroupMemberUtil groupMemberUtil = getIntance();

    private synchronized static GroupMemberUtil getIntance() {
        if (groupMemberUtil == null) {
            groupMemberUtil = new GroupMemberUtil();
        }
        return groupMemberUtil;
    }

    private Map<String, GroupMemberEntity> memEntityMap = null;

    public void loadGroupMembersMap(String groupKey) {
        if (memEntityMap == null) {
            memEntityMap = new HashMap<>();
        }
        List<GroupMemberEntity> groupMemEntities = ContactHelper.getInstance().loadGroupMemEntities(groupKey);
        for (GroupMemberEntity memEntity : groupMemEntities) {
            memEntityMap.put(memEntity.getUid(), memEntity);
        }
    }

    public void loadGroupMember(String groupKey, String memberkey, BaseListener<GroupMemberEntity> baseListener) {
        if (memEntityMap == null) {
            loadGroupMembersMap(groupKey);
        }

        GroupMemberEntity memberEntity = memEntityMap.get(memberkey);
        if (memberEntity == null) {
            requestGroupMemberDetailInfo(memberkey, baseListener);
        } else {
            baseListener.Success(memberEntity);
        }
    }

    public void requestGroupMemberDetailInfo(String publickey, final BaseListener<GroupMemberEntity> baseListener) {
        Connect.SearchUser searchUser = Connect.SearchUser.newBuilder()
                .setCriteria(publickey)
                .build();
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.CONNEXT_V1_USERS_SEARCHBYPUBKEY, searchUser, new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                try {
                    Connect.StructData structData = Connect.StructData.parseFrom(response.getBody());
                    Connect.UserInfo userInfo = Connect.UserInfo.parseFrom(structData.getPlainData());
                    if (ProtoBufUtil.getInstance().checkProtoBuf(userInfo)) {
                        GroupMemberEntity memberEntity = new GroupMemberEntity();
                        memberEntity.setAvatar(userInfo.getAvatar());
                        memberEntity.setUsername(userInfo.getUsername());
                        memEntityMap.put(userInfo.getPubKey(), memberEntity);
                        baseListener.Success(memberEntity);
                    }
                } catch (InvalidProtocolBufferException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Connect.HttpResponse response) {
                baseListener.fail("");
            }
        });
    }
}
