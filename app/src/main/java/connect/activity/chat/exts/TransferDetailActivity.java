package connect.activity.chat.exts;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.protobuf.InvalidProtocolBufferException;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.activity.base.BaseActivity;
import connect.activity.chat.bean.ContainerBean;
import connect.activity.chat.bean.MsgDefinBean;
import connect.activity.chat.bean.MsgDirect;
import connect.activity.wallet.BlockchainActivity;
import connect.database.MemoryDataManager;
import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.DaoHelper.CurrencyHelper;
import connect.database.green.DaoHelper.TransactionHelper;
import connect.database.green.bean.ContactEntity;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.ProtoBufUtil;
import connect.utils.TimeUtil;
import connect.utils.ToastUtil;
import connect.utils.UriUtil;
import connect.utils.cryption.DecryptionUtil;
import connect.utils.cryption.SupportKeyUril;
import connect.utils.data.RateFormatUtil;
import connect.utils.glide.GlideUtil;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import connect.wallet.cwallet.bean.CurrencyEnum;
import connect.widget.TopToolBar;
import connect.widget.roundedimageview.RoundedImageView;
import protos.Connect;

/**
 * Transfer details
 */
public class TransferDetailActivity extends BaseActivity {

    @Bind(R.id.toolbar)
    TopToolBar toolbar;
    @Bind(R.id.roundimg1)
    RoundedImageView roundimg1;
    @Bind(R.id.roundimg2)
    RoundedImageView roundimg2;
    @Bind(R.id.txt1)
    TextView txt1;
    @Bind(R.id.txt2)
    TextView txt2;
    @Bind(R.id.txt3)
    TextView txt3;
    @Bind(R.id.txt4)
    TextView txt4;
    @Bind(R.id.txt5)
    TextView txt5;
    @Bind(R.id.txt6)
    TextView txt6;
    @Bind(R.id.txt7)
    TextView txt7;
    @Bind(R.id.linearlayout)
    LinearLayout linearlayout;

