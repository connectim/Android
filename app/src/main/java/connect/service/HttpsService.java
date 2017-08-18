package connect.service;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import connect.database.MemoryDataManager;
import connect.database.SharedPreferenceUtil;
import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.DaoHelper.ConversionSettingHelper;
import connect.database.green.DaoHelper.ParamHelper;
import connect.database.green.DaoHelper.ParamManager;
import connect.database.green.bean.ConversionSettingEntity;
import connect.database.green.bean.GroupEntity;
import connect.database.green.bean.GroupMemberEntity;
import connect.im.model.FailMsgsManager;
import connect.ui.activity.R;
import connect.activity.contact.bean.ContactNotice;
import connect.activity.home.bean.EstimatefeeBean;
import connect.activity.home.bean.HttpRecBean;
import connect.activity.set.bean.PaySetBean;
import connect.activity.set.bean.PrivateSetBean;
import connect.activity.wallet.bean.RateBean;
import connect.utils.ProtoBufUtil;
import connect.utils.RegularUtil;
import connect.utils.StringUtil;
import connect.utils.TimeUtil;
import connect.utils.UriUtil;
import connect.utils.cryption.DecryptionUtil;
import connect.utils.cryption.EncryptionUtil;
import connect.utils.cryption.SupportKeyUril;
import connect.utils.data.RateDataUtil;
import connect.utils.log.LogManager;
import connect.utils.okhttp.HttpRequest;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import connect.utils.system.SystemDataUtil;
import connect.utils.system.SystemUtil;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import protos.Connect;
import wallet_gateway.WalletOuterClass;

/**
 * Background processing HTTP requests
 * Created by gtq on 2016/12/23.
 */
public class HttpsService extends Service {

    private String Tag = "HttpService";
    private HttpsService service;

    private SoundPool soundPool = null;

