package connect.activity.login;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.activity.base.BaseActivity;
import connect.activity.login.adapter.CountryAdapter;
import connect.activity.login.bean.CountryBean;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.data.PhoneDataUtil;
import connect.widget.SideBar;
import connect.widget.TopToolBar;

/**
 * Choose the country code number.
 */
public class CountryCodeActivity extends BaseActivity {

    @Bind(R.id.toolbar_top)
    TopToolBar toolbarTop;
    @Bind(R.id.siderbar)
    SideBar siderbar;
    @Bind(R.id.recyclerview)
    RecyclerView recyclerview;

    private LinearLayoutManager linearLayoutManager;
    private CountryAdapter adapter;
    private CountryCodeActivity mActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_country_code);
        ButterKnife.bind(this);
        initView();
    }

    @Override
    public void initView() {
        mActivity = this;
        toolbarTop.setLeftImg(R.mipmap.back_black);
        toolbarTop.setTitle(null, R.string.Login_Select_Country);

        linearLayoutManager = new LinearLayoutManager(mActivity);
        recyclerview.setLayoutManager(linearLayoutManager);
        adapter = new CountryAdapter(mActivity);
        recyclerview.setAdapter(adapter);
        List<CountryBean> list = PhoneDataUtil.getInstance().getCountryData();
        adapter.setDataNotify(list);
        adapter.setItemClickListener(onItemClickListener);
        siderbar.setOnTouchingLetterChangedListener(changedListener);
    }

    @OnClick(R.id.left_img)
    void goBack(View view) {
        ActivityUtil.goBack(mActivity);
    }

    /**
     * Click the item back.
     */
    CountryAdapter.OnItemClickListener onItemClickListener = new CountryAdapter.OnItemClickListener() {
        @Override
        public void itemClick(CountryBean countryBean) {
            Bundle bundle = new Bundle();
            bundle.putSerializable("country", countryBean);
            ActivityUtil.goBackWithResult(mActivity, Activity.RESULT_OK, bundle);
        }
    };

    /**
     * Initials location click callback.
     */
    SideBar.OnTouchingLetterChangedListener changedListener = new SideBar.OnTouchingLetterChangedListener() {
        @Override
        public void onTouchingLetterChanged(String s) {
            int position = adapter.getPositionForSection(s.charAt(0));
            if (position >= 0) {
                linearLayoutManager.scrollToPosition(position);
            }
        }
    };

}
