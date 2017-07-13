package connect.activity.wallet;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import connect.activity.base.BaseActivity;
import connect.activity.wallet.adapter.CAddressAdapter;
import connect.activity.wallet.manager.CurrencyType;
import connect.database.green.DaoHelper.CurrencyHelper;
import connect.database.green.bean.CurrencyAddressEntity;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.widget.TopToolBar;

/**
 * Sets the default address
 */
public class CAddressListActivty extends BaseActivity {

    @Bind(R.id.toolbar)
    TopToolBar toolbar;
    @Bind(R.id.recyclerview)
    RecyclerView recyclerview;

    private CAddressListActivty activity;
    private CurrencyType currencyBean;
    private List<CurrencyAddressEntity> currencyAddressEntities = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_caddress_list_activty);
        ButterKnife.bind(this);
    }

    public static void startActivity(Activity activity, CurrencyType bean) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("Currency", bean);
        ActivityUtil.next(activity, CAddressListActivty.class, bundle);
    }

    @Override
    public void initView() {
        activity = this;
        toolbar.setBlackStyle();
        toolbar.setTitle(getString(R.string.Chat_Address));
        toolbar.setLeftImg(R.mipmap.back_white);
        toolbar.setLeftListence(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityUtil.goBack(activity);
            }
        });

        currencyBean = (CurrencyType) getIntent().getSerializableExtra("Currency");
        currencyAddressEntities = CurrencyHelper.getInstance().loadCurrencyAddress(currencyBean.getName());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(activity);
        CAddressAdapter cAddressAdapter = new CAddressAdapter(activity, currencyAddressEntities);
        recyclerview.setLayoutManager(linearLayoutManager);
        recyclerview.setAdapter(cAddressAdapter);
        cAddressAdapter.setItemClickListener(new CAddressAdapter.OnItemClickListener() {
            @Override
            public void onClick(View v) {
                CurrencyAddressEntity addressEntity = (CurrencyAddressEntity) v.getTag();
                updateDefaultAddress(addressEntity);
            }
        });
    }

    public void updateDefaultAddress(CurrencyAddressEntity addressEntity){

    }
}
