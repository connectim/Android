package connect.activity.login;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import connect.ui.activity.R;
import connect.activity.login.adapter.CountryAdapter;
import connect.activity.login.bean.CountryBean;
import connect.activity.base.BaseActivity;
import connect.utils.ActivityUtil;
import connect.utils.data.PhoneDataUtil;
import connect.widget.SideBar;
import connect.widget.TopToolBar;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Administrator on 2017/1/5.
 */
public class CountryCodeActivity extends BaseActivity {

    @Bind(R.id.toolbar_top)
    TopToolBar toolbarTop;
    @Bind(R.id.list_view)
    ListView listView;
    @Bind(R.id.siderbar)
    SideBar siderbar;

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

        adapter = new CountryAdapter();
        listView.setAdapter(adapter);
        List<CountryBean> list = PhoneDataUtil.getInstance().getCountryData();
        adapter.setDataNotify(list);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CountryBean countryBean = (CountryBean) parent.getAdapter().getItem(position);
                Bundle bundle = new Bundle();
                bundle.putSerializable("country", countryBean);
                ActivityUtil.goBackWithResult(mActivity, Activity.RESULT_OK, bundle);
            }
        });

        siderbar.setOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener() {
            @Override
            public void onTouchingLetterChanged(String s) {
                int position = adapter.getPositionForSection(s.charAt(0));
                if(position >= 0){
                    listView.setSelection(position);
                }
            }
        });
    }

    @OnClick(R.id.left_img)
    void goBack(View view) {
        ActivityUtil.goBack(mActivity);
    }
}
