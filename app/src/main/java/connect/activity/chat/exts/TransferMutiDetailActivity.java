package connect.activity.chat.exts;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import connect.activity.base.BaseActivity;
import connect.activity.chat.adapter.TransferMutiDetailAdapter;
import connect.activity.chat.bean.ContainerBean;
import connect.activity.chat.exts.contract.TransferMutiDetailContract;
import connect.activity.chat.exts.presenter.TransferMutiDetailPresenter;
import connect.activity.home.view.LineDecoration;
import connect.database.green.DaoHelper.TransactionHelper;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.data.RateFormatUtil;
import connect.utils.glide.GlideUtil;
import connect.widget.TopToolBar;

public class TransferMutiDetailActivity extends BaseActivity implements TransferMutiDetailContract.BView{

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
    @Bind(R.id.recyclerview)
    RecyclerView recyclerview;

    private TransferMutiDetailActivity activity;
    private static String TAG = "_TransferMutiDetailActivity";
    private static String HASH_ID = "HASH_ID";
    private static String MESSAGE_ID = "MESSAGE_ID";

    private String hashId;
    private String messageId;

    private TransferMutiDetailContract.Presenter presenter;
    private TransferMutiDetailAdapter mutiDetailAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer_muti_detail);
        ButterKnife.bind(this);
        initView();
    }

    public static void startActivity(Activity activity, String hashid, String msgid) {
        Bundle bundle = new Bundle();
        bundle.putString(HASH_ID, hashid);
        bundle.putString(MESSAGE_ID, msgid);
        ActivityUtil.next(activity, TransferMutiDetailActivity.class, bundle);
    }

    @Override
    public void initView() {
        activity = this;
        toolbar.setBlackStyle();
        toolbar.setLeftImg(R.mipmap.back_white);
        toolbar.setTitle(getString(R.string.Chat_Transfer_Detail));
        toolbar.setLeftListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityUtil.goBack(activity);
            }
        });

        hashId = getIntent().getStringExtra(HASH_ID);
        messageId = getIntent().getStringExtra(MESSAGE_ID);
        new TransferMutiDetailPresenter(this).start();
        presenter.requestTransferDetail(hashId);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(activity);
        mutiDetailAdapter = new TransferMutiDetailAdapter(new String[]{},0L);
        recyclerview.setLayoutManager(linearLayoutManager);
        recyclerview.addItemDecoration(new LineDecoration(activity));
        recyclerview.setAdapter(mutiDetailAdapter);
        mutiDetailAdapter.setItemClickListener(new TransferMutiDetailAdapter.OnItemClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }


    @Override
    public void setPresenter(TransferMutiDetailContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public Activity getActivity() {
        return activity;
    }

    @Override
    public void showTransferDetail(String sender, String[] receivers, String tips, long amount,int transferstate, long createtime) {
        presenter.requestSenderInfo(sender);

        if (TextUtils.isEmpty(tips)) {
            txt2.setVisibility(View.GONE);
        } else {
            txt2.setText(tips);
        }

        long totalamount = amount * receivers.length;
        txt3.setText(getString(R.string.Set_BTC_symbol) + "" + RateFormatUtil.longToDoubleBtc(totalamount));
        mutiDetailAdapter.updateData(receivers,amount);

        TransactionHelper.getInstance().updateTransEntity(hashId, messageId, transferstate);
        ContainerBean.sendRecExtMsg(ContainerBean.ContainerType.TRANSFER_STATE, messageId, hashId);
    }

    @Override
    public void showSenderInfo(String avatar, String name) {
        GlideUtil.loadAvatarRound(roundimg, avatar);
        txt1.setText(name);
    }
}
