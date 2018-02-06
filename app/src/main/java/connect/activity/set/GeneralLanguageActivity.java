package connect.activity.set;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;

import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.activity.base.BaseActivity;
import connect.activity.base.BaseApplication;
import connect.activity.home.HomeActivity;
import connect.activity.home.view.LineDecoration;
import connect.activity.set.adapter.CurrencyAdapter;
import connect.utils.data.LanguageData;
import connect.database.SharedPreferenceUtil;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.data.RateBean;
import connect.utils.system.SystemDataUtil;
import connect.widget.TopToolBar;

/**
 * Within the application language switching
 */
public class GeneralLanguageActivity extends BaseActivity {

    @Bind(R.id.toolbar_top)
    TopToolBar toolbarTop;
    @Bind(R.id.recyclerview)
    RecyclerView recyclerview;

    private GeneralLanguageActivity mActivity;
    private CurrencyAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_curency);
        ButterKnife.bind(this);
        initView();
    }

    @Override
    public void initView() {
        mActivity = this;
        toolbarTop.setBlackStyle();
        toolbarTop.setLeftImg(R.mipmap.back_white);
        toolbarTop.setTitle(null, R.string.Set_Language);
        toolbarTop.setRightText(R.string.Set_Save);
        toolbarTop.setRightTextEnable(true);

        bindingAdapter();
    }

    @OnClick(R.id.left_img)
    void goback(View view) {
        ActivityUtil.goBack(mActivity);
    }

    @OnClick(R.id.right_lin)
    void save(View view) {
        String code = adapter.getSeleCurrency();
        if (!TextUtils.isEmpty(code)) {
            changeLanguage(code);
        }
    }

    private void bindingAdapter(){
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mActivity);
        recyclerview.setLayoutManager(linearLayoutManager);
        recyclerview.addItemDecoration(new LineDecoration(mActivity));
        List<RateBean> list = LanguageData.getInstance().getLanguageData();
        adapter = new CurrencyAdapter(mActivity);

        String languageCode = SharedPreferenceUtil.getInstance().getStringValue(SharedPreferenceUtil.APP_LANGUAGE_CODE);
        if(TextUtils.isEmpty(languageCode)){
            Locale myLocale = Locale.getDefault();
            languageCode = myLocale.getLanguage();
        }
        adapter.setSeleCurrency(languageCode);

        recyclerview.setAdapter(adapter);
        adapter.setDataNotify(list);
        adapter.setItemClickListener(new CurrencyAdapter.OnItemClickListener() {
            @Override
            public void itemClick(RateBean rateBean) {
                adapter.setSeleCurrency(rateBean.getCode());
                adapter.notifyDataSetChanged();
            }
        });
    }

    /**
     * Change within the App language
     *
     * @param languageCode language code
     */
    private void changeLanguage(String languageCode) {
        SystemDataUtil.setAppLanguage(mActivity, languageCode);
        SharedPreferenceUtil.getInstance().putValue(SharedPreferenceUtil.APP_LANGUAGE_CODE, languageCode);
        List<Activity> list = BaseApplication.getInstance().getActivityList();
        for (Activity activity : list) {
            if (!activity.getClass().getName().equals(mActivity.getClass().getName())) {
                activity.finish();
            }
        }
        HomeActivity.startActivity(mActivity);
        mActivity.finish();
    }

}
