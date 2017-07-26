package connect.activity.chat.view.holder;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;

import com.google.gson.Gson;

import connect.database.green.DaoHelper.MessageHelper;
import connect.database.green.bean.MessageEntity;
import connect.ui.activity.R;
import connect.activity.chat.bean.MsgDefinBean;
import connect.activity.chat.bean.MsgEntity;
import connect.activity.chat.bean.TransferExt;
import connect.activity.wallet.PacketDetailActivity;
import connect.utils.DialogUtil;
import connect.utils.ProtoBufUtil;
import connect.utils.ToastEUtil;
import connect.utils.UriUtil;
import connect.utils.cryption.DecryptionUtil;
import connect.utils.cryption.SupportKeyUril;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import protos.Connect;

/**
 * Created by gtq on 2016/11/23.
 */
public class MsgLuckyHolder extends MsgChatHolder {

    /** check is group lucky packet */
    private boolean systemPacket = false;

    public MsgLuckyHolder(View itemView) {
        super(itemView);
    }

    @Override
    public void buildRowData(MsgBaseHolder msgBaseHolder, final MsgEntity entity) {
        super.buildRowData(msgBaseHolder, entity);
        contentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MsgDefinBean bean = entity.getMsgDefinBean();
                requestLuckPacket(bean.getContent());
            }
        });

        if (entity.getReadstate() == 0) {//do not click
            TransferExt transferExt = new Gson().fromJson(entity.getMsgDefinBean().getExt1(), TransferExt.class);
            if (transferExt != null && transferExt.getType() == 1) {//outer lucky packet
                contentLayout.performClick();
            }
        }
    }

    private Handler handler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 100:
                    contentLayout.setClickable(true);
                    break;
                case 120:
                    contentLayout.setClickable(false);
            }
        }
    };

    /**
     * request to get the lucky packet
     *
     * @param hashid
     */
    private void requestLuckPacket(final String hashid) {
        baseEntity.setReadstate(1);
        MessageEntity msgEntity = MessageHelper.getInstance().loadMsgByMsgid(baseEntity.getMsgDefinBean().getMessage_id());
        if (msgEntity != null) {
            msgEntity.setState(1);
            MessageHelper.getInstance().updateMsg(msgEntity);
        }

        Connect.RedPackageHash packageHash = Connect.RedPackageHash.newBuilder().setId(hashid).build();

        if (context.getString(R.string.app_name).equals(baseEntity.getMsgDefinBean().getPublicKey())) {
            systemPacket = true;
        }

        String uri = (!systemPacket) ? UriUtil.REDPACKAGE_GRAB : UriUtil.WALLET_PACKAGE_GRABSYSTEM;
        OkHttpUtil.getInstance().postEncrySelf(uri, packageHash, new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                try {
                    Connect.IMResponse imResponse = Connect.IMResponse.parseFrom(response.getBody().toByteArray());
                    if (!SupportKeyUril.verifySign(imResponse.getSign(), imResponse.getCipherData().toByteArray())) {
                        throw new Exception("Validation fails");
                    }

                    Connect.StructData structData = DecryptionUtil.decodeAESGCMStructData(imResponse.getCipherData());
                    Connect.GrabRedPackageResp packageResp = Connect.GrabRedPackageResp.parseFrom(structData.getPlainData());
                    if (!ProtoBufUtil.getInstance().checkProtoBuf(packageResp)) {//state default 0
                        packageResp = Connect.GrabRedPackageResp.newBuilder().setStatus(0).build();
                    }
                    switch (packageResp.getStatus()) {
                        case 0://fail
                            handler.sendEmptyMessage(120);
                            DialogUtil.showRedPacketState(context, 0, new DialogUtil.OnGifListener() {
                                @Override
                                public void click() {
                                    startPacketDetail(hashid, systemPacket ? 1 : 0);
                                    handler.sendEmptyMessage(100);
                                }
                            });
                            break;
                        case 1://get it
                            handler.sendEmptyMessage(120);
                            DialogUtil.showRedPacketState(context, 1, new DialogUtil.OnGifListener() {
                                @Override
                                public void click() {
                                    startPacketDetail(hashid, systemPacket ? 1 : 0);
                                    handler.sendEmptyMessage(100);
                                }
                            });
                            break;
                        case 2://Have got it , display  the details
                            startPacketDetail(hashid, systemPacket ? 1 : 0);
                            break;
                        case 3://fail
                            break;
                        case 4://have no lucky packet
                            startPacketDetail(hashid, systemPacket ? 1 : 0);
                            break;
                        case 5://Mobile phone is not bound
                            ToastEUtil.makeText(context,R.string.Chat_Your_account_is_not_bound_to_the_phone,2).show();
                            break;
                        case 6://A phone number can only get once
                            ToastEUtil.makeText(context,R.string.Set_A_phone_number_can_only_grab_once,2).show();
                            break;
                        case 7://PAUSE
                            ToastEUtil.makeText(context,R.string.Chat_system_luckypackage_have_been_frozen,2).show();
                            break;
                        case 8://DEVICELIMIT
                            ToastEUtil.makeText(context,R.string.Chat_one_device_can_only_grab_a_luckypackage,2).show();
                            break;
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

    protected void startPacketDetail(String hashid, int code) {
        PacketDetailActivity.startActivity((Activity) context, hashid, code);
    }
}
