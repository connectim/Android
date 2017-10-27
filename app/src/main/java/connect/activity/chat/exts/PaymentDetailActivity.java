package connect.activity.chat.exts;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.protobuf.InvalidProtocolBufferException;
import com.wallet.bean.CurrencyEnum;
import com.wallet.inter.WalletListener;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.activity.base.BaseActivity;
import connect.activity.chat.bean.ContainerBean;
import connect.activity.chat.bean.RecExtBean;
import connect.activity.chat.exts.contract.PaymentDetailContract;
import connect.activity.chat.exts.presenter.PaymentDetailPresenter;
import connect.activity.wallet.manager.TransferManager;
import connect.activity.wallet.manager.TransferType;
import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.DaoHelper.MessageHelper;
import connect.database.green.DaoHelper.TransactionHelper;
import connect.database.green.bean.ContactEntity;
import connect.instant.model.CFriendChat;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.ToastEUtil;
import connect.utils.data.RateFormatUtil;
import connect.utils.glide.GlideUtil;
import connect.widget.TopToolBar;
import instant.bean.ChatMsgEntity;
import instant.bean.MsgDirect;
import protos.Connect;

/**
 * private chat gather
 * Created by gtq on 2016/12/22.
 */
public class PaymentDetailActivity extends BaseActivity implements PaymentDetailContract.BView{

    @Bind(R.id.toolbar)
    TopToolBar toolbar;
    @Bind(R.id.roundimg)
    ImageView roundimg;
    @Bind(R.id.txt1)
    TextView txt1;
    @Bind(R.id.txt2)
    TextView txt2;
    @Bind(R.id.txt3)
    TextView txt3;
    @Bind(R.id.txt4)
    TextView txt4;
    @Bind(R.id.btn)
    Button btn;

    private PaymentDetailActivity activity;
    private static String TAG = "_PaymentDetailActivity";
    private static String MESSAGE_ENTITY = "MESSAGE_ENTITY";
    private int state;
    private Connect.Bill billDetail = null;

