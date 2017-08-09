package connect.activity.set;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AdapterView;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.activity.base.BaseActivity;
import connect.activity.set.adapter.CurrencyAdapter;
import connect.activity.wallet.bean.RateBean;
import connect.database.green.DaoHelper.ParamManager;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.data.RateDataUtil;
import connect.widget.TopToolBar;

/**
 * Choose the country currency
 * Created by Administrator on 2017/1/12.
 */
public class CurrencyActivity extends BaseActivity {

    @Bind(R.id.toolbar_top)
    TopToolBar toolbarTop;
    @Bind(R.id.recyclerview)
    RecyclerView recyclerview;


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
        adapter = new CurrencyAdapter(mActivity);

        RateBean rateBean = ParamManager.getInstance().getCountryRate();
        adapter.setSeleCurrency(rateBean == null ? "" : rateBean.getCode());

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mActivity);
        recyclerview.setLayoutManager(linearLayoutManager);
        recyclerview.setAdapter(adapter);
        adapter.setDataNotify(list);
        adapter.setItemClickListener(new CurrencyAdapter.OnItemClickListener() {
            @Override
            public void itemClick(RateBean rateBean) {
                ParamManager.getInstance().putCountryRate(rateBean);
                adapter.setSeleCurrency(rateBean.getCode());
                adapter.notifyDataSetChanged();
            }
        });
    }

    @OnClick(R.id.left_img)
    void goback(View view) {
        ActivityUtil.goBack(mActivity);
    }

}
