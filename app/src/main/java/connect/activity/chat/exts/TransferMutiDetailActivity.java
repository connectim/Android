package connect.activity.chat.exts;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import connect.activity.base.BaseActivity;
import connect.activity.chat.exts.contract.TransferMutiDetailContract;
import connect.activity.chat.exts.presenter.TransferMutiDetailPresenter;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.glide.GlideUtil;
import connect.widget.TopToolBar;
import connect.widget.roundedimageview.RoundedImageView;

public class TransferMutiDetailActivity extends BaseActivity implements TransferMutiDetailContract.BView{

    @Bind(R.id.toolbar)
    TopToolBar toolbar;
    @Bind(R.id.roundimg)
    RoundedImageView roundimg;
    @Bind(R.id.txt1)
    TextView txt1;
    @Bind(R.id.txt2)
    TextView txt2;
    @Bind(R.id.recyclerview)
    RecyclerView recyclerview;

    private TransferMutiDetailActivity activity;
    private TransferMutiDetailContract.Presenter presenter;
    private String hashId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer_muti_detail);
        ButterKnife.bind(this);
        initView();
    }

    public static void startActivity(Activity activity, String hashid, String msgid) {
        Bundle bundle = new Bundle();
        bundle.putString("HASHID", hashid);
        bundle.putString("MESSAGEID", msgid);
        ActivityUtil.next(activity, TransferMutiDetailActivity.class, bundle);
    }

    @Override
    public void initView() {
        activity = this;
        toolbar.setBlackStyle();
        toolbar.setLeftImg(R.mipmap.back_white);
        toolbar.setTitle(getString(R.string.Chat_Transfer_Detail));
        toolbar.setLeftListence(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityUtil.goBack(activity);
            }
        });

        hashId = getIntent().getStringExtra("HASHID");
        new TransferMutiDetailPresenter(this).start();
        presenter.requestTransferDetail(hashId);
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
    public void showSenderInfo(String avatar, String name) {
        GlideUtil.loadAvater(roundimg, avatar);
        txt1.setText(name);
    }
}
