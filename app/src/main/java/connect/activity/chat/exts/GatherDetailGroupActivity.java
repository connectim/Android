package connect.activity.chat.exts;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.activity.base.BaseActivity;
import connect.activity.base.BaseApplication;
import connect.activity.chat.bean.ContainerBean;
import connect.activity.chat.bean.MsgEntity;
import connect.activity.chat.bean.RecExtBean;
import connect.activity.chat.model.content.FriendChat;
import connect.activity.chat.model.content.GroupChat;
import connect.activity.chat.model.content.NormalChat;
import connect.activity.wallet.BlockchainActivity;
import connect.database.MemoryDataManager;
import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.DaoHelper.CurrencyHelper;
import connect.database.green.DaoHelper.MessageHelper;
import connect.database.green.DaoHelper.TransactionHelper;
import connect.database.green.bean.CurrencyEntity;
import connect.database.green.bean.GroupEntity;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.ProtoBufUtil;
import connect.utils.TimeUtil;
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
 * Created by gtq on 2016/12/21.
 */
public class GatherDetailGroupActivity extends BaseActivity {

    @Bind(R.id.toolbar)
    TopToolBar toolbar;
    @Bind(R.id.roundimg)
    RoundedImageView roundimg;
    @Bind(R.id.txt1)
    TextView senderNameTxt;
    @Bind(R.id.txt2)
    TextView txt2;
    @Bind(R.id.txt3)
    TextView txt3;
    @Bind(R.id.txt4)
    TextView txt4;
    @Bind(R.id.txt5)
    TextView txt5;
    @Bind(R.id.btn)
    Button btn;
    @Bind(R.id.layout_first)
    LinearLayout layoutFirst;
    @Bind(R.id.txt6)
    TextView txt6;
    @Bind(R.id.recyclerview)
    RecyclerView recyclerview;

    private GatherDetailGroupActivity activity;
    private static String GATHER_HASHID = "GATHER_HASHID";
    private static String GATHER_MSGID = "GATHER_MSGID";

    private PaymentAdapter paymentAdapter;

