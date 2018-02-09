package connect.activity.login;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import connect.database.SharedPreferenceUtil;
import connect.ui.activity.R;
import connect.widget.ViewPagerAdapter;
import connect.activity.base.BaseActivity;
import connect.utils.ActivityUtil;
import connect.widget.GuidePointView;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * App guide page.
 */
public class GuidePageActivity extends BaseActivity {

    @Bind(R.id.viewpager)
    ViewPager viewpager;
    @Bind(R.id.start_message_tv)
    TextView startMessageTv;

    private GuidePageActivity mActivity;
    private int[] titleId = new int[]{R.string.Login_guide_encryptedChat
            ,R.string.Login_guide_bitcoinWallet
            ,R.string.Login_guide_funStickers};
    private int[] describeId = new int[]{R.string.Login_guide_encryptedChat_guide_encryptedChatDescribe
            ,R.string.Login_guide_bitcoinWalletDescribe
            ,R.string.Login_guide_funStickersDescribe};
    private int[] imagesId = new int[]{R.mipmap.bitcoin_wallet2x
            ,R.mipmap.encrypted_chat2x
            ,R.mipmap.fun_stickers2x};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_guide);
        ButterKnife.bind(this);
        initView();
    }

    @Override
    public void initView() {
        mActivity = this;
        ArrayList<View> arrayList = new ArrayList<>();
        for (int i = 0;i < titleId.length;i ++) {
            View view = LayoutInflater.from(mActivity).inflate(R.layout.item_login_guide_1,null);
            TextView titleTv = (TextView) view.findViewById(R.id.title_tv);
            TextView describeTv = (TextView) view.findViewById(R.id.describe_tv);
            ImageView imageView = (ImageView) view.findViewById(R.id.image_view);
            GuidePointView pointView = (GuidePointView) view.findViewById(R.id.point_view);

            titleTv.setText(titleId[i]);
            describeTv.setText(describeId[i]);
            imageView.setImageResource(imagesId[i]);
            pointView.setSelePoint(i);
            arrayList.add(view);
        }
        viewpager.setAdapter(new ViewPagerAdapter(arrayList));
    }

    @OnClick(R.id.start_message_tv)
    void startChat(){
        SharedPreferenceUtil.getInstance().putValue(SharedPreferenceUtil.FIRST_INTO_APP, 1);
        LoginUserActivity.startActivity(mActivity);
        finish();
    }
}
