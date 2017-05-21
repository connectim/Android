package connect.ui.activity.chat.set;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import connect.db.MemoryDataManager;
import connect.db.SharedPreferenceUtil;
import connect.db.green.DaoHelper.ContactHelper;
import connect.db.green.DaoHelper.ConversionHelper;
import connect.db.green.DaoHelper.ConversionSettingHelper;
import connect.db.green.bean.ContactEntity;
import connect.db.green.bean.ConversionEntity;
import connect.db.green.bean.ConversionSettingEntity;
import connect.db.green.bean.GroupEntity;
import connect.db.green.bean.GroupMemberEntity;
import connect.ui.activity.R;
import connect.ui.activity.chat.bean.RecExtBean;
import connect.ui.activity.contact.FriendInfoActivity;
import connect.ui.activity.contact.StrangerInfoActivity;
import connect.ui.activity.contact.bean.ContactNotice;
import connect.ui.activity.contact.bean.SourceType;
import connect.ui.activity.home.HomeActivity;
import connect.ui.activity.set.ModifyInfoActivity;
import connect.ui.base.BaseActivity;
import connect.utils.ActivityUtil;
import connect.utils.DialogUtil;
import connect.utils.UriUtil;
import connect.utils.cryption.DecryptionUtil;
import connect.utils.glide.GlideUtil;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import connect.view.TopToolBar;
import connect.view.roundedimageview.RoundedImageView;
import protos.Connect;

/**
 * group setting
 * Created by gtq on 2016/12/15.
 */
public class GroupSetActivity extends BaseActivity {

    @Bind(R.id.toolbar)
    TopToolBar toolbar;
    @Bind(R.id.count)
    TextView count;
    @Bind(R.id.linearlayout)
    LinearLayout linearlayout;
    @Bind(R.id.relativelayout_1)
    RelativeLayout relativelayout1;

    private static String GROUP_KEY = "GROUP_KEY";
    private final String TAG_ADD = "TAG_ADD";
    private final String TAG_MEMBER = "TAG_MEMBER";

