package connect.activity.contact;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.activity.base.BaseFragmentActivity;
import connect.activity.contact.adapter.FragmentAdapter;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.widget.TopToolBar;

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
    private List<String> tabDatas = new ArrayList<>();
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
        toolbar.setTitle(null, R.string.Link_Friend_Recommendation);

        tabDatas.clear();
        fragments.clear();
        tabDatas.add("okex");
        tabDatas.add("huobi.pro");
        tabDatas.add("bitfinex");
        tabDatas.add("bittrex");
        tabDatas.add("poloniex");
        tabDatas.add("binance");

        for (int i = 0; i < tabDatas.size(); i++) {
            tabLayout.addTab(tabLayout.newTab().setText(tabDatas.get(i)));
            fragments.add(SubscribeMarketFragment.newInstance(tabDatas.get(i)));
        }
        adapter = new FragmentAdapter(mActivity.getSupportFragmentManager(), fragments, tabDatas);
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setTabsFromPagerAdapter(adapter);
    }

    @OnClick(R.id.left_img)
    void goBack(View view) {
        ActivityUtil.goBack(mActivity);
    }

}
