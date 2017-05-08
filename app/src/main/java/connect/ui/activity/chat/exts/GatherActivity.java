package connect.ui.activity.chat.exts;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import connect.db.SharedPreferenceUtil;
import connect.db.green.DaoHelper.ContactHelper;
import connect.db.green.bean.ContactEntity;
import connect.im.bean.MsgType;
import connect.ui.activity.R;
import connect.ui.activity.chat.bean.GatherBean;
import connect.ui.activity.chat.bean.MsgSend;
import connect.ui.activity.login.bean.UserBean;
import connect.ui.base.BaseActivity;
import connect.utils.ActivityUtil;
import connect.utils.data.RateFormatUtil;
import connect.utils.ToastEUtil;
import connect.utils.ToastUtil;
import connect.utils.UriUtil;
import connect.utils.cryption.DecryptionUtil;
import connect.utils.cryption.SupportKeyUril;
import connect.utils.glide.GlideUtil;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import connect.view.TopToolBar;
import connect.view.roundedimageview.RoundedImageView;
import connect.view.transferEdit.TransferEditView;
import protos.Connect;

/**
 * gather
 * Created by gtq on 2016/12/21.
 */
public class GatherActivity extends BaseActivity {

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
    @Bind(R.id.txt2)
    TextView txt2;
    @Bind(R.id.btn)
    Button btn;

