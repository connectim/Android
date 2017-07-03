package connect.ui.activity.set;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.db.green.DaoHelper.ParamManager;
import connect.ui.activity.R;
import connect.ui.activity.set.adapter.CurrencyAdapter;
import connect.ui.activity.wallet.bean.RateBean;
import connect.ui.base.BaseActivity;
import connect.utils.ActivityUtil;
import connect.utils.data.RateDataUtil;
import connect.view.TopToolBar;

/**
 * Choose the country currency
 * Created by Administrator on 2017/1/12.
 */
public class CurrencyActivity extends BaseActivity {

    @Bind(R.id.toolbar_top)
    TopToolBar toolbarTop;
    @Bind(R.id.list_view)
    ListView listView;

    private CurrencyActivity mActivity;
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
        toolbarTop.setTitle(null, R.string.Set_Currency);

        List<RateBean> list = RateDataUtil.getInstance().getRateData();
        adapter = new CurrencyAdapter();

        RateBean rateBean = ParamManager.getInstance().getCountryRate();
        adapter.setSeleCurrency(rateBean == null ? "" : rateBean.getCode());

        listView.setAdapter(adapter);
        adapter.setDataNotify(list);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                RateBean rateBean = (RateBean)parent.getAdapter().getItem(position);
                ParamManager.getInstance().putCountryRate(rateBean);
                adapter.setSeleCurrency(rateBean.getCode());
                adapter.notifyDataSetChanged();
            }
        });
    }

    @OnClick(R.id.left_img)
    void goback(View view){
        ActivityUtil.goBack(mActivity);
    }

}