    @Nullable
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
        Intent intent = new Intent(activity, HttpsService.class);
        activity.startService(intent);
    }

    public static void stopServer(Context context) {
        Intent intent = new Intent(context, HttpsService.class);
        context.stopService(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogManager.getLogger().d(Tag, "***  onStartCommand start  ***");

        initSoundPool();
        SocketService.startService(service);
        PushService.startService(service);

        String index = ParamManager.getInstance().getString(ParamManager.GENERATE_TOKEN_SALT);
        if (TextUtils.isEmpty(index)) {
            HttpRecBean.sendHttpRecMsg(HttpRecBean.HttpRecType.SALTEXPIRE);
        } else {
            verifySalt();
            loginSuccessHttp();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    /** salt timeout */
    private static final int SALT_TIMEOUT = 100;

    private Handler serviceHandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 102:
                    HttpRecBean.sendHttpRecMsg(HttpRecBean.HttpRecType.SALTEXPIRE);
                    break;
            }
        }
    };

    public void loginSuccessHttp() {
        //get payment set
        if (ParamManager.getInstance().getPaySet() == null) {
            HttpsService.sendPaySet();
        }

        //get private set
        if (ParamManager.getInstance().getPrivateSet() == null) {
            HttpsService.sendPrivateSet();
        }

        HttpsService.sendEstimatefee();

        if (ParamHelper.getInstance().loadParamEntity(ParamManager.COUNTRY_RATE) == null) {
            String countryCode = SystemDataUtil.getCountryCode();
            if(!TextUtils.isEmpty(countryCode)){
                RateBean rateBean = RateDataUtil.getInstance().getRate(countryCode);
                ParamManager.getInstance().putCountryRate(rateBean);
            }
        }

        HttpsService.sendBlackList();
        requestEstimatefee();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public synchronized void onEventMainThread(HttpRecBean httpRec) {
        Object[] objects = null;
        if (httpRec.obj != null) {
            objects = (Object[]) httpRec.obj;
        }

        switch (httpRec.httpRecType) {
            case SALTEXPIRE://salt time
                connectSalt();
                break;
            case GroupInfo://get group information
                groupInfo((String) objects[0]);
                break;
            case UpLoadBackUp://upload group backup
                groupBackUp((String) objects[0], (String) objects[1]);
                break;
            case PaySet://pay set
                requestSetPayInfo();
                break;
            case PrivateSet://private set
                requestPrivateInfo();
                break;
            case BlackList://black list
                requestBlackList();
                break;
            case Estimate://fee
                requestEstimatefee();
                break;
            case DownBackUp://download backup by myselt
                downloadBackUp((String) objects[0]);
                break;
            case DownGroupBackUp://download backup by group
                downloadGroupBackUp((String) objects[0]);
                break;
            case SOUNDPOOL:
                playSystemVoice((Integer) objects[0]);
                break;
            case SYSTEM_VIBRATION:
                SystemUtil.noticeVibrate(service);
                break;
            case GroupNotificaton:
                updateGroupMute((String) objects[0], (Integer) objects[1]);
                break;
            case WALLET_DEFAULT_ADDRESS:
                getCurrencyDefaultAddress();
                break;
            case WALLET_CURRENCY_SET:
                updateCurrency();
                break;
        }
    }

    public void initSoundPool() {
        soundPool = new SoundPool(10, AudioManager.STREAM_SYSTEM, 5);
        soundPool.load(service, R.raw.instant_message, 1);
    }

    public void playSystemVoice(int state) {
        if (state == 0) {
            SystemUtil.noticeVoice(service);
        } else {
            if (soundPool != null) {
                soundPool.play(1, 7, 7, 0, 0, 1);
            }
        }
    }

    public static void sendPaySet() {
        ParamManager.getInstance().putPaySet(PaySetBean.initPaySet());
        HttpRecBean.sendHttpRecMsg(HttpRecBean.HttpRecType.PaySet, "");
    }

    public static void sendPrivateSet() {
        HttpRecBean.sendHttpRecMsg(HttpRecBean.HttpRecType.PrivateSet, "");
    }

    public static void sendBlackList() {
        HttpRecBean.sendHttpRecMsg(HttpRecBean.HttpRecType.BlackList, "");
    }

    public static void sendEstimatefee() {
        EstimatefeeBean estimatefeeBean = SharedPreferenceUtil.getInstance().getEstimatefee();
        if (estimatefeeBean != null) {
            long day = estimatefeeBean.getTime() / (1000 * 60 * 60 * 24);
            long currentDay = TimeUtil.getCurrentTimeInLong() / (1000 * 60 * 60 * 24);
            if ((currentDay - day) > 1) {
                return;
            }
        }
        HttpRecBean.sendHttpRecMsg(HttpRecBean.HttpRecType.Estimate, "");
    }

    public void verifySalt() {
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.CONNECT_USERS_EXPIRE_SALT, ByteString.copyFrom(new byte[]{}), new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                String prikey = MemoryDataManager.getInstance().getPriKey();
                try {
                    Connect.IMResponse imResponse = Connect.IMResponse.parseFrom(response.getBody().toByteArray());
                    Connect.StructData structData = DecryptionUtil.decodeAESGCMStructData(SupportKeyUril.EcdhExts.SALT, prikey, imResponse.getCipherData());
                    Connect.GenerateTokenResponse tokenResponse = Connect.GenerateTokenResponse.parseFrom(structData.getPlainData());
                    if(ProtoBufUtil.getInstance().checkProtoBuf(tokenResponse)){
                        if (tokenResponse.getExpired() <= 400) {
                            HttpRecBean.sendHttpRecMsg(HttpRecBean.HttpRecType.SALTEXPIRE);
                        } else {
                            serviceHandler.removeMessages(SALT_TIMEOUT);
                            serviceHandler.sendEmptyMessageDelayed(SALT_TIMEOUT, tokenResponse.getExpired());
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Connect.HttpResponse response) {
                LogManager.getLogger().d(Tag, response.getMessage());
            }
        });
    }

    public void connectSalt() {
        final byte[] bytes = SecureRandom.getSeed(64);

        Connect.GenerateToken generateToken = Connect.GenerateToken.newBuilder().setSalt(ByteString.copyFrom(bytes)).build();
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.CONNECT_USER_SALT, generateToken, SupportKeyUril.EcdhExts.EMPTY,
                new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                String prikey = MemoryDataManager.getInstance().getPriKey();
                try {
                    Connect.IMResponse imResponse = Connect.IMResponse.parseFrom(response.getBody().toByteArray());
                    Connect.StructData structData = DecryptionUtil.decodeAESGCMStructData(SupportKeyUril.EcdhExts.EMPTY, prikey, imResponse.getCipherData());
                    Connect.GenerateTokenResponse tokenResponse = Connect.GenerateTokenResponse.parseFrom(structData.getPlainData());
                    if(ProtoBufUtil.getInstance().checkProtoBuf(tokenResponse)){
                        byte[] salts = SupportKeyUril.xor(bytes, tokenResponse.getSalt().toByteArray());
                        ParamManager.getInstance().putValue(ParamManager.GENERATE_TOKEN_SALT, StringUtil.bytesToHexString(salts));
                        ParamManager.getInstance().putValue(ParamManager.GENERATE_TOKEN_EXPIRED, String.valueOf(tokenResponse.getExpired()));
                        loginSuccessHttp();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Connect.HttpResponse response) {
                LogManager.getLogger().d(Tag, response.getMessage());
            }
        });
    }

    public void groupInfo(String pubkey) {
        Connect.GroupId groupId = Connect.GroupId.newBuilder().setIdentifier(pubkey).build();
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.GROUP_PULLINFO, groupId, new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                try {
                    Connect.IMResponse imResponse = Connect.IMResponse.parseFrom(response.getBody().toByteArray());
                    Connect.StructData structData = DecryptionUtil.decodeAESGCMStructData(imResponse.getCipherData());
                    Connect.GroupInfo groupInfo = Connect.GroupInfo.parseFrom(structData.getPlainData());
                    if (ProtoBufUtil.getInstance().checkProtoBuf(groupInfo)) {
                        Connect.Group group = groupInfo.getGroup();
                        String pubkey = group.getIdentifier();

                        GroupEntity groupEntity = ContactHelper.getInstance().loadGroupEntity(pubkey);
                        if (groupEntity == null) {
                            groupEntity = new GroupEntity();
                            groupEntity.setIdentifier(pubkey);
                            String groupname = group.getName();
                            if (TextUtils.isEmpty(groupname)) {
                                groupname = "groupname9";
                            }
                            groupEntity.setName(groupname);
                            groupEntity.setVerify(groupInfo.getGroup().getReviewed() ? 1 : 0);
                            groupEntity.setAvatar(RegularUtil.groupAvatar(group.getIdentifier()));
                            ContactHelper.getInstance().inserGroupEntity(groupEntity);
                        }

                        List<GroupMemberEntity> memEntities = new ArrayList<>();
                        for (Connect.GroupMember member : groupInfo.getMembersList()) {
                            GroupMemberEntity memEntity = ContactHelper.getInstance().loadGroupMemberEntity(pubkey, member.getAddress());
                            if (memEntity == null) {
                                memEntity = new GroupMemberEntity();
                                memEntity.setIdentifier(pubkey);
                                memEntity.setPub_key(member.getPubKey());
                                memEntity.setAddress(member.getAddress());
                                memEntity.setAvatar(member.getAvatar());
                                memEntity.setNick(member.getUsername());
                                memEntity.setUsername(member.getUsername());
                                memEntity.setRole(member.getRole());
                                memEntities.add(memEntity);
                            }
                        }

                        ContactHelper.getInstance().inserGroupMemEntity(memEntities);
                        HttpRecBean.sendHttpRecMsg(HttpRecBean.HttpRecType.DownBackUp, pubkey);
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
            String ranprikey = EncryptionUtil.randomPriKey();
            String randpubkey = EncryptionUtil.randomPubKey(ranprikey);

            String priKey = MemoryDataManager.getInstance().getPriKey();
            byte[] ecdhkey = SupportKeyUril.rawECDHkey(priKey, randpubkey);
            Connect.GcmData gcmData = EncryptionUtil.encodeAESGCM(SupportKeyUril.EcdhExts.EMPTY, ecdhkey, groupecdhkey.getBytes("UTF-8"));

            String groupHex = StringUtil.bytesToHexString(gcmData.toByteArray());
            String collaFormat = String.format("%1$s/%2$s", randpubkey, groupHex);

            Connect.GroupCollaborative collaborative = Connect.GroupCollaborative.newBuilder()
                    .setIdentifier(groupkey)
                    .setCollaborative(collaFormat).build();
            OkHttpUtil.getInstance().postEncrySelf(UriUtil.CONNECT_GROUP_UPLOADKEY, collaborative, new ResultCall<Connect.HttpResponse>() {
                @Override
                public void onResponse(Connect.HttpResponse response) {
                    LogManager.getLogger().d(Tag, "backup success");
                }

                @Override
                public void onError(Connect.HttpResponse response) {
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void downloadBackUp(final String pubkey) {
        Connect.GroupId groupId = Connect.GroupId.newBuilder()
                .setIdentifier(pubkey).build();
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.CONNECT_GROUP_DOWNLOAD_KEY, groupId, new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                try {
                    Connect.IMResponse imResponse = Connect.IMResponse.parseFrom(response.getBody().toByteArray());
                    Connect.StructData structData = DecryptionUtil.decodeAESGCMStructData(imResponse.getCipherData());
                    Connect.GroupCollaborative groupCollaborative = Connect.GroupCollaborative.parseFrom(structData.getPlainData().toByteArray());
                    if(!ProtoBufUtil.getInstance().checkProtoBuf(groupCollaborative)){
                        return;
                    }
                    String[] infos = groupCollaborative.getCollaborative().split("/");
                    if (infos.length < 2) {
                        HttpRecBean.sendHttpRecMsg(HttpRecBean.HttpRecType.DownGroupBackUp, pubkey);
                    } else {
                        byte[] ecdHkey = SupportKeyUril.rawECDHkey(MemoryDataManager.getInstance().getPriKey(), infos[0]);
                        Connect.GcmData gcmData = Connect.GcmData.parseFrom(StringUtil.hexStringToBytes(infos[1]));
                        ecdHkey = DecryptionUtil.decodeAESGCM(SupportKeyUril.EcdhExts.EMPTY, ecdHkey, gcmData);

                        try {
                            String groupEcdh = new String(ecdHkey, "UTF-8");
                            downGroupBackUpSuccess(pubkey, groupEcdh);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } catch (InvalidProtocolBufferException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Connect.HttpResponse response) {
                HttpRecBean.sendHttpRecMsg(HttpRecBean.HttpRecType.DownGroupBackUp, pubkey);
            }
        });
    }

    public void downloadGroupBackUp(final String pubkey) {
        Connect.GroupId groupId = Connect.GroupId.newBuilder()
                .setIdentifier(pubkey).build();
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.CONNECT_GROUP_BACKUP, groupId, new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                try {
                    Connect.IMResponse imResponse = Connect.IMResponse.parseFrom(response.getBody().toByteArray());
                    Connect.StructData structData = DecryptionUtil.decodeAESGCMStructData(imResponse.getCipherData());
                    Connect.DownloadBackUpResp backUpResp = Connect.DownloadBackUpResp.parseFrom(structData.getPlainData().toByteArray());
                    if(!ProtoBufUtil.getInstance().checkProtoBuf(backUpResp)){
                        return;
                    }

                    String[] infos = backUpResp.getBackup().split("/");
                    if (infos.length >= 2) {
                        byte[] ecdHkey = SupportKeyUril.rawECDHkey(MemoryDataManager.getInstance().getPriKey(), infos[0]);
                        Connect.GcmData gcmData = Connect.GcmData.parseFrom(StringUtil.hexStringToBytes(infos[1]));
                        structData = DecryptionUtil.decodeAESGCMStructData(SupportKeyUril.EcdhExts.EMPTY, ecdHkey, gcmData);
                        Connect.CreateGroupMessage groupMessage = Connect.CreateGroupMessage.parseFrom(structData.getPlainData().toByteArray());
                        String groupEcdh = groupMessage.getSecretKey();
                        downGroupBackUpSuccess(pubkey, groupEcdh);

                        HttpRecBean.sendHttpRecMsg(HttpRecBean.HttpRecType.UpLoadBackUp, pubkey, groupEcdh);
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

    public void requestSetPayInfo() {
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.SETTING_PAY_SUNC, ByteString.copyFrom(SupportKeyUril.createrBinaryRandom()),
                new ResultCall<Connect.HttpResponse>() {
                    @Override
                    public void onResponse(Connect.HttpResponse response) {
                        try {
                            Connect.IMResponse imResponse = Connect.IMResponse.parseFrom(response.getBody().toByteArray());
                            Connect.StructData structData = DecryptionUtil.decodeAESGCMStructData(imResponse.getCipherData());
                            PaySetBean paySetBean = null;
                            if (structData == null || structData.getPlainData() == null) {
                                paySetBean = PaySetBean.initPaySet();
                            } else {
                                Connect.PaymentSetting paymentSetting = Connect.PaymentSetting.parseFrom(structData.getPlainData());
                                paySetBean = new PaySetBean(paymentSetting);
                                paySetBean.setAutoFee(false);
                            }
                            ParamManager.getInstance().putPaySet(paySetBean);
                            getPayVersion(paySetBean);
                        } catch (InvalidProtocolBufferException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Connect.HttpResponse response) {

                    }
                });
    }

    private void getPayVersion(final PaySetBean paySetBean){
        Connect.PayPinVersion payPinVersion = Connect.PayPinVersion.newBuilder()
                .setVersion("0")
                .build();
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.SETTING_PAY_VERSION, payPinVersion, new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                try {
                    Connect.IMResponse imResponse = Connect.IMResponse.parseFrom(response.getBody().toByteArray());
                    Connect.StructData structData = DecryptionUtil.decodeAESGCMStructData(imResponse.getCipherData());
                    Connect.PayPinVersion payPinVersion = Connect.PayPinVersion.parseFrom(structData.getPlainData());
                    if(ProtoBufUtil.getInstance().checkProtoBuf(payPinVersion)){
                        paySetBean.setVersionPay(payPinVersion.getVersion());
                        ParamManager.getInstance().putPaySet(paySetBean);
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

    public void requestPrivateInfo() {
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.SETTING_PRIVACY_INFO, ByteString.copyFrom(new byte[]{}),
                new ResultCall<Connect.HttpResponse>() {
                    @Override
                    public void onResponse(Connect.HttpResponse response) {
                        try {
                            Connect.IMResponse imResponse = Connect.IMResponse.parseFrom(response.getBody().toByteArray());
                            Connect.StructData structData = DecryptionUtil.decodeAESGCMStructData(imResponse.getCipherData());
                            PrivateSetBean privateSetBean = null;
                            if (structData == null) {
                                privateSetBean = PrivateSetBean.initSetBean();
                            } else {
                                Connect.Privacy privacy = Connect.Privacy.parseFrom(structData.getPlainData());
                                privateSetBean = new PrivateSetBean();
                                privateSetBean.setPhoneFind(privacy.getPhoneNum());
                                privateSetBean.setAddressFind(privacy.getAddress());
                                privateSetBean.setRecommend(privacy.getRecommend());
                            }
                            ParamManager.getInstance().putPrivateSet(privateSetBean);
                        } catch (InvalidProtocolBufferException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Connect.HttpResponse response) {

                    }
                });
    }

    public void requestEstimatefee() {
        HttpRequest.getInstance().get(UriUtil.CONNECT_V1_ESTIMATEFEE, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String tempResponse =  response.body().string();
                int code = 0;
                try {
                    JSONObject jsonObject = new JSONObject(tempResponse);
                    code = jsonObject.getInt("code");
                    if (code == 2000) {
                        Type type = new TypeToken<EstimatefeeBean>() {}.getType();
                        EstimatefeeBean estimatefeeBean = new Gson().fromJson(jsonObject.toString(), type);
                        estimatefeeBean.setTime(TimeUtil.getCurrentTimeInLong());
                        SharedPreferenceUtil.getInstance().putEstimatefee(estimatefeeBean);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void requestBlackList() {
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.CONNEXT_V1_BLACKLIST_LIST, ByteString.copyFrom(new byte[]{}),
                new ResultCall<Connect.HttpResponse>() {
                    @Override
                    public void onResponse(Connect.HttpResponse response) {
                        try {
                            Connect.IMResponse imResponse = Connect.IMResponse.parseFrom(response.getBody().toByteArray());
                            Connect.StructData structData = DecryptionUtil.decodeAESGCMStructData(imResponse.getCipherData());
                            if(structData == null || structData.getPlainData() == null){
                                return;
                            }
                            Connect.UsersInfo usersInfo = Connect.UsersInfo.parseFrom(structData.getPlainData());
                            List<Connect.UserInfo> list = usersInfo.getUsersList();
                            for (Connect.UserInfo info : list) {
                                ContactHelper.getInstance().updataFriendBlack(info.getPubKey());
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

    private void updateGroupMute(final String groupkey, final int state) {
        Connect.UpdateGroupMute groupMute = Connect.UpdateGroupMute.newBuilder()
                .setIdentifier(groupkey)
                .setMute(state == 1).build();
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

    private void getCurrencyDefaultAddress() {
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.WALLET_V2_COINS_ADDRESS_GET_DEFAULT, ByteString.copyFrom(new byte[]{}), new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                try {
                    Connect.IMResponse imResponse = Connect.IMResponse.parseFrom(response.getBody().toByteArray());
                    Connect.StructData structData = DecryptionUtil.decodeAESGCMStructData(imResponse.getCipherData());
//                    WalletOuterClass.ListDefaultAddress createCoinInfo = WalletOuterClass.ListDefaultAddress.parseFrom(structData.getPlainData());
//                    if (ProtoBufUtil.getInstance().checkProtoBuf(createCoinInfo)) {
//
//                    }
                } catch (InvalidProtocolBufferException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Connect.HttpResponse response) {

            }
        });
    }

    private void updateCurrency() {
        WalletOuterClass.Coin coin = WalletOuterClass.Coin.newBuilder()
                .setCurrency(0)
                .setStatus(0).build();

        OkHttpUtil.getInstance().postEncrySelf(UriUtil.WALLET_V2_COINS_CURRENCY_SET, coin, new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                try {
                    Connect.IMResponse imResponse = Connect.IMResponse.parseFrom(response.getBody().toByteArray());
                    Connect.StructData structData = DecryptionUtil.decodeAESGCMStructData(imResponse.getCipherData());
//                    WalletOuterClass.ListDefaultAddress createCoinInfo = WalletOuterClass.ListDefaultAddress.parseFrom(structData.getPlainData());
//                    if (ProtoBufUtil.getInstance().checkProtoBuf(createCoinInfo)) {
//
//                    }
                } catch (InvalidProtocolBufferException e) {
                    e.printStackTrace();
                }
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
