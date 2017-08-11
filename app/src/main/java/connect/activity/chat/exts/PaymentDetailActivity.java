package connect.activity.chat.exts;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.activity.base.BaseActivity;
import connect.activity.chat.bean.ContainerBean;
import connect.activity.chat.bean.MsgDefinBean;
import connect.activity.chat.bean.MsgDirect;
import connect.activity.chat.bean.MsgEntity;
import connect.activity.chat.bean.RecExtBean;
import connect.activity.chat.exts.contract.PaymentDetailContract;
import connect.activity.chat.exts.presenter.PaymentDetailPresenter;
import connect.activity.chat.model.content.FriendChat;
import connect.activity.chat.model.content.NormalChat;
import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.DaoHelper.MessageHelper;
import connect.database.green.DaoHelper.TransactionHelper;
import connect.database.green.bean.ContactEntity;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.ProtoBufUtil;
import connect.utils.ToastEUtil;
import connect.utils.UriUtil;
import connect.utils.cryption.DecryptionUtil;
import connect.utils.cryption.SupportKeyUril;
import connect.utils.data.RateFormatUtil;
import connect.utils.glide.GlideUtil;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import connect.wallet.cwallet.bean.CurrencyEnum;
import connect.wallet.cwallet.business.BaseBusiness;
import connect.wallet.cwallet.business.TransferType;
import connect.wallet.cwallet.inter.WalletListener;
import connect.widget.TopToolBar;
import connect.widget.roundedimageview.RoundedImageView;
import protos.Connect;

/**
 * private chat gather
 * Created by gtq on 2016/12/22.
 */
public class PaymentDetailActivity extends BaseActivity implements PaymentDetailContract.BView{

    @Bind(R.id.toolbar)
    TopToolBar toolbar;
    @Bind(R.id.roundimg)
    RoundedImageView roundimg;
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

    private int state;
    private Connect.Bill billDetail = null;

    private MsgDefinBean definBean;
    private String msgId;
    private PaymentDetailContract.Presenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gather_singledetail);
        ButterKnife.bind(this);
        initView();
    }

    public static void startActivity(Activity activity, MsgDefinBean definBean) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("MsgDefinBean",definBean);
        ActivityUtil.next(activity, PaymentDetailActivity.class, bundle);
    }

    @Override
    public void initView() {
        activity = this;
        toolbar.setBlackStyle();
        toolbar.setLeftImg(R.mipmap.back_white);
        toolbar.setTitle(getResources().getString(R.string.Wallet_Detail));
        toolbar.setLeftListence(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityUtil.goBack(activity);
            }
        });

        definBean = (MsgDefinBean) getIntent().getSerializableExtra("MsgDefinBean");
        msgId = definBean.getMessage_id();
        String hashid = definBean.getContent();
        requestGatherDetail(hashid);

        new PaymentDetailPresenter(this).start();
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
                        String hashid = definBean.getContent();
                        requestPayment(hashid);
                        break;
                    case 2:
                        ActivityUtil.goBack(activity);
                        break;
                }
                break;
        }
    }

    protected void requestGatherDetail(final String hashid) {
        final Connect.BillHashId hashId = Connect.BillHashId.newBuilder().setHash(hashid).build();
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.BILLING_INFO, hashId, new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                try {
                    Connect.IMResponse imResponse = Connect.IMResponse.parseFrom(response.getBody().toByteArray());
                    if (!SupportKeyUril.verifySign(imResponse.getSign(), imResponse.getCipherData().toByteArray())) {
                        throw new Exception("Validation fails");
                    }

                    Connect.StructData structData = DecryptionUtil.decodeAESGCMStructData(imResponse.getCipherData());
                    billDetail = Connect.Bill.parseFrom(structData.getPlainData());
                    if (ProtoBufUtil.getInstance().checkProtoBuf(billDetail)) {
                        String username = "";
                        ContactEntity entity = null;
                        if (definBean.msgDirect() == MsgDirect.To) {//I started gathering
                            entity = ContactHelper.getInstance().loadFriendEntity(definBean.getPublicKey());
                            username = TextUtils.isEmpty(entity.getUsername()) ? entity.getRemark() : entity.getUsername();
                            txt1.setText(String.format(getString(R.string.Wallet_has_requested_to_payment), username));
                            txt4.setVisibility(View.INVISIBLE);
                        } else {//I received the payment
                            entity = ContactHelper.getInstance().loadFriendEntity(definBean.getSenderInfoExt().getPublickey());
                            username = TextUtils.isEmpty(entity.getUsername()) ? entity.getRemark() : entity.getUsername();
                            txt1.setText(String.format(getString(R.string.Wallet_has_requested_for_payment), username));
                            txt4.setVisibility(View.VISIBLE);
                        }
                        if (entity != null) {
                            GlideUtil.loadAvater(roundimg, entity.getAvatar());
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
                            if (definBean.msgDirect() == MsgDirect.To) {
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

                        if (!TextUtils.isEmpty(msgId)) {
                            TransactionHelper.getInstance().updateTransEntity(hashid, msgId, state);
                        }
                        ContainerBean.sendRecExtMsg(ContainerBean.ContainerType.GATHER_DETAIL, msgId, 0, state);
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

    protected void requestPayment(String hashid) {
        BaseBusiness baseBusiness = new BaseBusiness(activity, CurrencyEnum.BTC);
        baseBusiness.typePayment(hashid, TransferType.TransactionTypePayCrowding.getType(), new WalletListener<String>() {
            @Override
            public void success(String hashId) {
                ContactEntity entity = ContactHelper.getInstance().loadFriendEntity(definBean.getSenderInfoExt().getPublickey());
                if (entity != null) {
                    String contactName = TextUtils.isEmpty(entity.getRemark()) ? entity.getUsername() : entity.getRemark();
                    String noticeContent = getString(R.string.Chat_paid_the_bill_to, activity.getString(R.string.Chat_You), contactName);
                    RecExtBean.getInstance().sendEvent(RecExtBean.ExtType.NOTICE, noticeContent);

                    NormalChat normalChat = new FriendChat(entity);
                    MsgEntity msgEntity = normalChat.noticeMsg(noticeContent);
                    MessageHelper.getInstance().insertToMsg(msgEntity.getMsgDefinBean());
                }

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