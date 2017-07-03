package connect.ui.activity.chat.exts;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.db.MemoryDataManager;
import connect.db.green.DaoHelper.ContactHelper;
import connect.db.green.DaoHelper.ParamManager;
import connect.db.green.bean.ContactEntity;
import connect.db.green.bean.GroupEntity;
import connect.im.bean.MsgType;
import connect.ui.activity.R;
import connect.ui.activity.chat.bean.MsgSend;
import connect.ui.activity.wallet.PacketHistoryActivity;
import connect.ui.activity.wallet.bean.TransferBean;
import connect.utils.ProtoBufUtil;
import connect.utils.transfer.TransferError;
import connect.utils.transfer.TransferUtil;
import connect.ui.activity.set.PayFeeActivity;
import connect.ui.base.BaseActivity;
import connect.utils.ActivityUtil;
import connect.utils.data.RateFormatUtil;
import connect.utils.ToastEUtil;
import connect.utils.UriUtil;
import connect.utils.cryption.DecryptionUtil;
import connect.utils.glide.GlideUtil;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import connect.view.MdStyleProgress;
import connect.view.TopToolBar;
import connect.view.payment.PaymentPwd;
import connect.view.roundedimageview.RoundedImageView;
import connect.utils.transfer.TransferEditView;
import protos.Connect;

/**
 * send lucky packet
 * Created by gtq on 2016/12/28.
 */
public class RedPacketActivity extends BaseActivity {

    @Bind(R.id.toolbar)
    TopToolBar toolbar;
    @Bind(R.id.roundimg)
    RoundedImageView roundimg;
    @Bind(R.id.txt1)
    TextView txt1;
    @Bind(R.id.layout_first)
    RelativeLayout layoutFirst;
    @Bind(R.id.edit)
    EditText edit;
    @Bind(R.id.layout_second)
    RelativeLayout layoutSecond;
    @Bind(R.id.transfer_edit_view)
    TransferEditView transferEditView;
    @Bind(R.id.btn)
    Button btn;