    private ChatMsgEntity msgExtEntity;
    private PaymentDetailContract.Presenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gather_singledetail);
        ButterKnife.bind(this);
        initView();
    }

    public static void startActivity(Activity activity, ChatMsgEntity msgExtEntity) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(MESSAGE_ENTITY,msgExtEntity);
        ActivityUtil.next(activity, PaymentDetailActivity.class, bundle);
    }

    @Override
    public void initView() {
        activity = this;
        toolbar.setBlackStyle();
        toolbar.setLeftImg(R.mipmap.back_white);
        toolbar.setTitle(getResources().getString(R.string.Wallet_Detail));
        toolbar.setLeftListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityUtil.goBack(activity);
            }
        });

        msgExtEntity = (ChatMsgEntity) getIntent().getSerializableExtra(MESSAGE_ENTITY);

        new PaymentDetailPresenter(this).start();
        try {
            Connect.PaymentMessage paymentMessage = Connect.PaymentMessage.parseFrom(msgExtEntity.getContents());
            presenter.requestPaymentDetail(paymentMessage.getHashId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @OnClick({R.id.btn})
    public void OnClickListener(View view) {
        switch (view.getId()) {
            case R.id.btn:
                int state = (int) view.getTag();
                switch (state) {
                    case 0://Did not pay ,wait for payment
                        ActivityUtil.goBack(activity);
                        break;
                    case 1://Did not pay ,to pay
                        try {
                            Connect.PaymentMessage paymentMessage = Connect.PaymentMessage.parseFrom(msgExtEntity.getContents());
                            requestPayment(paymentMessage.getHashId());
                        } catch (InvalidProtocolBufferException e) {
                            e.printStackTrace();
                        }
                        break;
                    case 2:
                        ActivityUtil.goBack(activity);
                        break;
                }
                break;
        }
    }

    @Override
    public void showPaymentDetail(Connect.Bill bill) {
        String username = "";
        ContactEntity entity = null;
        if (msgExtEntity.parseDirect() == MsgDirect.To) {//I started gathering
            entity = ContactHelper.getInstance().loadFriendEntity(msgExtEntity.getMessage_to());
            username = TextUtils.isEmpty(entity.getUsername()) ? entity.getRemark() : entity.getUsername();
            txt1.setText(String.format(getString(R.string.Wallet_has_requested_to_payment), username));
            txt4.setVisibility(View.INVISIBLE);
        } else {//I received the payment
            entity = ContactHelper.getInstance().loadFriendEntity(msgExtEntity.getMessage_from());
            username = TextUtils.isEmpty(entity.getUsername()) ? entity.getRemark() : entity.getUsername();
            txt1.setText(String.format(getString(R.string.Wallet_has_requested_for_payment), username));
            txt4.setVisibility(View.VISIBLE);
        }
        if (entity != null) {
            GlideUtil.loadAvatarRound(roundimg, entity.getAvatar());
        }

        if (TextUtils.isEmpty(billDetail.getTips())) {
            txt2.setVisibility(View.GONE);
        } else {
            txt2.setText(billDetail.getTips());
        }

        String amout = RateFormatUtil.longToDoubleBtc(billDetail.getAmount());
        txt3.setText(getResources().getString(R.string.Set_BTC_symbol) + amout);

        state = billDetail.getStatus();
        if (state == 0) {//Did not pay
            if (msgExtEntity.parseDirect() == MsgDirect.To) {
                btn.setText(getResources().getString(R.string.Wallet_Waitting_for_pay));
                btn.setBackgroundResource(R.drawable.shape_stroke_red);
                btn.setTag(0);
            } else {
                btn.setText(getResources().getString(R.string.Set_Payment));
                btn.setBackgroundResource(R.drawable.shape_stroke_green);
                btn.setTag(1);
            }
        } else if (state == 1) {
            btn.setText(getResources().getString(R.string.Common_Cancel));
            btn.setBackgroundResource(R.drawable.shape_stroke_red);
            btn.setTag(2);
        }

        String msgId = msgExtEntity.getMessage_id();
        if (!TextUtils.isEmpty(msgId)) {
            TransactionHelper.getInstance().updateTransEntity(bill.getHash(), msgId, state);
        }
        ContainerBean.sendRecExtMsg(ContainerBean.ContainerType.GATHER_DETAIL, msgId, 0, state);
    }

    protected void requestPayment(final String hashid) {
        TransferManager transferManager = new TransferManager(activity, CurrencyEnum.BTC);
        transferManager.typePayment(hashid, TransferType.TransactionTypePayCrowding.getType(), new WalletListener<String>() {
            @Override
            public void success(String hashId) {
                ContactEntity entity = ContactHelper.getInstance().loadFriendEntity(msgExtEntity.getMessage_ower());
                if (entity != null) {
                    String contactName = TextUtils.isEmpty(entity.getRemark()) ? entity.getUsername() : entity.getRemark();
                    String noticeContent = getString(R.string.Chat_paid_the_bill_to, activity.getString(R.string.Chat_You), contactName);
                    RecExtBean.getInstance().sendEvent(RecExtBean.ExtType.NOTICE, noticeContent);

                    CFriendChat normalChat = new CFriendChat(entity);
                    ChatMsgEntity msgExtEntity = normalChat.noticeMsg(1, noticeContent, hashId);
                    MessageHelper.getInstance().insertMsgExtEntity(msgExtEntity);
                }

                String msgId=msgExtEntity.getMessage_id();
                TransactionHelper.getInstance().updateTransEntity(billDetail.getHash(), msgId, 1);
                ToastEUtil.makeText(activity, R.string.Wallet_Payment_Successful).show();
                ActivityUtil.goBack(activity);
            }

            @Override
            public void fail(WalletError error) {

            }
        });
    }

    @Override
    public void setPresenter(PaymentDetailContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public Activity getActivity() {
        return activity;
    }
}