package connect.ui.activity.chat.exts;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.db.MemoryDataManager;
import connect.db.green.DaoHelper.ParamManager;
import connect.db.green.DaoHelper.TransactionHelper;
import connect.ui.activity.R;
import connect.ui.activity.chat.bean.ContainerBean;
import connect.ui.activity.chat.bean.RecExtBean;
import connect.ui.activity.wallet.BlockchainActivity;
import connect.ui.activity.wallet.bean.WalletAccountBean;
import connect.utils.ProtoBufUtil;
import connect.utils.transfer.TransferError;
import connect.utils.transfer.TransferUtil;
import connect.ui.base.BaseActivity;
import connect.ui.base.BaseApplication;
import connect.utils.ActivityUtil;
import connect.utils.ToastEUtil;
import connect.utils.data.RateFormatUtil;
import connect.utils.TimeUtil;
import connect.utils.UriUtil;
import connect.utils.cryption.DecryptionUtil;
import connect.utils.cryption.SupportKeyUril;
import connect.utils.glide.GlideUtil;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import connect.view.MdStyleProgress;
import connect.view.TopToolBar;
import connect.view.payment.PaymentPwd;
import connect.view.roundedimageview.RoundedImageView;
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
    @Bind(R.id.listview)
    ListView listview;
    @Bind(R.id.txt5)
    TextView txt5;
    @Bind(R.id.btn)
    Button btn;
    @Bind(R.id.layout_first)
    LinearLayout layoutFirst;
    @Bind(R.id.txt6)
    TextView txt6;

    private GatherDetailGroupActivity activity;
    private static String GATHER_HASHID = "GATHER_HASHID";
    private static String GATHER_MSGID = "GATHER_MSGID";

    private PaymentAdapter paymentAdapter;
    private PaymentPwd paymentPwd;

    private String msgId;
    private Connect.Crowdfunding crowdfunding = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gather_groupdetail);
        ButterKnife.bind(this);
        initView();
    }

    public static void startActivity(Activity activity, String... strings) {
        Bundle bundle = new Bundle();
        bundle.putString(GATHER_HASHID, strings[0]);
        if (strings.length == 2) {
            bundle.putString(GATHER_MSGID, strings[1]);
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

        paymentAdapter = new PaymentAdapter();
        listview.setAdapter(paymentAdapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Connect.CrowdfundingRecord record = (Connect.CrowdfundingRecord) parent.getItemAtPosition(position);
                if (record != null) {
                    BlockchainActivity.startActivity(activity, record.getTxid());
                }
            }
        });

        String hashid = getIntent().getStringExtra(GATHER_HASHID);
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
                    if(!ProtoBufUtil.getInstance().checkProtoBuf(crowdfunding)){
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
                        requestWallet();
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
                    int crowdSize=(int) crowdfunding.getSize();
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

    /**
     * Get the wallet balance
     */
    private void requestWallet() {
        String url = String.format(UriUtil.BLOCKCHAIN_UNSPENT_INFO, MemoryDataManager.getInstance().getAddress());
        OkHttpUtil.getInstance().get(url, new ResultCall<Connect.HttpNotSignResponse>() {
            @Override
            public void onResponse(Connect.HttpNotSignResponse response) {
                try {
                    if (response.getCode() == 2000) {
                        Connect.UnspentAmount unspentAmount = Connect.UnspentAmount.parseFrom(response.getBody());
                        if(ProtoBufUtil.getInstance().checkProtoBuf(unspentAmount)){
                            WalletAccountBean accountBean = new WalletAccountBean(unspentAmount.getAmount(), unspentAmount.getAvaliableAmount());
                            txt6.setText(BaseApplication.getInstance().getString(R.string.Wallet_Balance,
                                    RateFormatUtil.longToDoubleBtc(accountBean.getAvaAmount())));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Connect.HttpNotSignResponse response) {
                txt6.setText(BaseApplication.getInstance().getString(R.string.Wallet_Balance,
                        RateFormatUtil.longToDoubleBtc(0)));
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
        final long amount = crowdfunding.getTotal() / crowdfunding.getSize();
        WalletAccountBean accountBean = ParamManager.getInstance().getWalletAmount();
        new TransferUtil().getOutputTran(activity, MemoryDataManager.getInstance().getAddress(), false,
                crowdfunding.getSender().getAddress(), accountBean.getAvaAmount(),amount,
                new TransferUtil.OnResultCall() {
            @Override
            public void result(String inputString, String outputString) {
                checkPayPassword(inputString, outputString);
            }
        });
    }

    private void checkPayPassword(final String inputString, final String outputString) {
        if (!TextUtils.isEmpty(outputString)) {
            paymentPwd = new PaymentPwd();
            paymentPwd.showPaymentPwd(activity, new PaymentPwd.OnTrueListener() {
                @Override
                public void onTrue() {
                    String samValue = new TransferUtil().getSignRawTrans(MemoryDataManager.getInstance().getPriKey(), inputString, outputString);
                    groupMemPayment(samValue);
                }
            });
        }
    }

    /**
     * payment
     *
     * @param rawtx
     */
    public void groupMemPayment(String rawtx) {
        Connect.PayCrowdfunding funding = Connect.PayCrowdfunding.newBuilder()
                .setAmount(crowdfunding.getTotal() / crowdfunding.getSize())
                .setRawTx(rawtx)
                .setHashId(crowdfunding.getHashId()).build();
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.CROWDFUN_PAY, funding, new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                try {
                    Connect.IMResponse imResponse = Connect.IMResponse.parseFrom(response.getBody().toByteArray());
                    paymentPwd.closeStatusDialog(MdStyleProgress.Status.LoadSuccess);
                    if (!SupportKeyUril.verifySign(imResponse.getSign(), imResponse.getCipherData().toByteArray())) {
                        throw new Exception("Validation fails");
                    }

                    Connect.StructData structData = DecryptionUtil.decodeAESGCMStructData(imResponse.getCipherData());
                    Connect.Crowdfunding crowdfunding = Connect.Crowdfunding.parseFrom(structData.getPlainData());
                    if(!ProtoBufUtil.getInstance().checkProtoBuf(crowdfunding)){
                        return;
                    }

                    String contactName = crowdfunding.getSender().getUsername();
                    String noticeContent = getString(R.string.Chat_paid_the_crowd_founding_to, activity.getString(R.string.Chat_You), contactName);
                    RecExtBean.sendRecExtMsg(RecExtBean.ExtType.NOTICE, noticeContent);

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
                } catch (Exception e) {
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

    private class PaymentAdapter extends BaseAdapter {

        private Connect.CrowdfundingRecord crowdRecord;
        private List<Connect.CrowdfundingRecord> crowdRecords = new ArrayList<>();

        public void setDatas(List<Connect.CrowdfundingRecord> records) {
            this.crowdRecords = records;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return crowdRecords.size();
        }

        @Override
        public Object getItem(int position) {
            return crowdRecords.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (null == convertView) {
                convertView = LayoutInflater.from(activity).inflate(R.layout.item_payment, parent, false);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            crowdRecord = crowdRecords.get(position);
            Connect.UserInfo userInfo = crowdRecord.getUser();

            GlideUtil.loadAvater(holder.avaterRimg, userInfo.getAvatar());
            holder.nameTv.setText(userInfo.getUsername());

            holder.balanceTv.setText(RateFormatUtil.longToDoubleBtc(crowdRecord.getAmount()) + getResources().getString(R.string.Set_BTC_symbol));

            String time = TimeUtil.getTime(crowdRecord.getCreatedAt() * 1000,TimeUtil.DATE_FORMAT_MONTH_HOUR);
            holder.timeTv.setText(time);

            if (crowdRecord.getStatus() == 0) {
                holder.statusTv.setText(parent.getContext().getString(R.string.Wallet_Waitting_for_pay));
                holder.statusTv.setTextColor(getResources().getColor(R.color.color_f04a5f));
            } else if (crowdRecord.getStatus() == 1) {
                holder.statusTv.setText(parent.getContext().getString(R.string.Wallet_Unconfirmed));
                holder.statusTv.setTextColor(getResources().getColor(R.color.color_f04a5f));
            } else {
                holder.statusTv.setTextColor(getResources().getColor(R.color.color_767a82));
                holder.statusTv.setText(parent.getContext().getString(R.string.Wallet_Confirmed));
            }
            return convertView;
        }
    }

    protected static class ViewHolder {
        @Bind(R.id.avater_rimg)
        RoundedImageView avaterRimg;
        @Bind(R.id.left_rela)
        RelativeLayout leftRela;
        @Bind(R.id.name_tv)
        TextView nameTv;
        @Bind(R.id.balance_tv)
        TextView balanceTv;
        @Bind(R.id.time_tv)
        TextView timeTv;
        @Bind(R.id.status_tv)
        TextView statusTv;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