    private String msgId;
    private String hashid;
    private Connect.Crowdfunding crowdfunding = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gather_groupdetail);
        ButterKnife.bind(this);
        initView();
    }

    public static void startActivity(Activity activity, String hashid) {
        startActivity(activity, hashid, null);
    }

    public static void startActivity(Activity activity, String hashid, String msgid) {
        Bundle bundle = new Bundle();
        bundle.putString(GATHER_HASHID, hashid);
        if (!TextUtils.isEmpty(msgid)) {
            bundle.putString(GATHER_MSGID, msgid);
        }
        ActivityUtil.next(activity, GatherDetailGroupActivity.class, bundle);
    }

    @Override
    public void initView() {
        activity = this;
        toolbar.setBlackStyle();
        toolbar.setLeftImg(R.mipmap.back_white);
        toolbar.setTitle(getResources().getString(R.string.Chat_Crowdfunding));
        toolbar.setLeftListence(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityUtil.goBack(activity);
            }
        });

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(activity);
        paymentAdapter = new PaymentAdapter();
        recyclerview.setLayoutManager(linearLayoutManager);
        recyclerview.setAdapter(paymentAdapter);
        paymentAdapter.setItemClickListener(new OnItemClickListener() {
            @Override
            public void itemClick(Connect.CrowdfundingRecord record) {
                if (record != null) {
                    BlockchainActivity.startActivity(activity, CurrencyEnum.BTC, record.getTxid());
                }
            }
        });

        hashid = getIntent().getStringExtra(GATHER_HASHID);
        msgId = getIntent().getStringExtra(GATHER_MSGID);
        requestGatherDetail(hashid);
    }

    /**
     * payment detail
     */
    protected void requestGatherDetail(final String hashid) {
        Connect.BillHashId hashId = Connect.BillHashId.newBuilder()
                .setHash(hashid).build();
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.CROWDFUN_INFO, hashId, new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                try {
                    Connect.IMResponse imResponse = Connect.IMResponse.parseFrom(response.getBody().toByteArray());
                    if (!SupportKeyUril.verifySign(imResponse.getSign(), imResponse.getCipherData().toByteArray())) {
                        throw new Exception("Validation fails");
                    }

                    Connect.StructData structData = DecryptionUtil.decodeAESGCMStructData(imResponse.getCipherData());
                    crowdfunding = Connect.Crowdfunding.parseFrom(structData.getPlainData());
                    if (!ProtoBufUtil.getInstance().checkProtoBuf(crowdfunding)) {
                        return;
                    }

                    List<Connect.CrowdfundingRecord> records = crowdfunding.getRecords().getListList();
                    Connect.UserInfo senderInfo = crowdfunding.getSender();
                    GlideUtil.loadAvater(roundimg, senderInfo.getAvatar());
                    String senderName = "";
                    if (MemoryDataManager.getInstance().getAddress().equals(senderInfo.getAddress())) {
                        senderName = activity.getString(R.string.Chat_You);
                    } else {
                        senderName = senderInfo.getUsername();

                        CurrencyEntity currencyEntity = CurrencyHelper.getInstance().loadCurrency(0);
                        if (currencyEntity != null) {
                            txt6.setText(BaseApplication.getInstance().getString(R.string.Wallet_Balance,
                                    RateFormatUtil.longToDoubleBtc(currencyEntity.getBalance())));
                        }
                    }
                    senderNameTxt.setText(getString(R.string.Chat_Crowd_funding_by_who, senderName));

                    if (TextUtils.isEmpty(crowdfunding.getTips())) {
                        txt2.setVisibility(View.GONE);
                    } else {
                        txt2.setText(crowdfunding.getTips());
                    }

                    txt3.setText(String.format(getString(R.string.Wallet_Goal), RateFormatUtil.longToDoubleBtc(crowdfunding.getTotal())));

                    long singePayAmount = crowdfunding.getTotal() / crowdfunding.getSize();
                    if (MemoryDataManager.getInstance().getAddress().equals(senderInfo.getAddress())) {
                        txt4.setText(String.format(getString(R.string.Wallet_BTC_Each), RateFormatUtil.longToDoubleBtc(singePayAmount)));
                    } else {
                        txt4.setText(String.format(getString(R.string.Wallet_BTC_Each), RateFormatUtil.longToDoubleBtc(singePayAmount)));
                    }

                    if (crowdfunding.getStatus() == 0) {
                        layoutFirst.setVisibility(View.GONE);
                        txt6.setVisibility(View.VISIBLE);
                        if (MemoryDataManager.getInstance().getAddress().equals(senderInfo.getAddress())) {//You initiate the raise
                            btn.setVisibility(View.GONE);
                        } else {
                            btn.setVisibility(View.VISIBLE);
                        }
                    } else {
                        layoutFirst.setVisibility(View.VISIBLE);
                        txt6.setVisibility(View.GONE);
                        btn.setVisibility(View.GONE);
                    }

                    int payMemCount = (int) (crowdfunding.getSize() - crowdfunding.getRemainSize());
                    int crowdSize = (int) crowdfunding.getSize();
                    txt5.setText(String.format(getString(R.string.Wallet_members_paid_BTC), payMemCount, crowdSize, "" + RateFormatUtil.longToDoubleBtc(payMemCount * singePayAmount)));

                    TransactionHelper.getInstance().updateTransEntity(hashid, msgId, payMemCount, crowdSize);
                    ContainerBean.sendRecExtMsg(ContainerBean.ContainerType.GATHER_DETAIL, msgId, 1, payMemCount, crowdSize);

                    for (Connect.CrowdfundingRecord record : records) {
                        if (MemoryDataManager.getInstance().getAddress().equals(record.getUser().getAddress())) {
                            txt6.setVisibility(View.GONE);
                            btn.setVisibility(View.GONE);
                        }
                    }

                    paymentAdapter.setDatas(records);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Connect.HttpResponse response) {

            }
        });
    }

    @OnClick({R.id.btn})
    public void OnClickListener(View view) {
        switch (view.getId()) {
            case R.id.btn:
                requestGatherPayment();
                break;
        }
    }

    protected void requestGatherPayment() {
        BaseBusiness baseBusiness = new BaseBusiness(activity,CurrencyEnum.BTC);
        baseBusiness.typePayment(hashid, TransferType.TransactionTypePayment.getType(), new WalletListener<String>() {
            @Override
            public void success(String hashId) {
                String contactName = crowdfunding.getSender().getUsername();
                String noticeContent = getString(R.string.Chat_paid_the_crowd_founding_to, activity.getString(R.string.Chat_You), contactName);
                RecExtBean.getInstance().sendEvent(RecExtBean.ExtType.NOTICE, noticeContent);

                GroupEntity groupEntity = ContactHelper.getInstance().loadGroupEntity(crowdfunding.getGroupHash());
                if (groupEntity != null) {
                    NormalChat normalChat = new GroupChat(groupEntity);
                    MsgEntity msgEntity = normalChat.noticeMsg(noticeContent);
                    MessageHelper.getInstance().insertToMsg(msgEntity.getMsgDefinBean());
                }

                String hashid = crowdfunding.getHashId();
                int paycount = (int) (crowdfunding.getSize() - crowdfunding.getRemainSize());
                int crowdcount = (int) crowdfunding.getSize();
                TransactionHelper.getInstance().updateTransEntity(hashid, msgId, paycount, crowdcount);

                ContainerBean.sendRecExtMsg(ContainerBean.ContainerType.GATHER_DETAIL, msgId, 1, paycount, crowdcount);
                ToastEUtil.makeText(activity, activity.getString(R.string.Wallet_Payment_Successful), 1, new ToastEUtil.OnToastListener() {
                    @Override
                    public void animFinish() {
                        ActivityUtil.goBack(activity);
                    }
                }).show();
            }

            @Override
            public void fail(WalletError error) {

            }
        });
    }

    private class PaymentAdapter extends RecyclerView.Adapter<ViewHolder> {

        private LayoutInflater inflater = LayoutInflater.from(activity);
        private OnItemClickListener itemClickListener;
        private List<Connect.CrowdfundingRecord> crowdRecords = new ArrayList<>();

        public void setDatas(List<Connect.CrowdfundingRecord> records) {
            this.crowdRecords = records;
            notifyDataSetChanged();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = inflater.inflate(R.layout.item_payment, parent, false);
            ViewHolder holder = new ViewHolder(view);
            return holder;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {
            Connect.CrowdfundingRecord crowdRecord = crowdRecords.get(position);
            Connect.UserInfo userInfo = crowdRecord.getUser();

            GlideUtil.loadAvater(holder.avaterRimg, userInfo.getAvatar());
            holder.nameTv.setText(userInfo.getUsername());

            holder.balanceTv.setText(RateFormatUtil.longToDoubleBtc(crowdRecord.getAmount()) + getResources().getString(R.string.Set_BTC_symbol));

            String time = TimeUtil.getTime(crowdRecord.getCreatedAt() * 1000, TimeUtil.DATE_FORMAT_MONTH_HOUR);
            holder.timeTv.setText(time);

            if (crowdRecord.getStatus() == 0) {
                holder.statusTv.setText(getString(R.string.Wallet_Waitting_for_pay));
                holder.statusTv.setTextColor(getResources().getColor(R.color.color_f04a5f));
            } else if (crowdRecord.getStatus() == 1) {
                holder.statusTv.setText(getString(R.string.Wallet_Unconfirmed));
                holder.statusTv.setTextColor(getResources().getColor(R.color.color_f04a5f));
            } else {
                holder.statusTv.setTextColor(getResources().getColor(R.color.color_767a82));
                holder.statusTv.setText(getString(R.string.Wallet_Confirmed));
            }
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (crowdRecords != null && position < crowdRecords.size()) {
                        itemClickListener.itemClick(crowdRecords.get(position));
                    }
                }
            });
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getItemCount() {
            return crowdRecords.size();
        }

        public void setItemClickListener(OnItemClickListener itemClickListener) {
            this.itemClickListener = itemClickListener;
        }
    }

    public interface OnItemClickListener {
        void itemClick(Connect.CrowdfundingRecord record);
    }

    protected class ViewHolder extends RecyclerView.ViewHolder {

        View itemView;
        RoundedImageView avaterRimg;
        RelativeLayout leftRela;
        TextView nameTv;
        TextView balanceTv;
        TextView timeTv;
        TextView statusTv;

        public ViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            avaterRimg = (RoundedImageView) itemView.findViewById(R.id.avater_rimg);
            leftRela = (RelativeLayout) itemView.findViewById(R.id.left_rela);
            nameTv = (TextView) itemView.findViewById(R.id.name_tv);
            balanceTv = (TextView) itemView.findViewById(R.id.balance_tv);
            timeTv = (TextView) itemView.findViewById(R.id.time_tv);
            statusTv = (TextView) itemView.findViewById(R.id.status_tv);
        }
    }
}
