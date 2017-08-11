package connect.activity.chat.exts;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.protobuf.InvalidProtocolBufferException;

import java.util.HashMap;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.activity.base.BaseActivity;
import connect.activity.chat.bean.MsgEntity;
import connect.activity.chat.bean.MsgSend;
import connect.activity.chat.model.content.FriendChat;
import connect.activity.chat.model.content.NormalChat;
import connect.activity.set.PayFeeActivity;
import connect.activity.wallet.bean.TransferBean;
import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.DaoHelper.MessageHelper;
import connect.database.green.DaoHelper.ParamManager;
import connect.database.green.DaoHelper.TransactionHelper;
import connect.database.green.bean.ContactEntity;
import connect.im.bean.MsgType;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.ProtoBufUtil;
import connect.utils.TimeUtil;
import connect.utils.ToastEUtil;
import connect.utils.UriUtil;
import connect.utils.cryption.DecryptionUtil;
import connect.utils.data.RateFormatUtil;
import connect.utils.glide.GlideUtil;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import connect.wallet.cwallet.business.TransferEditView;
import connect.wallet.cwallet.bean.CurrencyEnum;
import connect.wallet.cwallet.business.BaseBusiness;
import connect.wallet.cwallet.inter.WalletListener;
import connect.widget.TopToolBar;
import connect.widget.random.RandomVoiceActivity;
import connect.widget.roundedimageview.RoundedImageView;
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
    private ContactEntity friendEntity;
    private BaseBusiness baseBusiness;

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
            transferEditView.initView(getIntent().getExtras().getDouble(TRANSFER_AMOUNT), activity);
        }else{
            transferEditView.initView(activity);
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

        baseBusiness = new BaseBusiness(activity, CurrencyEnum.BTC);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (getIntent().getExtras().containsKey(TRANSFER_AMOUNT)) {
            transferEditView.initView(getIntent().getExtras().getDouble(TRANSFER_AMOUNT), activity);
        } else {
            transferEditView.initView(activity);
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
                requestSingleSend();
                break;
        }
    }

    private void requestSingleSend() {
        HashMap<String,Long> outMap = new HashMap();
        outMap.put(friendEntity.getPub_key(),transferEditView.getCurrentBtcLong());
        baseBusiness.transferConnectUser(null, outMap, new WalletListener<String>() {
            @Override
            public void success(String value) {
                sendTransferMsg(value);
                ActivityUtil.goBack(activity);
            }

            @Override
            public void fail(WalletError error) {
                ToastEUtil.makeText(activity,R.string.Login_Send_failed).show();
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
        ParamManager.getInstance().putLatelyTransfer(new TransferBean(4, friendEntity.getAvatar(),
                friendEntity.getUsername(), friendEntity.getAddress()));
        if (transferType == TransferType.CHAT) {
            MsgSend.sendOuterMsg(MsgType.Transfer, hashid, amount, transferEditView.getNote());
        } else if (transferType == TransferType.ADDRESS) {
            NormalChat friendChat = new FriendChat(friendEntity);
            MsgEntity msgEntity = friendChat.transferMsg(hashid, amount, transferEditView.getNote(), 0);
            MessageHelper.getInstance().insertToMsg(msgEntity.getMsgDefinBean());
            String showTxt = msgEntity.getMsgDefinBean().showContentTxt(0);
            friendChat.updateRoomMsg(null, showTxt, TimeUtil.getCurrentTimeInLong());

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
                    Connect.StructData structData = DecryptionUtil.decodeAESGCMStructData(imResponse.getCipherData());
                    Connect.UserInfo sendUserInfo = Connect.UserInfo.parseFrom(structData.getPlainData());

                    if(ProtoBufUtil.getInstance().checkProtoBuf(sendUserInfo)){
                        friendEntity = new ContactEntity();
                        friendEntity.setPub_key(sendUserInfo.getPubKey());
                        friendEntity.setUsername(sendUserInfo.getUsername());
                        friendEntity.setAddress(sendUserInfo.getAddress());
                        friendEntity.setAvatar(sendUserInfo.getAvatar());

                        GlideUtil.loadAvater(roundimg, friendEntity.getAvatar());
                        String username = TextUtils.isEmpty(friendEntity.getRemark()) ? friendEntity.getUsername() : friendEntity.getRemark();
                        txt1.setText(getString(R.string.Wallet_Transfer_To_User, username));
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            switch (requestCode){
                case RandomVoiceActivity.REQUEST_CODE:
                    transferEditView.createWallet(data);
                    break;
                default:
                    break;
            }
        }
    }
}
