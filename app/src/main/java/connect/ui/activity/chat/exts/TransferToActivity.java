package connect.ui.activity.chat.exts;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.protobuf.InvalidProtocolBufferException;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.db.SharedPreferenceUtil;
import connect.db.green.DaoHelper.ContactHelper;
import connect.db.green.DaoHelper.MessageHelper;
import connect.db.green.DaoHelper.ParamManager;
import connect.db.green.DaoHelper.TransactionHelper;
import connect.db.green.bean.ContactEntity;
import connect.im.bean.MsgType;
import connect.ui.activity.R;
import connect.ui.activity.chat.bean.MsgEntity;
import connect.ui.activity.chat.bean.MsgSend;
import connect.ui.activity.chat.model.ChatMsgUtil;
import connect.ui.activity.chat.model.content.FriendChat;
import connect.ui.activity.chat.model.content.NormalChat;
import connect.ui.activity.login.bean.UserBean;
import connect.ui.activity.set.PayFeeActivity;
import connect.ui.activity.wallet.bean.TransferBean;
import connect.ui.activity.wallet.support.TransaUtil;
import connect.ui.activity.wallet.support.TransferError;
import connect.ui.base.BaseActivity;
import connect.utils.ActivityUtil;
import connect.utils.data.RateFormatUtil;
import connect.utils.TimeUtil;
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
import connect.view.transferEdit.TransferEditView;
import protos.Connect;

/**
 * transaction
 * Created by gtq on 2016/12/23.
 */
public class TransferToActivity extends BaseActivity {

    @Bind(R.id.toolbar)
    TopToolBar toolbar;
    @Bind(R.id.roundimg)
    RoundedImageView roundimg;
    @Bind(R.id.txt1)
    TextView txt1;
    @Bind(R.id.layout_first)
    RelativeLayout layoutFirst;
    @Bind(R.id.layout_second)
    LinearLayout layoutSecond;
    @Bind(R.id.transfer_edit_view)
    TransferEditView transferEditView;
    @Bind(R.id.btn)
    Button btn;

    private static String TRANSFER_TYPE = "TRANSFER_TYPE";
    private static String TRANSFER_PUBKEY = "TRANSFER_PUBKEY";
    private static String TRANSFER_AMOUNT = "TRANSFER_AMOUNT";

    private TransferToActivity activity;
    private boolean isStranger = false;

