package connect.activity.chat.exts;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.wallet.bean.CurrencyEnum;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import connect.activity.base.BaseActivity;
import connect.activity.chat.bean.LinkMessageRow;
import connect.activity.chat.bean.MsgSend;
import connect.activity.wallet.manager.TransferManager;
import connect.database.MemoryDataManager;
import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.bean.ContactEntity;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.ToastEUtil;
import connect.utils.data.RateFormatUtil;
import connect.utils.glide.GlideUtil;
import connect.activity.wallet.manager.TransferEditView;
import com.wallet.inter.WalletListener;
import connect.widget.TopToolBar;
import protos.Connect;

/**
 * gather
 * Created by gtq on 2016/12/21.
 */
public class GatherActivity extends BaseActivity {

    @Bind(R.id.toolbar)
    TopToolBar toolbar;
    @Bind(R.id.roundimg)
    ImageView roundimg;
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
                    GroupCrowdingRecordsActivity.startActivity(activity);
                }
            });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        transferEditView.initView(activity);
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
                if (MemoryDataManager.getInstance().getPubKey().equals(gatherKey)) {
                    friendEntity = new ContactEntity();
                    friendEntity.setAvatar(MemoryDataManager.getInstance().getAvatar());
                    friendEntity.setUsername(MemoryDataManager.getInstance().getName());
                    friendEntity.setAddress(MemoryDataManager.getInstance().getAddress());
                } else {
                    ActivityUtil.goBack(activity);
                    return;
                }
            }
            GlideUtil.loadAvatarRound(roundimg, friendEntity.getAvatar());
            String nameTxt = TextUtils.isEmpty(friendEntity.getRemark()) ? friendEntity.getUsername() : friendEntity.getRemark();
            txt1.setText(nameTxt);
            btn.setText(getString(R.string.Wallet_Receipt));
        } else if (gatherType == 1) {
            layoutFirst.setVisibility(View.GONE);
            layoutSecond.setVisibility(View.VISIBLE);
            transferEditView.setAmountTvGone();

            int countMem = ContactHelper.getInstance().loadGroupMemEntities(gatherKey).size();
            edit.setText(String.valueOf(countMem-1));

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
                if (gatherType == 0) {
                    requestSingleGather();
                } else if (gatherType == 1 && !TextUtils.isEmpty(edit.getText())) {
                    requestGroupGather();
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
    protected void requestSingleGather() {
        CurrencyEnum currencyEnum = transferEditView.getCurrencyEnum();
        final long amount = RateFormatUtil.doubleToLongBtc(Double.valueOf(transferEditView.getCurrentBtc()));
        String senderAddress = friendEntity.getPub_key();
        String tips = transferEditView.getNote();
        TransferManager transferManager = new TransferManager(activity, currencyEnum);
        transferManager.friendReceiver(amount, senderAddress, tips, new WalletListener<Connect.Bill>() {

            @Override
            public void success(Connect.Bill bill) {
                ToastEUtil.makeText(activity, R.string.Wallet_Sent).show();

                MsgSend.sendOuterMsg(LinkMessageRow.Request_Payment, 0, bill.getHash(), amount, 1, bill.getTips());
                ActivityUtil.goBack(activity);
            }

            @Override
            public void fail(WalletError error) {

            }
        });
    }

    /**
     * Group gathering
     */
    protected void requestGroupGather() {
        long amount = RateFormatUtil.doubleToLongBtc(Double.valueOf(transferEditView.getCurrentBtc()));
        int member = Integer.parseInt(edit.getText().toString());
        long totalAmount = amount * member;
        String tips = transferEditView.getNote();

        TransferManager transferManager = new TransferManager(activity, transferEditView.getCurrencyEnum());
        transferManager.crowdFuning(gatherKey, totalAmount, member, tips, new WalletListener<Connect.Crowdfunding>() {

            @Override
            public void success(Connect.Crowdfunding crowdfunding) {
                int size = Integer.parseInt(edit.getText().toString());

                MsgSend.sendOuterMsg(LinkMessageRow.Request_Payment, 1, crowdfunding.getHashId(), crowdfunding.getTotal() / size, size, crowdfunding.getTips());
                ActivityUtil.goBack(activity);
            }

            @Override
            public void fail(WalletError error) {

            }
        });
    }
}
