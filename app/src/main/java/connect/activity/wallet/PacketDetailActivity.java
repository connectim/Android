package connect.activity.wallet;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.activity.base.BaseActivity;
import connect.activity.wallet.adapter.RedDerailAdapter;
import connect.activity.wallet.contract.PacketDetailContract;
import connect.activity.wallet.manager.CurrencyType;
import connect.activity.wallet.presenter.PacketDetailPresenter;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.data.RateFormatUtil;
import connect.utils.glide.GlideUtil;
import connect.widget.TopToolBar;
import connect.widget.roundedimageview.RoundedImageView;
import protos.Connect;

/**
 * lucky packet detail
 * Created by Administrator on 2016/12/19.
 */
public class PacketDetailActivity extends BaseActivity implements PacketDetailContract.View {

    @Bind(R.id.toolbar_top)
    TopToolBar toolbarTop;
    @Bind(R.id.avater_rimg)
    RoundedImageView avaterRimg;
    @Bind(R.id.name_tv)
    TextView nameTv;
    @Bind(R.id.btc)
    TextView btc;
    @Bind(R.id.open_money_title_tv)
    TextView openMoneyTitleTv;
    @Bind(R.id.open_detail_tv)
    TextView openDetailTv;
    @Bind(R.id.content_lin)
    LinearLayout contentLin;
    @Bind(R.id.open_money_rela)
    RelativeLayout openMoneyRela;
    @Bind(R.id.no_confirm_tv)
    TextView noConfirmTv;
    @Bind(R.id.overtime_tv)
    TextView overtimeTv;
    @Bind(R.id.note_tv)
    TextView noteTv;
    @Bind(R.id.recyclerview)
    RecyclerView recyclerview;

    private PacketDetailActivity mActivity;
    private PacketDetailContract.Presenter presenter;
    private String hashId;
    private int type;//lucky packet type: 0:inner 1:system
    private Connect.RedPackageInfo redPackageInfo;

    public static void startActivity(Activity activity, Object... objects) {
        Bundle bundle = new Bundle();
        bundle.putString("id", (String) objects[0]);
        if (objects.length == 2) {
            bundle.putInt("type", (Integer) objects[1]);
        }
        ActivityUtil.next(activity, PacketDetailActivity.class, bundle);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet_packet_detail);
        ButterKnife.bind(this);
        initView();
    }

    @Override
    public void initView() {
        mActivity = this;
        toolbarTop.setRedStyle();
        toolbarTop.setBackground(R.color.color_clear);
        toolbarTop.setLeftImg(R.mipmap.back_white);
        toolbarTop.setTitle(null, R.string.Wallet_Packet);
        setPresenter(new PacketDetailPresenter(this));

        Bundle bundle = getIntent().getExtras();
        hashId = bundle.getString("id");
        type = bundle.getInt("type", 0);
        //system packet
        if (0 != type) {
            GlideUtil.loadImage(avaterRimg, R.mipmap.connect_logo);
        }
        presenter.requestRedDetail(hashId, type);
    }

    @Override
    public void setPresenter(PacketDetailContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void updataSendView(Connect.UserInfo sendUserInfo) {
        GlideUtil.loadAvater(avaterRimg, sendUserInfo.getAvatar());
        nameTv.setText(sendUserInfo.getUsername());
    }

    @Override
    public void updataView(int status, long openMoney, long bestAmount, final Connect.RedPackageInfo redPackageInfo) {
        this.redPackageInfo = redPackageInfo;
        contentLin.setVisibility(View.VISIBLE);
        openMoneyTitleTv.setText(RateFormatUtil.longToDoubleBtc(openMoney));

        if (!TextUtils.isEmpty(redPackageInfo.getRedpackage().getTips())) {
            noteTv.setText(redPackageInfo.getRedpackage().getTips());
        }
        openMoneyRela.setVisibility(View.VISIBLE);
        switch (status) {
            case 1: // Bitcoin has been return to your wallet
                overtimeTv.setVisibility(View.VISIBLE);
                overtimeTv.setText(R.string.Chat_Bitcoin_has_been_return_to_your_wallet);
                break;
            case 2: // Waitting for open
                overtimeTv.setVisibility(View.VISIBLE);
                overtimeTv.setText(R.string.Chat_Waitting_for_open);
                toolbarTop.setRightImg(R.mipmap.wallet_share_payment2x);
                break;
            case 3: // Lucky packet Overtime
                overtimeTv.setVisibility(View.VISIBLE);
                overtimeTv.setText(R.string.Chat_Lucky_packet_Overtime);
                break;
            case 4: // Lucky packet transfering to your wallet
                noConfirmTv.setVisibility(View.VISIBLE);
                noConfirmTv.setText(R.string.Chat_Lucky_packet_transfering_to_your_wallet);
                break;
            case 5: // Good luck next time
                noConfirmTv.setVisibility(View.VISIBLE);
                openMoneyRela.setVisibility(View.GONE);
                noConfirmTv.setTextColor(mActivity.getResources().getColor(R.color.color_b3b5bc));
                noConfirmTv.setText(R.string.Wallet_Good_luck_next_time);
                break;
            case 6: // Lucky packet transferred to your wallet
                noConfirmTv.setVisibility(View.VISIBLE);
                noConfirmTv.setText(R.string.Wallet_Lucky_packet_transferred_to_your_wallet);
                break;
            default:
                break;
        }
        Connect.RedPackage redPackage = redPackageInfo.getRedpackage();
        openDetailTv.setText(mActivity.getResources().getString(R.string.Wallet_Opened,
                redPackage.getSize() - redPackage.getRemainSize(),
                redPackage.getSize()));

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mActivity);
        recyclerview.setLayoutManager(linearLayoutManager);
        RedDerailAdapter redDerailAdapter = new RedDerailAdapter(mActivity, redPackageInfo.getGradHistoryList(), bestAmount);
        recyclerview.setAdapter(redDerailAdapter);
        redDerailAdapter.notifyDataSetChanged();
        redDerailAdapter.setItemClickListener(new RedDerailAdapter.OnItemClickListener() {
            @Override
            public void itemClick(Connect.GradRedPackageHistroy histroy) {
                String txId = redPackageInfo.getRedpackage().getTxid();
                if (!TextUtils.isEmpty(txId)) {
                    BlockchainActivity.startActivity(mActivity, CurrencyType.BTC, txId);
                }
            }
        });
    }

    @OnClick(R.id.left_img)
    void goback(View view) {
        ActivityUtil.goBack(mActivity);
    }

    @OnClick(R.id.right_lin)
    void goshare(View view) {
        String url = redPackageInfo.getRedpackage().getUrl();
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, url);
        shareIntent.setType("text/plain");
        startActivity(Intent.createChooser(shareIntent, "share to"));
    }
}
