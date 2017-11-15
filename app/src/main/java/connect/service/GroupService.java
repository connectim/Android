package connect.service;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.text.TextUtils;

import com.google.protobuf.InvalidProtocolBufferException;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import connect.activity.contact.bean.ContactNotice;
import connect.activity.home.bean.GroupRecBean;
import connect.database.SharedPreferenceUtil;
import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.DaoHelper.ConversionSettingHelper;
import connect.database.green.bean.ContactEntity;
import connect.database.green.bean.ConversionSettingEntity;
import connect.database.green.bean.GroupEntity;
import connect.database.green.bean.GroupMemberEntity;
import connect.utils.ProtoBufUtil;
import connect.utils.RegularUtil;
import connect.utils.StringUtil;
import connect.utils.UriUtil;
import connect.utils.cryption.DecryptionUtil;
import connect.utils.cryption.EncryptionUtil;
import connect.utils.cryption.SupportKeyUril;
import connect.utils.log.LogManager;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import instant.utils.manager.FailMsgsManager;
import protos.Connect;

public class GroupService extends Service {

    private static String TAG = "_GroupService";

    private GroupService service;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        service = this;
        EventBus.getDefault().register(this);
    }

    public static void startService(Activity activity) {
        Intent intent = new Intent(activity, GroupService.class);
        activity.startService(intent);
    }

    public static void stopServer(Context context) {
        Intent intent = new Intent(context, GroupService.class);
        context.stopService(intent);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(GroupRecBean groupRecBean) {
        Object[] objects = null;
        if (groupRecBean.obj != null) {
            objects = (Object[]) groupRecBean.obj;
        }

        switch (groupRecBean.groupRecType) {
            case GroupInfo://get group information
                groupInfo((String) objects[0]);
                break;
            case UpLoadBackUp://upload group backup
                groupBackUp((String) objects[0], (String) objects[1]);
                break;
            case DownBackUp://download backup by myselt
                downloadBackUp((String) objects[0]);
                break;
            case DownGroupBackUp://download backup by group
                downloadGroupBackUp((String) objects[0]);
                break;
            case GroupNotificaton:
                updateGroupMute((String) objects[0], (Integer) objects[1]);
                break;
        }
    }

    public void groupInfo(String pubkey) {
        Connect.GroupId groupId = Connect.GroupId.newBuilder()
                .setIdentifier(pubkey)
                .build();

        OkHttpUtil.getInstance().postEncrySelf(UriUtil.GROUP_PULLINFO, groupId, new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                try {
                    Connect.IMResponse imResponse = Connect.IMResponse.parseFrom(response.getBody().toByteArray());
                    Connect.StructData structData = DecryptionUtil.decodeAESGCMStructData(imResponse.getCipherData());
                    Connect.GroupInfo groupInfo = Connect.GroupInfo.parseFrom(structData.getPlainData());
                    if (ProtoBufUtil.getInstance().checkProtoBuf(groupInfo)) {
                        Connect.Group group = groupInfo.getGroup();
                        String groupIdentifier = group.getIdentifier();

                        GroupEntity groupEntity = ContactHelper.getInstance().loadGroupEntity(groupIdentifier);
                        if (groupEntity == null) {
                            groupEntity = new GroupEntity();
                            groupEntity.setIdentifier(groupIdentifier);
                            String groupname = group.getName();
                            if (TextUtils.isEmpty(groupname)) {
                                groupname = "groupname9";
                            }
                            groupEntity.setName(groupname);
                            groupEntity.setVerify(groupInfo.getGroup().getReviewed() ? 1 : 0);
                            groupEntity.setAvatar(RegularUtil.groupAvatar(group.getIdentifier()));
                            ContactHelper.getInstance().inserGroupEntity(groupEntity);
                        }

                        Map<String, GroupMemberEntity> memberEntityMap = new HashMap<>();
                        for (Connect.GroupMember member : groupInfo.getMembersList()) {
                            GroupMemberEntity memEntity = new GroupMemberEntity();
                            memEntity.setIdentifier(groupIdentifier);
                            memEntity.setUid(member.getUid());
                            memEntity.setAvatar(member.getAvatar());
                            memEntity.setNick(member.getUsername());
                            memEntity.setUsername(member.getUsername());
                            memEntity.setRole(member.getRole());
                            memberEntityMap.put(member.getUid(), memEntity);
                        }
                        Collection<GroupMemberEntity> memberEntityCollection = memberEntityMap.values();
                        List<GroupMemberEntity> memEntities = new ArrayList<GroupMemberEntity>(memberEntityCollection);
                        ContactHelper.getInstance().inserGroupMemEntity(memEntities);

                        ContactHelper.getInstance().inserGroupMemEntity(memEntities);
                        GroupRecBean.sendGroupRecMsg(GroupRecBean.GroupRecType.DownBackUp, groupIdentifier);
                        ContactNotice.receiverGroup();
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

    public void groupBackUp(String groupkey, String groupecdhkey) {
        try {
            String ranprikey = SupportKeyUril.getNewPriKey();
            String randpubkey = SupportKeyUril.getPubKeyFromPriKey(ranprikey);

            byte[] ecdhkey = SupportKeyUril.getRawECDHKey(SharedPreferenceUtil.getInstance().getUser().getPriKey(), randpubkey);
            Connect.GcmData gcmData = EncryptionUtil.encodeAESGCM(EncryptionUtil.ExtendedECDH.EMPTY, ecdhkey, groupecdhkey.getBytes("UTF-8"));

            String groupHex = StringUtil.bytesToHexString(gcmData.toByteArray());
            String collaFormat = String.format("%1$s/%2$s", randpubkey, groupHex);

            Connect.GroupCollaborative collaborative = Connect.GroupCollaborative.newBuilder()
                    .setIdentifier(groupkey)
                    .setCollaborative(collaFormat)
                    .build();

            OkHttpUtil.getInstance().postEncrySelf(UriUtil.CONNECT_GROUP_UPLOADKEY, collaborative, new ResultCall<Connect.HttpResponse>() {
                @Override
                public void onResponse(Connect.HttpResponse response) {
                    LogManager.getLogger().d(TAG, "backup success");
                }

                @Override
                public void onError(Connect.HttpResponse response) {
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void downloadBackUp(final String pubkey) {
        Connect.GroupId groupId = Connect.GroupId.newBuilder()
                .setIdentifier(pubkey)
                .build();

        OkHttpUtil.getInstance().postEncrySelf(UriUtil.CONNECT_GROUP_DOWNLOAD_KEY, groupId, new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                try {
                    Connect.IMResponse imResponse = Connect.IMResponse.parseFrom(response.getBody().toByteArray());
                    Connect.StructData structData = DecryptionUtil.decodeAESGCMStructData(imResponse.getCipherData());
                    Connect.GroupCollaborative groupCollaborative = Connect.GroupCollaborative.parseFrom(structData.getPlainData().toByteArray());

                    boolean isDownLoadSuccessful = false;
                    if (ProtoBufUtil.getInstance().checkProtoBuf(groupCollaborative)) {
                        String[] infos = groupCollaborative.getCollaborative().split("/");
                        if (infos.length < 2) {

                        } else {
                            byte[] ecdHkey = SupportKeyUril.getRawECDHKey(SharedPreferenceUtil.getInstance().getUser().getPriKey(), infos[0]);
                            Connect.GcmData gcmData = Connect.GcmData.parseFrom(StringUtil.hexStringToBytes(infos[1]));
                            ecdHkey = DecryptionUtil.decodeAESGCM(EncryptionUtil.ExtendedECDH.EMPTY, ecdHkey, gcmData);

                            try {
                                String groupEcdh = new String(ecdHkey, "UTF-8");
                                downGroupBackUpSuccess(pubkey, groupEcdh);
                                isDownLoadSuccessful = false;
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    if (!isDownLoadSuccessful) {
                        GroupRecBean.sendGroupRecMsg(GroupRecBean.GroupRecType.DownGroupBackUp, pubkey);
                    }
                } catch (InvalidProtocolBufferException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Connect.HttpResponse response) {
                GroupRecBean.sendGroupRecMsg(GroupRecBean.GroupRecType.DownGroupBackUp, pubkey);
            }
        });
    }

    public void downloadGroupBackUp(final String pubkey) {
        Connect.GroupId groupId = Connect.GroupId.newBuilder()
                .setIdentifier(pubkey)
                .build();

        OkHttpUtil.getInstance().postEncrySelf(UriUtil.CONNECT_GROUP_BACKUP, groupId, new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                try {
                    Connect.IMResponse imResponse = Connect.IMResponse.parseFrom(response.getBody().toByteArray());
                    Connect.StructData structData = DecryptionUtil.decodeAESGCMStructData(imResponse.getCipherData());
                    Connect.DownloadBackUpResp backUpResp = Connect.DownloadBackUpResp.parseFrom(structData.getPlainData().toByteArray());
                    if (!ProtoBufUtil.getInstance().checkProtoBuf(backUpResp)) {
                        return;
                    }

                    String[] infos = backUpResp.getBackup().split("/");
                    if (infos.length >= 2) {
                        byte[] ecdHkey = SupportKeyUril.getRawECDHKey(SharedPreferenceUtil.getInstance().getUser().getPriKey(), infos[0]);
                        Connect.GcmData gcmData = Connect.GcmData.parseFrom(StringUtil.hexStringToBytes(infos[1]));
                        structData = DecryptionUtil.decodeAESGCMStructData(EncryptionUtil.ExtendedECDH.EMPTY, ecdHkey, gcmData);
                        if (structData != null) {
                            Connect.CreateGroupMessage groupMessage = Connect.CreateGroupMessage.parseFrom(structData.getPlainData().toByteArray());
                            String groupEcdh = groupMessage.getSecretKey();
                            downGroupBackUpSuccess(pubkey, groupEcdh);

                            GroupRecBean.sendGroupRecMsg(GroupRecBean.GroupRecType.UpLoadBackUp, pubkey, groupEcdh);
                        }
                    }
                } catch (InvalidProtocolBufferException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Connect.HttpResponse response) {

            }
        });
    }

    public void downGroupBackUpSuccess(String groupkey, String ecdhkey) {
        GroupEntity groupEntity = ContactHelper.getInstance().loadGroupEntity(groupkey);
        if (groupEntity != null) {
            groupEntity.setEcdh_key(ecdhkey);

            String groupname = groupEntity.getName();
            if (TextUtils.isEmpty(groupname)) {
                groupname = "groupname10";
            }
            groupEntity.setName(groupname);
            ContactHelper.getInstance().inserGroupEntity(groupEntity);
            FailMsgsManager.getInstance().receiveFailMsgs(groupkey);
        }
    }

    private void updateGroupMute(final String groupkey, final int state) {
        Connect.UpdateGroupMute groupMute = Connect.UpdateGroupMute.newBuilder()
                .setIdentifier(groupkey)
                .setMute(state == 1)
                .build();

        OkHttpUtil.getInstance().postEncrySelf(UriUtil.CONNECT_GROUP_MUTE, groupMute, new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                ConversionSettingEntity setEntity = ConversionSettingHelper.getInstance().loadSetEntity(groupkey);
                setEntity.setDisturb(state);
                ConversionSettingHelper.getInstance().insertSetEntity(setEntity);
            }

            @Override
            public void onError(Connect.HttpResponse response) {
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