    private RedPacketActivity activity;
    private static String RED_TYPE = "RED_TYPE";
    private static String RED_KEY = "RED_KEY";
    /** packet type 1:private 2:group */
    private int redType;
    /** packet address */
    private String redKey;
    private TransferUtil transaUtil;
    private ContactEntity friendEntity;
    private GroupEntity groupEntity;
    private Connect.PendingRedPackage pendingRedPackage = null;
    private PaymentPwd paymentPwd = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_redpacket);
        ButterKnife.bind(this);
        initView();
    }

    public static void startActivity(Activity activity, int type, String roomkey) {
        Bundle bundle = new Bundle();
        bundle.putInt(RED_TYPE, type);
        bundle.putString(RED_KEY, roomkey);
        ActivityUtil.next(activity, RedPacketActivity.class, bundle);
    }

    @Override
    public void initView() {
        activity = this;
        toolbar.setRedStyle();
        toolbar.setLeftImg(R.mipmap.back_white);
        toolbar.setTitle(getResources().getString(R.string.Wallet_Packet));
        toolbar.setLeftListence(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityUtil.goBack(activity);
            }
        });
        toolbar.setRightText(getString(R.string.Chat_History));
        toolbar.setRightListence(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PacketHistoryActivity.startActivity(activity);
            }
        });

        transaUtil = new TransferUtil();

        redType = getIntent().getIntExtra(RED_TYPE, 0);
        redKey = getIntent().getStringExtra(RED_KEY);

        if (redType == 0) {
            layoutFirst.setVisibility(View.VISIBLE);
            layoutSecond.setVisibility(View.GONE);

            friendEntity = ContactHelper.getInstance().loadFriendEntity(redKey);
            if (friendEntity == null) {
                if (MemoryDataManager.getInstance().getPubKey().equals(redKey)) {
                    friendEntity = new ContactEntity();
                    friendEntity.setAvatar(MemoryDataManager.getInstance().getAvatar());
                    friendEntity.setUsername(MemoryDataManager.getInstance().getName());
                    friendEntity.setAddress(MemoryDataManager.getInstance().getAddress());
                } else {
                    ActivityUtil.goBack(activity);
                    return;
                }
            }

            GlideUtil.loadAvater(roundimg, friendEntity.getAvatar());
            String nameTxt = TextUtils.isEmpty(friendEntity.getRemark()) ? friendEntity.getUsername() : friendEntity.getRemark();
            txt1.setText(getString(R.string.Wallet_Send_Lucky_Packet_to, nameTxt));
        } else if (redType == 1) {
            layoutFirst.setVisibility(View.GONE);
            layoutSecond.setVisibility(View.VISIBLE);

            groupEntity = ContactHelper.getInstance().loadGroupEntity(redKey);
            if (groupEntity == null) {
                ActivityUtil.goBack(activity);
                return;
            }
            int countMem = ContactHelper.getInstance().loadGroupMemEntity(redKey).size();
            edit.setText(String.valueOf(countMem));
        }

        getPaddingInfo();
    }

    @Override
    protected void onStart() {
        super.onStart();
        transferEditView.initView();
        transferEditView.setNote(getString(R.string.Wallet_Best_wishes));
        transferEditView.setEditListener(new TransferEditView.OnEditListener() {
            @Override
            public void onEdit(String value) {
                if (TextUtils.isEmpty(value) || Double.valueOf(transferEditView.getCurrentBtc()) < 0.0001) {
                    btn.setEnabled(false);
                } else {
                    btn.setEnabled(true);
                }
            }

            @Override
            public void setFee() {
                PayFeeActivity.startActivity(activity);
            }
        });
    }

    private void getPaddingInfo() {
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.WALLET_PACKAGE_PENDING, ByteString.copyFrom(new byte[]{}),
                new ResultCall<Connect.HttpResponse>() {
                    @Override
                    public void onResponse(Connect.HttpResponse response) {
                        try {
                            Connect.IMResponse imResponse = Connect.IMResponse.parseFrom(response.getBody().toByteArray());
                            Connect.StructData structData = DecryptionUtil.decodeAESGCMStructData(imResponse.getCipherData());
                            Connect.PendingRedPackage pending = Connect.PendingRedPackage.parseFrom(structData.getPlainData());
                            if(ProtoBufUtil.getInstance().checkProtoBuf(pending)){
                                pendingRedPackage = pending;
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

    private void checkWalletMoney() {
        final long amount = RateFormatUtil.stringToLongBtc(transferEditView.getCurrentBtc());
        new TransferUtil().getOutputTran(activity, MemoryDataManager.getInstance().getAddress(), true,
                pendingRedPackage.getAddress(),transferEditView.getAvaAmount(), amount,
                new TransferUtil.OnResultCall() {
            @Override
            public void result(String inputString, String outputString) {
                checkPayPassword(amount, inputString, outputString);
            }
        });
    }

    private void checkPayPassword(final long amount, final String inputString, final String outputString) {
        if (!TextUtils.isEmpty(outputString)) {
            paymentPwd = new PaymentPwd();
            paymentPwd.showPaymentPwd(activity, new PaymentPwd.OnTrueListener() {
                @Override
                public void onTrue() {
                    String samValue = transaUtil.getSignRawTrans(MemoryDataManager.getInstance().getPriKey(), inputString, outputString);
                    sendPacket(amount, samValue);
                }
            });
        }
    }

    private void sendPacket(final long amount, String siginRaw) {
        Connect.OrdinaryRedPackage.Builder builder = Connect.OrdinaryRedPackage.newBuilder();
        builder.setHashId(pendingRedPackage.getHashId());
        builder.setMoney(amount);
        if (!TextUtils.isEmpty(transferEditView.getNote())) {
            builder.setTips(transferEditView.getNote());
        }
        if (redType == 0) {
            builder.setSize(1);
            ContactEntity entity = ContactHelper.getInstance().loadFriendEntity(redKey);
            builder.setReciverIdentifier(entity.getAddress());
        } else {
            builder.setReciverIdentifier(redKey);
            builder.setSize(Integer.valueOf(edit.getText().toString()));
        }

        builder.setRawTx(siginRaw);
        builder.setCategory(redType);
        builder.setType(0);//inner packet 0 ,outer packet 1
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.WALLET_PACKAGE_SEND,builder.build(),new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(final Connect.HttpResponse response) {
                paymentPwd.closeStatusDialog(MdStyleProgress.Status.LoadSuccess, new PaymentPwd.OnAnimationListener() {
                    @Override
                    public void onComplete() {
                        try {
                            Connect.IMResponse imResponse = Connect.IMResponse.parseFrom(response.getBody().toByteArray());
                            Connect.StructData structData = DecryptionUtil.decodeAESGCMStructData(imResponse.getCipherData());
                            Connect.RedPackage redPackage = Connect.RedPackage.parseFrom(structData.getPlainData());
                            if(ProtoBufUtil.getInstance().checkProtoBuf(redPackage)){
                                MsgSend.sendOuterMsg(MsgType.Lucky_Packet, redPackage.getHashId(), transferEditView.getNote());
                            }
                            if (redType == 0) {
                                ParamManager.getInstance().putLatelyTransfer(new TransferBean(5,friendEntity.getAvatar(),
                                        friendEntity.getUsername(),friendEntity.getAvatar()));
                            } else {

                            }
                            ToastEUtil.makeText(activity,R.string.Link_Send_successful).show();
                            ActivityUtil.goBack(activity);
                        } catch (InvalidProtocolBufferException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }

            @Override
            public void onError(Connect.HttpResponse response) {
                if (response.getCode() == 2421) {
                    ToastEUtil.makeText(activity, activity.getString(R.string.Wallet_error_lucky_packet_amount_too_small, 0.00005), 2).show();
                }else {
                    paymentPwd.closeStatusDialog(MdStyleProgress.Status.LoadFail);
                    TransferError.getInstance().showError(response.getCode(),response.getMessage());
                }
            }
        });
    }

    @OnClick({R.id.btn})
    public void OnClickListener(View view) {
        switch (view.getId()) {
            case R.id.btn:
                if (redType != 0) {//minimum amount check
                    long amount = RateFormatUtil.doubleToLongBtc(Double.valueOf(transferEditView.getCurrentBtc()));
                    int size = Integer.valueOf(edit.getText().toString());
                    if (amount / size < 5000) {
                        ToastEUtil.makeText(activity, activity.getString(R.string.Wallet_error_lucky_packet_amount_too_small, 0.00005), 2).show();
                        return;
                    }
                }

                checkWalletMoney();
                break;
        }
    }
}
