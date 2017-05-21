package connect.ui.activity.chat.view.holder;

import android.app.Activity;
import android.view.View;

import com.google.gson.Gson;

import connect.db.MemoryDataManager;
import connect.db.SharedPreferenceUtil;
import connect.db.green.DaoHelper.MessageHelper;
import connect.db.green.bean.MessageEntity;
import connect.ui.activity.R;
import connect.ui.activity.chat.bean.MsgDefinBean;
import connect.ui.activity.chat.bean.MsgEntity;
import connect.ui.activity.chat.bean.TransferExt;
import connect.ui.activity.wallet.PacketDetailActivity;
import connect.utils.DialogUtil;
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

    private boolean playAnim = false;

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

                    if (playAnim) {
                        return;
                    }

                    Connect.StructData structData = DecryptionUtil.decodeAESGCMStructData(imResponse.getCipherData());
                    Connect.GrabRedPackageResp packageResp = Connect.GrabRedPackageResp.parseFrom(structData.getPlainData());
                    switch (packageResp.getStatus()) {
                        case 0://fail
                            playAnim=true;
                            DialogUtil.showRedPacketState(context, 0, new DialogUtil.OnGifListener() {
                                @Override
                                public void click() {
                                    startPacketDetail(hashid, systemPacket ? 1 : 0);
                                    playAnim=false;
                                }
                            });
                            break;
                        case 1://get it
                            playAnim=true;
                            DialogUtil.showRedPacketState(context, 1, new DialogUtil.OnGifListener() {
                                @Override
                                public void click() {
                                    startPacketDetail(hashid, systemPacket ? 1 : 0);
                                    playAnim=false;
                                }
                            });
                            break;
                        case 2://Have got it , display  the details
                            playAnim=false;
                            startPacketDetail(hashid, systemPacket ? 1 : 0);
                            break;
                        case 3://fail
                            break;
                        case 4://have no lucky packet
                            playAnim=false;
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