    private UserBean userBean;
    private ContactEntity friendEntity;
    private PaymentPwd paymentPwd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transferto);
        ButterKnife.bind(this);
        initView();
    }

    private TransferType transferType;
    public enum TransferType {
        CHAT,
        ADDRESS,
    }

    public static void startActivity(Activity activity, String address) {
        startActivity(activity, TransferType.CHAT, address, null);
    }

    public static void startActivity(Activity activity, String address, Double amount) {
        startActivity(activity, TransferType.ADDRESS, address, amount);
    }

    public static void startActivity(Activity activity, TransferType type, String address, Double amount) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(TRANSFER_TYPE, type);
        bundle.putString(TRANSFER_PUBKEY, address);
        if (amount != null)
            bundle.putDouble(TRANSFER_AMOUNT, amount);
        ActivityUtil.next(activity, TransferToActivity.class, bundle);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(getIntent().getExtras().containsKey(TRANSFER_AMOUNT)){
            transferEditView.initView(getIntent().getExtras().getDouble(TRANSFER_AMOUNT));
        }else{
            transferEditView.initView();
        }
    }

    @Override
    public void initView() {
        activity = this;
        toolbar.setBlackStyle();

        toolbar.setTitle(getString(R.string.Wallet_Transfer));
        toolbar.setLeftImg(R.mipmap.back_white);

        toolbar.setLeftListence(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityUtil.goBack(activity);
            }
        });
        userBean = SharedPreferenceUtil.getInstance().getUser();

        transferType = (TransferType) getIntent().getSerializableExtra(TRANSFER_TYPE);
        String transAddress = getIntent().getStringExtra(TRANSFER_PUBKEY);
        friendEntity = ContactHelper.getInstance().loadFriendEntity(transAddress);
        if (friendEntity == null) {
            isStranger = true;
            requestUserInfo(transAddress);
        } else {
            isStranger = false;
            GlideUtil.loadAvater(roundimg, friendEntity.getAvatar());
            String username = TextUtils.isEmpty(friendEntity.getRemark()) ? friendEntity.getUsername() : friendEntity.getRemark();
            txt1.setText(getString(R.string.Wallet_Transfer_To_User, username));
        }

        layoutFirst.setVisibility(View.VISIBLE);
        layoutSecond.setVisibility(View.GONE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (getIntent().getExtras().containsKey(TRANSFER_AMOUNT)) {
            transferEditView.initView(getIntent().getExtras().getDouble(TRANSFER_AMOUNT));
        } else {
            transferEditView.initView();
        }

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

    @OnClick({R.id.btn})
    public void OnClickListener(View view) {
        switch (view.getId()) {
            case R.id.btn:
                checkWalletMoney();
                break;
        }
    }

    private void checkWalletMoney() {
        final long amount = RateFormatUtil.stringToLongBtc(transferEditView.getCurrentBtc());
        paymentPwd = new PaymentPwd();
        new TransaUtil().getOutputTran(activity, userBean.getAddress(), false, friendEntity.getAddress(),
                transferEditView.getAvaAmount(), amount, new TransaUtil.OnResultCall() {
            @Override
            public void result(String inputString, String outputString) {
                checkPayPassword(amount, inputString, outputString);
            }
        });
    }

    private void checkPayPassword(final long amount, final String inputString, final String outputString) {
        if (!TextUtils.isEmpty(outputString)) {
            paymentPwd.showPaymentPwd(activity, new PaymentPwd.OnTrueListener() {
                @Override
                public void onTrue() {
                    String samValue = new TransaUtil().getSignRawTrans(userBean.getPriKey(), inputString, outputString);
                    requestSingleSend(amount, samValue);
                }

                @Override
                public void onFalse() {

                }
            });
        }
    }

    private void requestSingleSend(long amount, final String samValue) {
        Connect.SendBill sendBill = Connect.SendBill.newBuilder()
                .setAmount(amount)
                .setReceiver(friendEntity.getAddress())
                .setTips(transferEditView.getNote()).build();
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.WALLET_BILLING_SEND, sendBill,
                new ResultCall<Connect.HttpResponse>() {
                    @Override
                    public void onResponse(Connect.HttpResponse response) {
                        try {
                            Connect.IMResponse imResponse = Connect.IMResponse.parseFrom(response.getBody().toByteArray());
                            final Connect.StructData structData = DecryptionUtil.decodeAESGCMStructData(userBean.getPriKey(), imResponse.getCipherData());
                            paymentPwd.closeStatusDialog(MdStyleProgress.Status.LoadSuccess,new PaymentPwd.OnAnimationListener(){
                                @Override
                                public void onComplete() {
                                    try {
                                        Connect.BillHashId billHashId = Connect.BillHashId.parseFrom(structData.getPlainData());
                                        requestPublicTx(billHashId.getHash(), samValue);
                                    } catch (InvalidProtocolBufferException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });

                        } catch (InvalidProtocolBufferException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Connect.HttpResponse response) {
                        paymentPwd.closeStatusDialog(MdStyleProgress.Status.LoadFail);
                        TransferError.getInstance().showError(response.getCode(),response.getMessage());
                    }
                });
    }

    private void requestPublicTx(final String hashId, String rawTx) {
        Connect.PublishTx publishTx = Connect.PublishTx.newBuilder()
                .setHash(hashId).setRawTx(rawTx).build();
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.WALLET_BILLING_PUBLISH_TX, publishTx, new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                sendTransferMsg(hashId);
                ToastEUtil.makeText(activity, activity.getString(R.string.Wallet_Payment_Successful), 1, new ToastEUtil.OnToastListener() {
                    @Override
                    public void animFinish() {
                        ActivityUtil.goBack(activity);
                    }
                }).show();
            }

            @Override
            public void onError(Connect.HttpResponse response) {
                paymentPwd.closeStatusDialog(MdStyleProgress.Status.LoadFail);
            }
        });
    }

    /**
     * send transfer message
     *
     * @param hashid
     */
    private void sendTransferMsg(String hashid) {
        long amount = RateFormatUtil.doubleToLongBtc(Double.valueOf(transferEditView.getCurrentBtc()));
        ParamManager.getInstance().putLatelyTransfer(new TransferBean(4,friendEntity.getAvatar(),
                friendEntity.getUsername(),friendEntity.getAddress()));
        if (transferType == TransferType.CHAT) {
            MsgSend.sendOuterMsg(MsgType.Transfer, hashid, amount, transferEditView.getNote());
        } else if (transferType == TransferType.ADDRESS) {
            NormalChat friendChat = new FriendChat(friendEntity);
            MsgEntity msgEntity = friendChat.transferMsg(hashid, amount, transferEditView.getNote(), 0);
            MessageHelper.getInstance().insertToMsg(msgEntity.getMsgDefinBean());
            String showTxt = ChatMsgUtil.showContentTxt(0, msgEntity.getMsgDefinBean());
            friendChat.updateRoomMsg("", showTxt, TimeUtil.getCurrentTimeInLong());

            TransactionHelper.getInstance().updateTransEntity(hashid, msgEntity.getMsgid(), 1);
        }
    }

    private void requestUserInfo(String address) {
        Connect.SearchUser searchUser = Connect.SearchUser.newBuilder()
                .setCriteria(address)
                .build();
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.CONNECT_V1_USER_SEARCH, searchUser, new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                try {
                    Connect.IMResponse imResponse = Connect.IMResponse.parseFrom(response.getBody().toByteArray());
                    Connect.StructData structData = DecryptionUtil.decodeAESGCMStructData(userBean.getPriKey(), imResponse.getCipherData());
                    Connect.UserInfo sendUserInfo = Connect.UserInfo.parseFrom(structData.getPlainData());

                    friendEntity = new ContactEntity();
                    friendEntity.setPub_key(sendUserInfo.getPubKey());
                    friendEntity.setUsername(sendUserInfo.getUsername());
                    friendEntity.setAddress(sendUserInfo.getAddress());
                    friendEntity.setAvatar(sendUserInfo.getAvatar());
                    friendEntity.setAddress(sendUserInfo.getAddress());

                    GlideUtil.loadAvater(roundimg, friendEntity.getAvatar());
                    String username = TextUtils.isEmpty(friendEntity.getRemark()) ? friendEntity.getUsername() : friendEntity.getRemark();
                    txt1.setText(getString(R.string.Wallet_Transfer_To_User, username));
                } catch (InvalidProtocolBufferException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Connect.HttpResponse response) {

            }
        });
    }
}
