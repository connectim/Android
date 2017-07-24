package connect.activity.chat.exts;

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
import connect.database.MemoryDataManager;
import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.DaoHelper.ParamManager;
import connect.database.green.bean.ContactEntity;
import connect.database.green.bean.GroupEntity;
import connect.im.bean.MsgType;
import connect.ui.activity.R;
import connect.activity.chat.bean.MsgSend;
import connect.activity.wallet.PacketHistoryActivity;
import connect.activity.wallet.bean.TransferBean;
import connect.utils.ProtoBufUtil;
import connect.utils.transfer.TransferError;
import connect.utils.transfer.TransferUtil;
import connect.activity.set.PayFeeActivity;
import connect.activity.base.BaseActivity;
import connect.utils.ActivityUtil;
import connect.utils.data.RateFormatUtil;
import connect.utils.ToastEUtil;
import connect.utils.UriUtil;
import connect.utils.cryption.DecryptionUtil;
import connect.utils.glide.GlideUtil;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import connect.wallet.cwallet.bean.CurrencyEnum;
import connect.wallet.cwallet.business.BaseBusiness;
import connect.wallet.cwallet.inter.WalletListener;
import connect.widget.MdStyleProgress;
import connect.widget.TopToolBar;
import connect.widget.payment.PaymentPwd;
import connect.widget.roundedimageview.RoundedImageView;
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
    private BaseBusiness baseBusiness;

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
        baseBusiness = new BaseBusiness(activity, CurrencyEnum.BTC);
    }

    @Override
    protected void onStart() {
        super.onStart();
        transferEditView.initView(activity);
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

    private void sendRedPacket() {
        baseBusiness.luckyPacket(null, redKey, 0, redType, redType == 0 ? 1 : Integer.valueOf(edit.getText().toString()),
                transferEditView.getCurrentBtcLong(), transferEditView.getNote(), new WalletListener<String>() {
                    @Override
                    public void success(String hashId) {
                        if (redType == 0) {
                            ParamManager.getInstance().putLatelyTransfer(new TransferBean(5, friendEntity.getAvatar(),
                                    friendEntity.getUsername(), friendEntity.getAvatar()));
                        }

                        MsgSend.sendOuterMsg(MsgType.Lucky_Packet, hashId, transferEditView.getNote());
                        ToastEUtil.makeText(activity, R.string.Link_Send_successful).show();
                        ActivityUtil.goBack(activity);
                    }

                    @Override
                    public void fail(WalletError error) {
                        ToastEUtil.makeText(activity,R.string.Login_Send_failed).show();
                    }
                });
    }

    @OnClick({R.id.btn})
    public void OnClickListener(View view) {
        switch (view.getId()) {
            case R.id.btn:
                sendRedPacket();
                break;
        }
    }
}