    private TransferDetailActivity activity;
    private int transferType;// 0:outer 1:inner
    private String senderKey;
    private String receiverKey;
    private String hashId;
    private String msgId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer_detail);
        ButterKnife.bind(this);
        initView();
    }

    public static void startActivity(Activity activity, int transferType, String sender, String receiver, String hashid, String msgid) {
        Bundle bundle = new Bundle();
        bundle.putInt("TransferType", transferType);
        bundle.putString("Sender", sender);
        bundle.putString("Receiver", receiver);
        bundle.putString("Hash", hashid);
        bundle.putString("Msgid", msgid);
        ActivityUtil.next(activity, TransferDetailActivity.class, bundle);
    }

    @Override
    public void initView() {
        activity = this;
        toolbar.setBlackStyle();
        toolbar.setLeftImg(R.mipmap.back_white);
        toolbar.setTitle(getString(R.string.Chat_Transfer_Detail));
        toolbar.setLeftListence(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityUtil.goBack(activity);
            }
        });

        transferType = getIntent().getIntExtra("TransferType", 0);
        senderKey = getIntent().getStringExtra("Sender");
        receiverKey = getIntent().getStringExtra("Receiver");
        hashId = getIntent().getStringExtra("Hash");
        msgId = getIntent().getStringExtra("Msgid");

        if (transferType == 0) {
            requestInnerTransferDetail(hashId);
        } else if (transferType == 1) {
            requestOuterTransferDetail(hashId);
        }
        loadSenderInfo(senderKey);
        loadReceiverInfo(receiverKey);
    }

    @OnClick({R.id.linearlayout})
    public void OnClickListener(View view) {
        switch (view.getId()) {
            case R.id.linearlayout:
                BlockchainActivity.startActivity(activity, CurrencyEnum.BTC, hashId);
                break;
        }
    }

    /**
     * Request to transfer details
     *
     * @param hashid
     */
    public void requestInnerTransferDetail(final String hashid) {
        Connect.BillHashId hashId = Connect.BillHashId.newBuilder().setHash(hashid).build();
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.TRANSFER_INNER, hashId, new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                try {
                    Connect.IMResponse imResponse = Connect.IMResponse.parseFrom(response.getBody().toByteArray());
                    if (!SupportKeyUril.verifySign(imResponse.getSign(), imResponse.getCipherData().toByteArray())) {
                        throw new Exception("Validation fails");
                    }
                    Connect.StructData structData = DecryptionUtil.decodeAESGCMStructData(imResponse.getCipherData());
                    final Connect.Bill bill = Connect.Bill.parseFrom(structData.getPlainData().toByteArray());
                    if (ProtoBufUtil.getInstance().checkProtoBuf(bill)) {
                        if (TextUtils.isEmpty(bill.getTips())) {
                            txt7.setVisibility(View.GONE);
                        } else {
                            txt7.setText(bill.getTips());
                        }

                        txt1.setText(getString(R.string.Set_BTC_symbol) + "" + RateFormatUtil.longToDoubleBtc(bill.getAmount()));
                        txt2.setText(bill.getTxid());
                        linearlayout.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                BlockchainActivity.startActivity(activity, CurrencyEnum.BTC, bill.getTxid());
                            }
                        });
                        txt3.setText(TimeUtil.getTime(bill.getCreatedAt() * 1000, TimeUtil.DEFAULT_DATE_FORMAT));
                        String state = "";
                        switch (bill.getStatus()) {
                            case 0://do not pay
                                state = getString(R.string.Chat_Unpaid);
                                txt4.setBackgroundResource(R.drawable.shape_stroke_green);
                                break;
                            case 1://do not confirm
                                state = getString(R.string.Wallet_Unconfirmed);
                                txt4.setBackgroundResource(R.drawable.shape_stroke_red);
                                break;
                            case 2://confirm
                                state = getString(R.string.Wallet_Confirmed);
                                txt4.setBackgroundResource(R.drawable.shape_stroke_green);
                                break;
                        }
                        txt4.setText(state);
                        TransactionHelper.getInstance().updateTransEntity(hashid, msgId, bill.getStatus());
                        ContainerBean.sendRecExtMsg(ContainerBean.ContainerType.TRANSFER_STATE, msgId, hashid);
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


    /**
     * Request to transfer details
     *
     * @param hashid
     */
    public void requestOuterTransferDetail(final String hashid) {
        Connect.BillHashId hashId = Connect.BillHashId.newBuilder().setHash(hashid).build();
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.TRANSFER_OUTER, hashId,
                new ResultCall<Connect.HttpResponse>() {
                    @Override
                    public void onResponse(Connect.HttpResponse response) {
                        try {
                            Connect.IMResponse imResponse = Connect.IMResponse.parseFrom(response.getBody().toByteArray());
                            if (!SupportKeyUril.verifySign(imResponse.getSign(), imResponse.getCipherData().toByteArray())) {
                                throw new Exception("Validation fails");
                            }
                            Connect.StructData structData = DecryptionUtil.decodeAESGCMStructData(imResponse.getCipherData());
                            final Connect.ExternalBillingInfo billingInfo = Connect.ExternalBillingInfo.parseFrom(structData.getPlainData().toByteArray());
                            if (ProtoBufUtil.getInstance().checkProtoBuf(billingInfo)) {
                                if (TextUtils.isEmpty(billingInfo.getTips())) {
                                    txt7.setVisibility(View.GONE);
                                } else {
                                    txt7.setText(billingInfo.getTips());
                                }

                                txt1.setText(getString(R.string.Set_BTC_symbol) + "" + RateFormatUtil.longToDoubleBtc(billingInfo.getAmount()));
                                txt2.setText(billingInfo.getTxid());
                                linearlayout.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        BlockchainActivity.startActivity(activity, CurrencyEnum.BTC, billingInfo.getTxid());
                                    }
                                });
                                txt3.setText(TimeUtil.getTime(billingInfo.getCreatedAt() * 1000, TimeUtil.DEFAULT_DATE_FORMAT));
                                String state = "";
                                switch (billingInfo.getStatus()) {
                                    case 0://do not pay
                                        state = getString(R.string.Chat_Unpaid);
                                        txt4.setBackgroundResource(R.drawable.shape_stroke_green);
                                        break;
                                    case 1://do not confirm
                                        state = getString(R.string.Wallet_Unconfirmed);
                                        txt4.setBackgroundResource(R.drawable.shape_stroke_red);
                                        break;
                                    case 2://confirm
                                        state = getString(R.string.Wallet_Confirmed);
                                        txt4.setBackgroundResource(R.drawable.shape_stroke_green);
                                        break;
                                }
                                txt4.setText(state);
                                TransactionHelper.getInstance().updateTransEntity(hashid, msgId, billingInfo.getStatus());
                                ContainerBean.sendRecExtMsg(ContainerBean.ContainerType.TRANSFER_STATE, msgId, hashid);
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

    /**
     * send information
     * @param pubkey
     */
    protected void loadSenderInfo(String pubkey) {
        String avatar = "";
        String name = "";

        if (MemoryDataManager.getInstance().getPubKey().equals(pubkey)) {
            avatar = MemoryDataManager.getInstance().getAvatar();
            name = MemoryDataManager.getInstance().getName();
        } else {
            ContactEntity friendEntity = ContactHelper.getInstance().loadFriendEntity(pubkey);
            if (friendEntity != null) {
                avatar = friendEntity.getAvatar();
                name = friendEntity.getUsername();
            } else {
                String address = SupportKeyUril.getAddressFromPubkey(pubkey);
                requestUserInfo(0, address);
            }
        }

        if (!(TextUtils.isEmpty(avatar) || TextUtils.isEmpty(name))) {
            showSenderInfo(avatar, name);
        }
    }

    protected void showSenderInfo(String avatar,String name){
        GlideUtil.loadAvater(roundimg1, avatar);
        txt5.setText(name);
    }

    /**
     * receiver information
     * @param pubkey
     */
    protected void loadReceiverInfo(String pubkey) {
        String avatar = "";
        String name = "";

        if (MemoryDataManager.getInstance().getPubKey().equals(pubkey)) {
            avatar = MemoryDataManager.getInstance().getAvatar();
            name = MemoryDataManager.getInstance().getName();
        } else {
            ContactEntity friendEntity = ContactHelper.getInstance().loadFriendEntity(pubkey);
            if (friendEntity != null) {
                avatar = friendEntity.getAvatar();
                name = friendEntity.getUsername();
            } else {
                String address = SupportKeyUril.getAddressFromPubkey(pubkey);
                requestUserInfo(1, address);
            }
        }

        if (!(TextUtils.isEmpty(avatar) || TextUtils.isEmpty(name))) {
            showReceiverInfo(avatar, name);
        }
    }

    protected void showReceiverInfo(String avatar,String name){
        GlideUtil.loadAvater(roundimg2, avatar);
        txt6.setText(name);
    }

    /**
     * Query the user information
     * @param direct
     * @param address
     */
    private void requestUserInfo(final int direct, String address) {
        final Connect.SearchUser searchUser = Connect.SearchUser.newBuilder()
                .setCriteria(address)
                .build();
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.CONNECT_V1_USER_SEARCH, searchUser,
                new ResultCall<Connect.HttpResponse>() {
                    @Override
                    public void onResponse(Connect.HttpResponse response) {
                        try {
                            Connect.IMResponse imResponse = Connect.IMResponse.parseFrom(response.getBody().toByteArray());
                            Connect.StructData structData = DecryptionUtil.decodeAESGCMStructData(imResponse.getCipherData());
                            Connect.UserInfo userInfo = Connect.UserInfo.parseFrom(structData.getPlainData());
                            if(ProtoBufUtil.getInstance().checkProtoBuf(userInfo)){
                                String avatar=userInfo.getAvatar();
                                String name=userInfo.getUsername();

                                if (direct == 0) {
                                    showSenderInfo(avatar, name);
                                } else if (direct == 1) {
                                    showReceiverInfo(avatar, name);
                                }
                            }
                        } catch (InvalidProtocolBufferException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Connect.HttpResponse response) {
                        ToastUtil.getInstance().showToast(response.getCode() + response.getMessage());
                    }
                });
    }
}