    private Activity activity;
    /** tag */
    private String Tag = "GatherActivity";
    /** gather type */
    private static String TYPE_GATHER = "TYPE_GATHER";
    /** gather  key */
    private static String KEY_GATHER = "KEY_GATHER";
    private int gatherType;
    private String gatherKey;
    private ContactEntity friendEntity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gather);
        ButterKnife.bind(this);
        initView();
    }

    public static void startActivity(Activity activity, int type, String roomkey) {
        Bundle bundle = new Bundle();
        bundle.putInt(TYPE_GATHER, type);
        bundle.putString(KEY_GATHER, roomkey);
        ActivityUtil.next(activity, GatherActivity.class, bundle);
    }

    @Override
    public void initView() {
        activity = this;
        toolbar.setBlackStyle();
        toolbar.setLeftImg(R.mipmap.back_white);
        toolbar.setLeftListence(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityUtil.goBack(activity);
            }
        });

        gatherType = getIntent().getIntExtra(TYPE_GATHER, 0);
        gatherKey = getIntent().getStringExtra(KEY_GATHER);

        if (gatherType == 0) {
            toolbar.setTitle(getResources().getString(R.string.Wallet_Receipt));
        } else {
            toolbar.setTitle(getResources().getString(R.string.Chat_Crowd_funding));
            toolbar.setRightTextColor(R.color.color_green);
            toolbar.setRightText(getString(R.string.Chat_Crowdfoundind_History));
            toolbar.setRightListence(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    GroupGatherRecordsActivity.startActivity(activity);
                }
            });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        transferEditView.initView();
        transferEditView.setVisibilityAmount(View.GONE);
        transferEditView.setFeeVisibility(View.GONE);
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

            }
        });

        if (gatherType == 0) {
            layoutFirst.setVisibility(View.VISIBLE);
            layoutSecond.setVisibility(View.GONE);
            txt2.setVisibility(View.GONE);

            friendEntity = ContactHelper.getInstance().loadFriendEntity(gatherKey);
            if (friendEntity == null) {
                if (SharedPreferenceUtil.getInstance().getPubKey().equals(gatherKey)) {
                    UserBean userBean = SharedPreferenceUtil.getInstance().getUser();
                    friendEntity = new ContactEntity();
                    friendEntity.setAvatar(userBean.getAvatar());
                    friendEntity.setUsername(userBean.getName());
                    friendEntity.setAddress(userBean.getAddress());
                } else {
                    ActivityUtil.goBack(activity);
                    return;
                }
            }
            GlideUtil.loadAvater(roundimg, friendEntity.getAvatar());
            String nameTxt = TextUtils.isEmpty(friendEntity.getRemark()) ? friendEntity.getUsername() : friendEntity.getRemark();
            txt1.setText(nameTxt);
            btn.setText(getString(R.string.Wallet_Receipt));
        } else if (gatherType == 1) {
            layoutFirst.setVisibility(View.GONE);
            layoutSecond.setVisibility(View.VISIBLE);
            transferEditView.setAmountTvGone();

            int countMem = ContactHelper.getInstance().loadGroupMemEntity(gatherKey).size();
            edit.setText(String.valueOf(countMem));

            txt2.setVisibility(View.VISIBLE);
            String amoutTxt = transferEditView.getCurrentBtc();
            if (TextUtils.isEmpty(amoutTxt)) amoutTxt = String.valueOf(0D);
            long amout = RateFormatUtil.doubleToLongBtc(Double.valueOf(amoutTxt));
            txt2.setText(getString(R.string.Wallet_BTC_Total, RateFormatUtil.longToDoubleBtc(amout * countMem)));
            btn.setText(getString(R.string.Chat_Crowfunding));
        }
    }

    @OnClick({R.id.btn})
    public void OnClickListener(View view) {
        switch (view.getId()) {
            case R.id.btn:
                long amout = RateFormatUtil.doubleToLongBtc(Double.valueOf(transferEditView.getCurrentBtc()));
                if (gatherType == 0) {
                    requestSingleGather(amout);
                } else if (gatherType == 1 && !TextUtils.isEmpty(edit.getText())) {
                    int member = Integer.parseInt(edit.getText().toString());
                    requestGroupGather(amout * member);
                }
                break;
        }
    }

    @OnTextChanged({R.id.edit, R.id.amoutinput_et})
    public void OnEditTextChange() {
        String amoutTxt = transferEditView.getCurrentBtc();
        if (TextUtils.isEmpty(amoutTxt)) amoutTxt = String.valueOf(0D);
        if (!TextUtils.isEmpty(edit.getText())) {
            int memCount = Integer.parseInt(edit.getText().toString());
            long amout = RateFormatUtil.doubleToLongBtc(Double.valueOf(amoutTxt));
            txt2.setText(getString(R.string.Wallet_BTC_Total, RateFormatUtil.longToDoubleBtc(amout * memCount)));
        }
    }

    /**
     * A single payment
     */
    protected void requestSingleGather(final long amout) {
        Connect.ReceiveBill receiveBill = Connect.ReceiveBill.newBuilder().setSender(friendEntity.getAddress())
                .setAmount(amout).setTips(transferEditView.getNote()).build();
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.BILLING_RECIVE, receiveBill, new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                String prikey = SharedPreferenceUtil.getInstance().getPriKey();
                try {
                    Connect.IMResponse imResponse = Connect.IMResponse.parseFrom(response.getBody().toByteArray());
                    if (!SupportKeyUril.verifySign(imResponse.getSign(), imResponse.getCipherData().toByteArray())) {
                        throw new Exception("Validation fails");
                    }

                    Connect.StructData structData = DecryptionUtil.decodeAESGCMStructData(prikey, imResponse.getCipherData());
                    Connect.BillHashId hashId = Connect.BillHashId.parseFrom(structData.getPlainData());

                    ToastEUtil.makeText(activity, R.string.Wallet_Sent).show();

                    GatherBean gatherBean = new GatherBean(hashId.getHash(), amout, 1, false, transferEditView.getNote());
                    MsgSend.sendOuterMsg(MsgType.Request_Payment, gatherBean);

                    ActivityUtil.goBack(activity);
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
     * Group gathering
     */
    protected void requestGroupGather(final long amout) {
        Connect.LaunchCrowdfunding crowdfunding = Connect.LaunchCrowdfunding.newBuilder()
                .setGroupHash(gatherKey)
                .setTotal(amout)
                .setTips(transferEditView.getNote())
                .setSize(Integer.parseInt(edit.getText().toString())).build();
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.CROWDFUN_LAUNCH, crowdfunding, new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                String prikey = SharedPreferenceUtil.getInstance().getPriKey();
                try {
                    Connect.IMResponse imResponse = Connect.IMResponse.parseFrom(response.getBody().toByteArray());
                    //check sign
                    if (!SupportKeyUril.verifySign(imResponse.getSign(), imResponse.getCipherData().toByteArray())) {
                        throw new Exception("Validation fails");
                    }

                    Connect.StructData structData = DecryptionUtil.decodeAESGCMStructData(prikey, imResponse.getCipherData());
                    Connect.Crowdfunding funding = Connect.Crowdfunding.parseFrom(structData.getPlainData());

                    int size = Integer.parseInt(edit.getText().toString());
                    GatherBean gatherBean = new GatherBean(funding.getHashId(), amout / size, size, true, transferEditView.getNote());
                    MsgSend.sendOuterMsg(MsgType.Request_Payment, gatherBean);

                    ActivityUtil.goBack(activity);
                } catch (Exception e) {
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
