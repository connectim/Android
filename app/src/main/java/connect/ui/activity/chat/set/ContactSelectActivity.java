package connect.ui.activity.chat.set;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;

import com.google.protobuf.ByteString;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import connect.db.MemoryDataManager;
import connect.db.SharedPreferenceUtil;
import connect.db.green.DaoHelper.ContactHelper;
import connect.db.green.DaoHelper.ConversionHelper;
import connect.db.green.DaoHelper.MessageHelper;
import connect.db.green.bean.ContactEntity;
import connect.db.green.bean.ConversionEntity;
import connect.db.green.bean.GroupEntity;
import connect.db.green.bean.GroupMemberEntity;
import connect.im.bean.SocketACK;
import connect.im.model.ChatSendManager;
import connect.ui.activity.R;
import connect.ui.activity.chat.ChatActivity;
import connect.ui.activity.chat.bean.GroupExt1Bean;
import connect.ui.activity.chat.bean.MsgEntity;
import connect.ui.activity.chat.bean.Talker;
import connect.ui.activity.chat.model.FriendCompara;
import connect.ui.activity.chat.model.content.FriendChat;
import connect.ui.activity.chat.model.content.GroupChat;
import connect.ui.activity.home.bean.HttpRecBean;
import connect.ui.activity.login.bean.UserBean;
import connect.ui.adapter.MulContactAdapter;
import connect.ui.base.BaseActivity;
import connect.utils.ActivityUtil;
import connect.utils.ProtoBufUtil;
import connect.utils.RegularUtil;
import connect.utils.StringUtil;
import connect.utils.TimeUtil;
import connect.utils.ToastEUtil;
import connect.utils.UriUtil;
import connect.utils.cryption.DecryptionUtil;
import connect.utils.cryption.EncryptionUtil;
import connect.utils.cryption.SupportKeyUril;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import connect.view.SideBar;
import connect.view.TopToolBar;
import protos.Connect;

/**
 * add group member
 * Created by gtq on 2016/12/15.
 */
public class ContactSelectActivity extends BaseActivity {

    @Bind(R.id.toolbar)
    TopToolBar toolbar;
    @Bind(R.id.recyclerview)
    RecyclerView recyclerview;
    @Bind(R.id.siderbar)
    SideBar siderbar;

    private final String Tag = "ContactSelectActivity";

    private ContactSelectActivity activity;
    private static String ROOM_TAG = "ROOM_TAG";
    private static String ROOM_KEYS = "ROOM_KEYS";
    private String groupEcdh = null;
    private int roomTag;
    private String roomKey;
    private List<String> oldMembers = null;
    private GroupEntity groupEntity = null;
    private List<ContactEntity> selectEntities = new ArrayList<>();
    private List<GroupMemberEntity> memEntities = null;
    private FriendCompara friendCompara = new FriendCompara();