    private String groupKey;
    private GroupSetActivity activity;
    private GroupEntity groupEntity;
    private ConversionEntity chatRoomEntity;
    private ConversionSettingEntity setEntity;
    private List<GroupMemberEntity> groupMemEntities = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groupset);
        ButterKnife.bind(this);
        initView();
    }

    public static void startActivity(Activity activity, String groupkey) {
        Bundle bundle = new Bundle();
        bundle.putString(GROUP_KEY, groupkey);
        ActivityUtil.next(activity, GroupSetActivity.class, bundle);
    }

    @Override
    public void initView() {
        activity = this;
        toolbar.setBlackStyle();
        toolbar.setLeftImg(R.mipmap.back_white);
        toolbar.setTitle(getResources().getString(R.string.Link_Group));
        toolbar.setLeftListence(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityUtil.goBack(activity);
            }
        });

        groupKey = getIntent().getStringExtra(GROUP_KEY);
        groupEntity = ContactHelper.getInstance().loadGroupEntity(groupKey);
        if (groupEntity == null) {
            ActivityUtil.goBack(activity);
            return;
        }

        chatRoomEntity = ConversionHelper.getInstance().loadRoomEnitity(groupKey);
        setEntity = ConversionSettingHelper.getInstance().loadSetEntity(groupKey);
        groupMemEntities = ContactHelper.getInstance().loadGroupMemEntity(groupKey);
        if (groupEntity == null) {
            ActivityUtil.goBack(activity);
            return;
        }
        if (chatRoomEntity == null) {
            chatRoomEntity = new ConversionEntity();
            chatRoomEntity.setIdentifier(groupKey);
        }
        if (setEntity == null) {
            setEntity = new ConversionSettingEntity();
            setEntity.setIdentifier(groupKey);
            setEntity.setDisturb(0);
        }

        count.setText(String.format(getString(R.string.Link_Members), groupMemEntities.size()));
        groupMemEntities = groupMemEntities.subList(0, groupMemEntities.size() > 3 ? 3 : groupMemEntities.size());

        showMemberList((LinearLayout) findViewById(R.id.linearlayout));
        relativelayout1.setTag(TAG_MEMBER);
        relativelayout1.setOnClickListener(contactClick);

        groupVerify(findViewById(R.id.groupset_groupname), R.mipmap.message_groupchat_name2x, getString(R.string.Link_Group_Name), groupEntity.getName());
        GroupMemberEntity myMember = ContactHelper.getInstance().loadGroupMemByAds(groupKey, MemoryDataManager.getInstance().getAddress());
        if (myMember == null) {
            findViewById(R.id.groupset_myname).setVisibility(View.GONE);
        } else {
            String myMemName = TextUtils.isEmpty(myMember.getUsername()) ? myMember.getNick() : myMember.getUsername();
            groupVerify(findViewById(R.id.groupset_myname), R.mipmap.message_groupchat_myname2x, getString(R.string.Link_My_Alias_in_Group), myMemName);
        }
        groupVerify(findViewById(R.id.groupset_qrcode), R.mipmap.message_groupchat_qrcode2x, getString(R.string.Link_Group_is_QR_Code), "");

        if (myMember.getRole() == null || myMember.getRole() == 0) {
            findViewById(R.id.groupset_manage).setVisibility(View.GONE);
        } else {
            groupVerify(findViewById(R.id.groupset_manage), R.mipmap.message_groupchat_setting, getString(R.string.Link_ManageGroup), "");
        }

        boolean top = Integer.valueOf(1).equals(chatRoomEntity.getTop());
        seSwitchLayout(findViewById(R.id.top), getResources().getString(R.string.Chat_Sticky_on_Top_chat), top);
        boolean notice = Integer.valueOf(1).equals(setEntity.getDisturb());
        seSwitchLayout(findViewById(R.id.mute), getResources().getString(R.string.Chat_Mute_Notification), notice);
        boolean common = Integer.valueOf(1).equals(groupEntity.getCommon());
        seSwitchLayout(findViewById(R.id.save), getResources().getString(R.string.Link_Save_to_Contacts), common);

        //other
        setOtherLayout(findViewById(R.id.clear), R.mipmap.message_clear_history2x, getResources().getString(R.string.Link_Clear_Chat_History));
        setOtherLayout(findViewById(R.id.delete), R.mipmap.message_group_leave2x, getResources().getString(R.string.Link_Delete_and_Leave));
        syncGroupInfo(groupEntity.getIdentifier());
    }

    protected void syncGroupInfo(final String value) {
        Connect.GroupId groupId = Connect.GroupId.newBuilder()
                .setIdentifier(value).build();
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.GROUP_SETTING_INFO, groupId, new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                try {
                    Connect.IMResponse imResponse = Connect.IMResponse.parseFrom(response.getBody().toByteArray());
                    Connect.StructData structData = DecryptionUtil.decodeAESGCMStructData(imResponse.getCipherData());
                    Connect.GroupSettingInfo settingInfo = Connect.GroupSettingInfo.parseFrom(structData.getPlainData());

                    setEntity.setDisturb(settingInfo.getMute() ? 1 : 0);
                    ConversionSettingHelper.getInstance().insertSetEntity(setEntity);
                    boolean notice = Integer.valueOf(1).equals(setEntity.getDisturb());
                    seSwitchLayout(findViewById(R.id.mute), getResources().getString(R.string.Chat_Mute_Notification), notice);

                    GroupEntity groupEntity = ContactHelper.getInstance().loadGroupEntity(value);
                    if (groupEntity != null) {
                        groupEntity.setVerify(settingInfo.getPublic()?1:0);
                        groupEntity.setAvatar(settingInfo.getAvatar());
                        ContactHelper.getInstance().inserGroupEntity(groupEntity);
                    }

                    if (settingInfo.getPublic()) {
                        GroupMemberEntity myMember = ContactHelper.getInstance().loadGroupMemByAds(groupKey, MemoryDataManager.getInstance().getAddress());
                        if (myMember == null || myMember.getRole() == 0) {
                            findViewById(R.id.groupset_groupname).setEnabled(false);
                        } else {
                            findViewById(R.id.groupset_groupname).setEnabled(true);
                        }
                    } else {
                        findViewById(R.id.groupset_groupname).setEnabled(true);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Connect.HttpResponse response) {

            }
        });
    }

    protected void groupVerify(View view, int img, String title, String name) {
        ImageView img1 = (ImageView) view.findViewById(R.id.img1);
        TextView txt1 = (TextView) view.findViewById(R.id.txt1);
        TextView txt2 = (TextView) view.findViewById(R.id.txt2);
        ImageView img2 = (ImageView) view.findViewById(R.id.img2);

        img1.setBackgroundResource(img);
        txt1.setText(title);
        if (!TextUtils.isEmpty(name)) {
            txt2.setText(name);
        }

        img2.setImageResource(R.mipmap.group_item_arrow);
        view.setTag(title);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = (String) v.getTag();
                if (title.equals(getString(R.string.Link_Group_Name))) {
                    GroupNameActivity.startActivity(activity, groupKey);
                } else if (title.equals(getString(R.string.Link_My_Alias_in_Group))) {
                    GroupMyNameActivity.startActivity(activity, groupKey);
                } else if (title.equals(getString(R.string.Link_Group_is_QR_Code))) {
                    GroupQRActivity.startActivity(activity, groupKey);
                } else if (title.equals(getString(R.string.Link_ManageGroup))) {
                    GroupManageActivity.startActivity(activity, groupKey);
                }
            }
        });
    }

    protected void seSwitchLayout(View view, String name, boolean state) {
        TextView txt = (TextView) view.findViewById(R.id.txt);
        txt.setText(name);

        View topToggle = view.findViewById(R.id.toggle);
        topToggle.setSelected(state);
        topToggle.setTag(name);
        topToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = (String) v.getTag();
                v.setSelected(!v.isSelected());
                if (getResources().getString(R.string.Chat_Sticky_on_Top_chat).equals(name)) {
                    int top = v.isSelected() ? 1 : 0;
                    chatRoomEntity.setTop(top);
                    ConversionHelper.getInstance().insertRoomEntity(chatRoomEntity);
                } else if (getResources().getString(R.string.Chat_Mute_Notification).equals(name)) {
                    updateGroupMute(v.isSelected());
                } else if (getResources().getString(R.string.Link_Save_to_Contacts).equals(name)) {
                    commonGroup(v.isSelected());
                }
            }
        });
    }

    protected void setOtherLayout(View view, int imgid, String str) {
        ImageView img = (ImageView) view.findViewById(R.id.img);
        img.setBackgroundResource(imgid);
        TextView txt = (TextView) view.findViewById(R.id.txt);
        txt.setText(str);

        view.setTag(str);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tag = (String) v.getTag();
                if (getResources().getString(R.string.Link_Clear_Chat_History).equals(tag)) {
                    ArrayList<String> strings = new ArrayList();
                    strings.add(getString(R.string.Link_Clear_Chat_History));
                    DialogUtil.showBottomListView(activity, strings, new DialogUtil.DialogListItemClickListener() {
                        @Override
                        public void confirm(AdapterView<?> parent, View view, int position) {
                            ConversionHelper.getInstance().deleteRoom(groupKey);
                            RecExtBean.sendRecExtMsg(RecExtBean.ExtType.CLEAR_HISTORY);
                        }
                    });
                } else if (getResources().getString(R.string.Link_Delete_and_Leave).equals(tag)) {
                    exitGroupRequest();
                }
            }
        });
    }

    protected void showMemberList(LinearLayout layout) {
        List<GroupMemberEntity> entities = groupMemEntities;

        GroupMemberEntity addEntity = new GroupMemberEntity();
        addEntity.setIdentifier(TAG_ADD);
        addEntity.setAvatar(TAG_ADD);
        addEntity.setAddress(TAG_ADD);
        entities.add(addEntity);

        for (GroupMemberEntity entity : entities) {
            View view = LayoutInflater.from(activity).inflate(R.layout.linear_contact, null);
            RoundedImageView headimg = (RoundedImageView) view.findViewById(R.id.roundimg);
            ImageView adminImg = (ImageView) view.findViewById(R.id.img1);
            TextView name = (TextView) view.findViewById(R.id.name);

            if (entity.getRole() != null && entity.getRole() == 1) {
                adminImg.setVisibility(View.VISIBLE);
            } else {
                adminImg.setVisibility(View.GONE);
            }

            String nameTxt = TextUtils.isEmpty(entity.getUsername()) ? entity.getNick() : entity.getUsername();
            if (TextUtils.isEmpty(nameTxt)) {
                name.setVisibility(View.GONE);
            } else {
                name.setVisibility(View.VISIBLE);
                name.setText(nameTxt);
            }

            if (TAG_ADD.equals(entity.getAvatar())) {
                GlideUtil.loadImage(headimg, R.mipmap.message_add_friends2x);
            } else {
                GlideUtil.loadAvater(headimg, entity.getAvatar());
            }

            view.setTag(entity.getAddress());
            layout.addView(view);
            view.setOnClickListener(contactClick);
        }
    }

    protected void updateGroupMute(final boolean state) {
        Connect.UpdateGroupMute groupMute = Connect.UpdateGroupMute.newBuilder()
                .setIdentifier(groupKey)
                .setMute(state).build();
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.CONNECT_GROUP_MUTE, groupMute, new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                int disturb = state ? 1 : 0;
                setEntity.setDisturb(disturb);
                ConversionSettingHelper.getInstance().insertSetEntity(setEntity);
            }

            @Override
            public void onError(Connect.HttpResponse response) {
                seSwitchLayout(findViewById(R.id.mute), getResources().getString(R.string.Chat_Mute_Notification), !state);
            }
        });
    }

    protected void exitGroupRequest() {
        DialogUtil.showAlertTextView(activity,
                getResources().getString(R.string.Set_tip_title),
                getResources().getString(R.string.Link_Delete_and_Leave),
                "", "", false, new DialogUtil.OnItemClickListener() {
                    @Override
                    public void confirm(String value) {
                        Connect.GroupId groupId = Connect.GroupId.newBuilder().setIdentifier(groupKey).build();
                        OkHttpUtil.getInstance().postEncrySelf(UriUtil.GROUP_QUIT, groupId, new ResultCall<Connect.HttpResponse>() {
                            @Override
                            public void onResponse(Connect.HttpResponse response) {
                                RecExtBean.sendRecExtMsg(RecExtBean.ExtType.CLEAR_HISTORY);

                                ContactHelper.getInstance().removeGroupInfos(groupKey);
                                //FileUtil.deleteDirectory();
                                ActivityUtil.backActivityWithClearTop(activity, HomeActivity.class);
                            }

                            @Override
                            public void onError(Connect.HttpResponse response) {

                            }
                        });
                    }

                    @Override
                    public void cancel() {

                    }
                });
    }

    protected void commonGroup(final boolean ischeck) {
        Connect.GroupId groupId = Connect.GroupId.newBuilder().setIdentifier(groupKey).build();
        String coomonUrl = ischeck ? UriUtil.GROUP_COMMON : UriUtil.GROUP_RECOMMON;
        OkHttpUtil.getInstance().postEncrySelf(coomonUrl, groupId, new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                int common = ischeck ? 1 : 0;
                GroupEntity groupEntity = ContactHelper.getInstance().loadGroupEntity(groupKey);
                groupEntity.setCommon(common);
                ContactHelper.getInstance().inserGroupEntity(groupEntity);

                ContactNotice.receiverGroup();
            }

            @Override
            public void onError(Connect.HttpResponse response) {
                seSwitchLayout(findViewById(R.id.mute), getResources().getString(R.string.Link_Save_to_Contacts), !ischeck);
            }
        });
    }

    private View.OnClickListener contactClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String address = (String) v.getTag();
            if (TAG_ADD.equals(address)) {
                ContactSelectActivity.startInviteGroupActivity(activity, groupKey);
            } else if (TAG_MEMBER.equals(address)) {
                GroupMemberActivity.startActivity(activity, groupKey);
            } else {
                if (MemoryDataManager.getInstance().getAddress().equals(address)) {
                    ModifyInfoActivity.startActivity(activity);
                } else {
                    ContactEntity entity = ContactHelper.getInstance().loadFriendEntity(address);
                    if (entity == null) {
                        StrangerInfoActivity.startActivity(activity, address, SourceType.CARD);
                    } else {
                        FriendInfoActivity.startActivity(activity, entity.getPub_key());
                    }
                }
            }
        }
    };
}
