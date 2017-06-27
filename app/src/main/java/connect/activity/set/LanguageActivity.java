package connect.activity.set;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.nio.ByteBuffer;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.database.SharedPreferenceUtil;
import connect.ui.activity.R;
import connect.activity.home.HomeActivity;
import connect.activity.set.adapter.CurrencyAdapter;
import connect.activity.set.manager.LanguageData;
import connect.activity.wallet.bean.RateBean;
import connect.activity.base.BaseActivity;
import connect.activity.base.BaseApplication;
import connect.service.bean.PushMessage;
import connect.service.bean.ServiceAck;
import connect.utils.ActivityUtil;
import connect.utils.system.SystemDataUtil;
import connect.widget.TopToolBar;

/**
 * Within the application language switching
 */
public class LanguageActivity extends BaseActivity {

    @Bind(R.id.toolbar_top)
    TopToolBar toolbarTop;
    @Bind(R.id.list_view)
    ListView listView;

    private LanguageActivity mActivity;
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
        toolbarTop.setRightTextColor(R.color.color_00c400);

        List<RateBean> list = LanguageData.getInstance().getLanguageData();
        adapter = new CurrencyAdapter();

        String languageCode = SharedPreferenceUtil.getInstance().getStringValue(SharedPreferenceUtil.APP_LANGUAGE_CODE);
        adapter.setSeleCurrency(languageCode);

        listView.setAdapter(adapter);
        adapter.setDataNotify(list);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                RateBean rateBean = (RateBean)parent.getAdapter().getItem(position);
                adapter.setSeleCurrency(rateBean.getCode());
                adapter.notifyDataSetChanged();
            }
        });
    }

    @OnClick(R.id.left_img)
    void goback(View view){
        ActivityUtil.goBack(mActivity);
    }

    @OnClick(R.id.right_lin)
    void save(View view) {
        String code = adapter.getSeleCurrency();
        if (!TextUtils.isEmpty(code)) {
            PushMessage.pushMessage(ServiceAck.STOP_CONNECT, ByteBuffer.allocate(0));
            changeLanguage(code);
        }
    }

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
