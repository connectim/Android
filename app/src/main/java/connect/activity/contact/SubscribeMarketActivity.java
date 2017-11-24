package connect.activity.contact;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.RelativeLayout;

import com.google.protobuf.ByteString;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.activity.base.BaseFragmentActivity;
import connect.activity.contact.adapter.FragmentAdapter;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.UriUtil;
import connect.utils.cryption.DecryptionUtil;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import connect.widget.TopToolBar;
import protos.Connect;

public class SubscribeMarketActivity extends BaseFragmentActivity {

    @Bind(R.id.toolbar)
    TopToolBar toolbar;
    @Bind(R.id.tab_rlayout)
    RelativeLayout tabRlayout;
    @Bind(R.id.view_pager)
    ViewPager viewPager;
    @Bind(R.id.tab_layout)
    TabLayout tabLayout;

    private SubscribeMarketActivity mActivity;
    List<Fragment> fragments = new ArrayList<>();
    private FragmentAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_subscribe_market);
        ButterKnife.bind(this);
        initView();
    }

    @Override
    public void initView() {
        mActivity = this;
        toolbar.setBlackStyle();
        toolbar.setLeftImg(R.mipmap.back_white);
        toolbar.setTitle(null, R.string.Link_Digital_currency_quotes);

        getMarket();
    }

    @OnClick(R.id.left_img)
    void goBack(View view) {
        ActivityUtil.goBack(mActivity);
    }

    private void getMarket(){
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.CONNECT_V2_MARKET_EXCHANGE, ByteString.copyFrom(new byte[]{}), new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                try{
                    Connect.IMResponse imResponse = Connect.IMResponse.parseFrom(response.getBody().toByteArray());
                    Connect.StructData structData = DecryptionUtil.decodeAESGCMStructData(imResponse.getCipherData());
                    Connect.Exchanges exchanges = Connect.Exchanges.parseFrom(structData.getPlainData());
                    List<Connect.Exchange> list = exchanges.getListList();
                    Connect.Exchange exchange1 = Connect.Exchange.newBuilder().setName("0").build();
                    ArrayList<String> titleList = new ArrayList<>();
                    tabLayout.addTab(tabLayout.newTab().setText(exchange1.getName()));
                    fragments.add(SubscribeMarketFragment.newInstance(exchange1));
                    titleList.add(mActivity.getString(R.string.Link_Market_value));

                    for(Connect.Exchange exchange : list){
                        tabLayout.addTab(tabLayout.newTab().setText(exchange.getName()));
                        fragments.add(SubscribeMarketFragment.newInstance(exchange));
                        titleList.add(exchange.getName());
                    }
                    adapter = new FragmentAdapter(mActivity.getSupportFragmentManager(), fragments, titleList);
                    viewPager.setAdapter(adapter);
                    tabLayout.setupWithViewPager(viewPager);
                    tabLayout.setTabsFromPagerAdapter(adapter);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Connect.HttpResponse response) {}
        });
    }

}