    private boolean move;
    private int topPosi;
    private LinearLayoutManager linearLayoutManager;
    private MulContactAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mulcontact);
        ButterKnife.bind(this);
        initView();
    }

    /**
     * create group
     *
     * @param activity
     * @param pubkeys
     */
    public static void startCreateGroupActivity(Activity activity, String pubkeys) {
        Bundle bundle = new Bundle();
        bundle.putInt(ROOM_TAG, 0);
        bundle.putString(ROOM_KEYS, pubkeys);
        ActivityUtil.next(activity, ContactSelectActivity.class, bundle);
    }

    /**
     * invite group member
     *
     * @param activity
     * @param pubkeys
     */
    public static void startInviteGroupActivity(Activity activity, String pubkeys) {
        Bundle bundle = new Bundle();
        bundle.putInt(ROOM_TAG, 1);
        bundle.putString(ROOM_KEYS, pubkeys);
        ActivityUtil.next(activity, ContactSelectActivity.class, bundle);
    }

    @Override
    public void initView() {
        activity = this;
        toolbar.setBlackStyle();
        toolbar.setLeftImg(R.mipmap.back_white);
        toolbar.setTitle(getResources().getString(R.string.Chat_Choose_contact));
        toolbar.setRightText(R.string.Chat_Complete);
        toolbar.setRightTextColor(R.color.color_6d6e75);

        toolbar.setLeftListence(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityUtil.goBack(activity);
            }
        });

        linearLayoutManager = new LinearLayoutManager(activity);
        List<ContactEntity> friendEntities = ContactHelper.getInstance().loadFriend();
        Collections.sort(friendEntities, friendCompara);

        roomTag = getIntent().getIntExtra(ROOM_TAG, 0);
        roomKey = getIntent().getStringExtra(ROOM_KEYS);
        groupEntity = ContactHelper.getInstance().loadGroupEntity(roomKey);

        oldMembers = new ArrayList<>();
        if (0 == roomTag) {//create group
            oldMembers.add(roomKey);
        } else {//Invite friends to join in the group
            memEntities = ContactHelper.getInstance().loadGroupMemEntity(roomKey);
            for (GroupMemberEntity memEntity : memEntities) {
                oldMembers.add(memEntity.getIdentifier());
            }
        }

        adapter = new MulContactAdapter(activity, oldMembers, friendEntities);
        recyclerview.setLayoutManager(linearLayoutManager);
        recyclerview.setAdapter(adapter);
        recyclerview.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (move) {
                    move = false;
                    int n = topPosi - linearLayoutManager.findFirstVisibleItemPosition();
                    if (0 <= n && n < recyclerview.getChildCount()) {
                        int top = recyclerview.getChildAt(n).getTop();
                        recyclerview.scrollBy(0, top);
                    }
                }
            }
        });
        adapter.setOnSeleFriendListence(new MulContactAdapter.OnSeleFriendListence() {
            @Override
            public void seleFriend(List<ContactEntity> list) {
                if (list == null || list.size() < 1) {
                    toolbar.setRightTextColor(R.color.color_6d6e75);
                    toolbar.setRightListence(null);
                } else {
                    toolbar.setRightTextColor(R.color.color_green);
                    toolbar.setRightListence(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            selectEntities = adapter.getSelectEntities();
                            if (selectEntities == null || selectEntities.size() < 1) {
                                toolbar.setRightTextColor(R.color.color_6d6e75);
                                return;
                            }

                            if (0 == roomTag) {
                                ContactEntity newentity = ContactHelper.getInstance().loadFriendEntity(roomKey);
                                selectEntities.add(0, newentity);

                                createNewGroup();
                            } else {
                                if (groupEntity.getVerify() == null || 0==groupEntity.getVerify()) {//Closed group  validation
                                    inviteToGroup();
                                } else {//Open group validation
                                    sendGroupToFriend();
                                }
                            }
                        }
                    });
                }
            }
        });

        siderbar.setOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener() {
            @Override
            public void onTouchingLetterChanged(String s) {
                int position = adapter.getPositionForSection(s.charAt(0));
                moveToPosition(position);
            }
        });
    }

    private void moveToPosition(int posi) {
        this.topPosi = posi;
        int firstItem = linearLayoutManager.findFirstVisibleItemPosition();
        int lastItem = linearLayoutManager.findLastVisibleItemPosition();
        if (posi <= firstItem) {
            recyclerview.scrollToPosition(posi);
        } else if (posi <= lastItem) {
            int top = recyclerview.getChildAt(posi - firstItem).getTop();
            recyclerview.scrollBy(0, top);
        } else {
            recyclerview.scrollToPosition(posi);
            move = true;
        }
    }

    protected void createNewGroup() {
        String groupname = String.format(getString(R.string.Link_user_friends), MemoryDataManager.getInstance().getName());

        String ranprikey = EncryptionUtil.randomPriKey();
        String randpubkey = EncryptionUtil.randomPubKey(ranprikey);

        byte[] groupecdhkey = SupportKeyUril.rawECDHkey(ranprikey, randpubkey);
        groupEcdh = StringUtil.bytesToHexString(groupecdhkey);
        Connect.CreateGroupMessage createGroupMessage = Connect.CreateGroupMessage.newBuilder()
                .setSecretKey(groupEcdh).build();

        List<Connect.AddGroupUserInfo> groupUserInfos = new ArrayList<>();

        for (ContactEntity entity : selectEntities) {
            byte[] memberecdhkey = SupportKeyUril.rawECDHkey(MemoryDataManager.getInstance().getPriKey(), entity.getPub_key());
            Connect.GcmData gcmData = EncryptionUtil.encodeAESGCMStructData(SupportKeyUril.EcdhExts.EMPTY, memberecdhkey, createGroupMessage.toByteString());

            String pubkey = MemoryDataManager.getInstance().getPubKey();
            String groupHex = StringUtil.bytesToHexString(gcmData.toByteArray());
            String backup = String.format("%1$s/%2$s", pubkey, groupHex);

            Connect.AddGroupUserInfo groupUserInfo = Connect.AddGroupUserInfo.newBuilder()
                    .setAddress(entity.getAddress())
                    .setBackup(backup).build();
            groupUserInfos.add(groupUserInfo);
        }
        createGroupRequest(groupname, groupUserInfos);
    }

    /**
     * Request sent to create groups
     */
    public void createGroupRequest(final String groupname, final List<Connect.AddGroupUserInfo> infos) {
        Connect.CreateGroup createGroup = Connect.CreateGroup.newBuilder()
                .setName(groupname)
                .addAllUsers(infos).build();
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.CREATE_GROUP, createGroup, new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                try {
                    Connect.IMResponse imResponse = Connect.IMResponse.parseFrom(response.getBody().toByteArray());
                    if (!SupportKeyUril.verifySign(imResponse.getSign(), imResponse.getCipherData().toByteArray())) {
                        throw new Exception("Validation fails");
                    }

                    Connect.StructData structData = DecryptionUtil.decodeAESGCMStructData(imResponse.getCipherData());
                    Connect.GroupInfo groupInfo = Connect.GroupInfo.parseFrom(structData.getPlainData());
                    if(ProtoBufUtil.getInstance().checkProtoBuf(groupInfo)){
                        loadGroupDb(groupname, groupInfo);
                        newGroupBroadCast(groupInfo);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Connect.HttpResponse response) {
                String contentTxt = response.getMessage();
                if (!TextUtils.isEmpty(contentTxt)) {
                    ToastEUtil.makeText(activity, contentTxt, 2).show();
                }
            }
        });
    }

    public void loadGroupDb(String groupname, Connect.GroupInfo groupInfo) {
        final String grouppubkey = groupInfo.getGroup().getIdentifier();

        ConversionEntity roomEntity = new ConversionEntity();
        roomEntity.setType(1);
        roomEntity.setIdentifier(grouppubkey);
        roomEntity.setAvatar(RegularUtil.groupAvatar(grouppubkey));
        roomEntity.setLast_time(TimeUtil.getCurrentTimeInLong());
        roomEntity.setContent(getString(R.string.Chat_Tips));
        ConversionHelper.getInstance().insertRoomEntity(roomEntity);

        final GroupEntity groupEntity = new GroupEntity();
        groupEntity.setName(groupname);
        groupEntity.setIdentifier(grouppubkey);
        groupEntity.setEcdh_key(groupEcdh);
        groupEntity.setAvatar(RegularUtil.groupAvatar(grouppubkey));
        ContactHelper.getInstance().inserGroupEntity(groupEntity);

        HttpRecBean.sendHttpRecMsg(HttpRecBean.HttpRecType.UpLoadBackUp, grouppubkey, groupEcdh);
        String stringMems = "";
        List<GroupMemberEntity> memEntities = new ArrayList<>();
        for (Connect.GroupMember member : groupInfo.getMembersList()) {
            GroupMemberEntity memEntity = ContactHelper.getInstance().loadGroupMemByAds(grouppubkey, member.getAddress());
            if (memEntity == null) {
                memEntity = new GroupMemberEntity();
            }
            memEntity.setIdentifier(grouppubkey);
            memEntity.setPub_key(member.getPubKey());
            memEntity.setAddress(member.getAddress());
            memEntity.setAvatar(member.getAvatar());
            memEntity.setNick(member.getUsername());
            memEntity.setRole(member.getRole());
            memEntity.setUsername(member.getUsername());
            memEntities.add(memEntity);
            stringMems = stringMems + member.getUsername() + ",";
        }
        ContactHelper.getInstance().inserGroupMemEntity(memEntities);

        GroupChat groupChat = new GroupChat(groupEntity);
        stringMems = String.format(getString(R.string.Link_enter_the_group), stringMems);

        MsgEntity invite = groupChat.inviteNotice(stringMems);
        MessageHelper.getInstance().insertToMsg(invite.getMsgDefinBean());

        ToastEUtil.makeText(activity, activity.getString(R.string.Link_Send_successful), 1, new ToastEUtil.OnToastListener() {
            @Override
            public void animFinish() {
                ChatActivity.startActivity(activity, new Talker(groupEntity));
            }
        }).show();
    }

    protected void newGroupBroadCast(Connect.GroupInfo groupInfo) {
        if (groupInfo.getMembersCount() <= 0) {
            return;
        }

        Connect.CreateGroupMessage groupMessage = Connect.CreateGroupMessage.newBuilder().setSecretKey(groupEcdh)
                .setIdentifier(groupInfo.getGroup().getIdentifier()).build();

        String prikey = MemoryDataManager.getInstance().getPriKey();
        for (Connect.GroupMember member : groupInfo.getMembersList()) {
            byte[] groupecdhkey = SupportKeyUril.rawECDHkey(prikey, member.getPubKey());
            Connect.GcmData gcmData = EncryptionUtil.encodeAESGCMStructData(SupportKeyUril.EcdhExts.EMPTY, groupecdhkey, groupMessage.toByteString());

            String msgid = TimeUtil.timestampToMsgid();
            Connect.MessageData messageData = Connect.MessageData.newBuilder()
                    .setCipherData(gcmData)
                    .setReceiverAddress(member.getAddress())
                    .setMsgId(msgid).build();

            ChatSendManager.getInstance().sendChatAckMsg(SocketACK.GROUP_INVITE, groupInfo.getGroup().getIdentifier(), messageData);
        }
    }

    protected void sendGroupToFriend() {
        Connect.GroupInviteUser.Builder builder = Connect.GroupInviteUser.newBuilder();
        builder.setIdentifier(roomKey);

        List<String> addStrs = new ArrayList<>();
        for (int i = 0; i < selectEntities.size(); i++) {
            addStrs.add(selectEntities.get(i).getAddress());
        }
        builder.addAllAddresses(addStrs);
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.GROUP_INVITE_TOKEN, builder.build(), new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                try {
                    Connect.IMResponse imResponse = Connect.IMResponse.parseFrom(response.getBody().toByteArray());
                    if (!SupportKeyUril.verifySign(imResponse.getSign(), imResponse.getCipherData().toByteArray())) {
                        throw new Exception("Validation fails");
                    }

                    Connect.StructData structData = DecryptionUtil.decodeAESGCMStructData(imResponse.getCipherData());
                    Connect.GroupInviteResponseList responseList = Connect.GroupInviteResponseList.parseFrom(structData.getPlainData());
                    for (Connect.GroupInviteResponse res : responseList.getListList()) {
                        if(ProtoBufUtil.getInstance().checkProtoBuf(res)){
                            sendFriendInvite(res.getAddress(), res.getToken());
                        }
                    }

                    ToastEUtil.makeText(activity, activity.getString(R.string.Link_Send_successful), 1, new ToastEUtil.OnToastListener() {

                        @Override
                        public void animFinish() {
                            ActivityUtil.goBack(activity);
                        }
                    }).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Connect.HttpResponse response) {

            }
        });
    }

    private void sendFriendInvite(String address, String token) {
        GroupExt1Bean ext1Bean = new GroupExt1Bean();
        ext1Bean.setAvatar(groupEntity.getAvatar());
        ext1Bean.setGroupidentifier(groupEntity.getIdentifier());
        ext1Bean.setGroupname(groupEntity.getName());
        ext1Bean.setInviteToken(token);

        ContactEntity friendEntity = ContactHelper.getInstance().loadFriendEntity(address);
        FriendChat friendChat = new FriendChat(friendEntity);
        MsgEntity msgEntity = friendChat.joinGroupMsg(ext1Bean);

        friendChat.sendPushMsg(msgEntity);
        MessageHelper.getInstance().insertToMsg(msgEntity.getMsgDefinBean());
        friendChat.updateRoomMsg(null, "[" + getString(R.string.Link_Join_Group) + "]", msgEntity.getMsgDefinBean().getSendtime());
    }

    protected void inviteToGroup() {
        GroupEntity groupEntity = ContactHelper.getInstance().loadGroupEntity(roomKey);
        Connect.CreateGroupMessage createGroupMessage = Connect.CreateGroupMessage.newBuilder().
                setSecretKey(groupEntity.getEcdh_key()).build();

        List<Connect.AddGroupUserInfo> addUsers = new ArrayList<>();
        for (ContactEntity entity : selectEntities) {
            byte[] memberecdhkey = SupportKeyUril.rawECDHkey(MemoryDataManager.getInstance().getPriKey(), entity.getPub_key());
            Connect.GcmData gcmData = EncryptionUtil.encodeAESGCM(SupportKeyUril.EcdhExts.EMPTY, memberecdhkey, createGroupMessage.toByteArray());

            String pubkey = MemoryDataManager.getInstance().getPubKey();
            String groupHex = StringUtil.bytesToHexString(gcmData.toByteArray());
            String backup = String.format("%1$s/%2$s", pubkey, groupHex);

            gcmData = EncryptionUtil.encodeAESGCMStructData(SupportKeyUril.EcdhExts.EMPTY, groupEntity.getEcdh_key(), ByteString.copyFrom(backup.getBytes()));
            backup = StringUtil.bytesToHexString(gcmData.toByteArray());

            Connect.AddGroupUserInfo addGroupUserInfo = Connect.AddGroupUserInfo.newBuilder()
                    .setAddress(entity.getAddress())
                    .setBackup(backup).build();
            addUsers.add(addGroupUserInfo);
        }

        inviteMemRequest(addUsers);
    }

    protected void inviteMemRequest(List<Connect.AddGroupUserInfo> members) {
        Connect.AddUserToGroup addUserToGroup = Connect.AddUserToGroup.newBuilder()
                .setIdentifier(roomKey).addAllUsers(members).build();
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.GROUP_ADDUSER, addUserToGroup, new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                broadInviteMember();
                ToastEUtil.makeText(activity, activity.getString(R.string.Link_Send_successful), 1, new ToastEUtil.OnToastListener() {
                    @Override
                    public void animFinish() {
                        GroupSetActivity.startActivity(activity, roomKey);
                    }
                }).show();
            }

            @Override
            public void onError(Connect.HttpResponse response) {

            }
        });
    }

    protected void broadInviteMember() {
        GroupEntity groupEntity = ContactHelper.getInstance().loadGroupEntity(roomKey);
        Connect.CreateGroupMessage groupMessage = Connect.CreateGroupMessage.newBuilder().setSecretKey(groupEntity.getEcdh_key())
                .setIdentifier(groupEntity.getIdentifier()).build();

        String prikey = MemoryDataManager.getInstance().getPriKey();
        for (GroupMemberEntity member : memEntities) {
            byte[] groupecdhkey = SupportKeyUril.rawECDHkey(prikey, member.getPub_key());
            Connect.GcmData gcmData = EncryptionUtil.encodeAESGCMStructData(SupportKeyUril.EcdhExts.EMPTY, groupecdhkey, groupMessage.toByteString());

            Connect.MessageData messageData = Connect.MessageData.newBuilder()
                    .setCipherData(gcmData)
                    .setReceiverAddress(member.getAddress())
                    .setMsgId(TimeUtil.timestampToMsgid()).build();

            ChatSendManager.getInstance().sendChatAckMsg(SocketACK.GROUP_INVITE, roomKey, messageData);
        }

        GroupChat groupChat = new GroupChat(groupEntity);
        String extnmae = "";
        for (GroupMemberEntity member : memEntities) {
            extnmae = member.getUsername() + ",";
        }

        MsgEntity chatBean = groupChat.addUserMsg(extnmae);
        groupChat.sendPushMsg(chatBean);
    }
}
