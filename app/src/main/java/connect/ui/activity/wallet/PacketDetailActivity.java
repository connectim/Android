package connect.ui.activity.wallet;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.ui.activity.R;
import connect.ui.activity.wallet.adapter.RedDerailAdapter;
import connect.ui.activity.wallet.contract.PacketDetailContract;
import connect.ui.activity.wallet.presenter.PacketDetailPresenter;
import connect.ui.base.BaseActivity;
import connect.utils.ActivityUtil;
import connect.utils.data.RateFormatUtil;
import connect.utils.glide.GlideUtil;
import connect.view.TopToolBar;
import connect.view.roundedimageview.RoundedImageView;
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
    @Bind(R.id.list_view)
    ListView listView;
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
        presenter.requestRedDetail(hashId,type);
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
    public void updataView(int status, long openMoney, final Connect.RedPackageInfo redPackageInfo) {
        this.redPackageInfo = redPackageInfo;
        contentLin.setVisibility(View.VISIBLE);
        openMoneyTitleTv.setText(RateFormatUtil.longToDoubleBtc(openMoney));

        List<Connect.GradRedPackageHistroy> list = redPackageInfo.getGradHistoryList();
        Connect.RedPackage redPackage = redPackageInfo.getRedpackage();
        if (redPackage.getDeadline() < 0)
            overtimeTv.setVisibility(View.VISIBLE);
        if (!redPackage.getExpired() && redPackage.getRemainSize() > 0 && redPackage.getTyp() == 1) {

        }
        if(!TextUtils.isEmpty(redPackageInfo.getRedpackage().getTips())){
            noteTv.setText(redPackageInfo.getRedpackage().getTips());
        }
        openMoneyRela.setVisibility(View.VISIBLE);

        switch (status) {
            case 0:
                overtimeTv.setVisibility(View.GONE);
                break;
            case 1://Red envelope timeout and refund is not complete (hair)
                overtimeTv.setVisibility(View.VISIBLE);
                overtimeTv.setText(R.string.Chat_Bitcoin_has_been_return_to_your_wallet);
                break;
            case 2://A red envelope timeout refund finish (hair)
                overtimeTv.setVisibility(View.VISIBLE);
                overtimeTv.setText(R.string.Wallet_Overtime_Bitcoin_has_been_return_to_your_wallet);
                break;
            case 3://Red envelope timeout (closed)
                overtimeTv.setVisibility(View.VISIBLE);
                overtimeTv.setText(R.string.Chat_Lucky_packet_Overtime);
                break;
            case 4://Red packets waiting to be receiving (hair)
                noConfirmTv.setVisibility(View.VISIBLE);
                noConfirmTv.setText(R.string.Chat_Waitting_for_open);
                toolbarTop.setRightImg(R.mipmap.wallet_share_payment2x);
                break;
            case 5://Red envelope has been brought out (closed)
                noConfirmTv.setVisibility(View.VISIBLE);
                openMoneyRela.setVisibility(View.GONE);
                noConfirmTv.setTextColor(mActivity.getResources().getColor(R.color.color_b3b5bc));
                noConfirmTv.setText(R.string.Wallet_Good_luck_next_time);
                break;
            case 6://A red envelope into your account (closed)
                noConfirmTv.setVisibility(View.VISIBLE);
                noConfirmTv.setText(R.string.Wallet_Lucky_packet_transferred_to_your_wallet);
                break;
            default:
                break;
        }

        openDetailTv.setText(mActivity.getResources().getString(R.string.Wallet_Opened,
                redPackage.getSize() - redPackage.getRemainSize(),
                redPackage.getSize()));

        String bestAddress = "";
        long bestAmount = 0;
        for(Connect.GradRedPackageHistroy histroy : list){
            if(bestAmount == 0 || bestAmount <= histroy.getAmount()){
                bestAddress = histroy.getUserinfo().getAddress();
                bestAmount = histroy.getAmount();
            }
        }
        RedDerailAdapter redDerailAdapter = new RedDerailAdapter(list,bestAmount);
        listView.setAdapter(redDerailAdapter);
        redDerailAdapter.notifyDataSetChanged();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String txId = redPackageInfo.getRedpackage().getTxid();
                if (!TextUtils.isEmpty(txId)) {
                    BlockchainActivity.startActivity(mActivity, txId);
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
