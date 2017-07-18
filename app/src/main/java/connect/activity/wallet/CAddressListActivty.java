package connect.activity.wallet;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.google.protobuf.InvalidProtocolBufferException;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import connect.activity.base.BaseActivity;
import connect.activity.wallet.adapter.CAddressAdapter;
import connect.activity.wallet.manager.CurrencyManage;
import connect.activity.wallet.manager.CurrencyType;
import connect.database.green.DaoHelper.CurrencyHelper;
import connect.database.green.bean.CurrencyAddressEntity;
import connect.database.green.bean.CurrencyEntity;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.ProtoBufUtil;
import connect.utils.UriUtil;
import connect.utils.cryption.DecryptionUtil;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import connect.widget.TopToolBar;
import protos.Connect;
import wallet_gateway.WalletOuterClass;

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
    private CurrencyEntity currencyEntity;
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
        currencyEntity = CurrencyHelper.getInstance().loadCurrency(currencyBean.getCode());
        currencyAddressEntities = CurrencyHelper.getInstance().loadCurrencyAddress(currencyBean.getCode());
        currencyAddressEntities = CurrencyHelper.getInstance().loadCurrencyAddress(0);
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

    public void updateDefaultAddress(final CurrencyAddressEntity addressEntity) {
//        WalletOuterClass.RequestCreateCoinInfo history = WalletOuterClass.RequestCreateCoinInfo.newBuilder()
//                .setCurrency(currencyEntity.getCurrency())
//                .setAddress(addressEntity.getAddress())
//                .build();
//
//        OkHttpUtil.getInstance().postEncrySelf(UriUtil.WALLET_V2_COINS_ADDRESS_DEFAULT, history, new ResultCall<Connect.HttpResponse>() {
//            @Override
//            public void onResponse(Connect.HttpResponse response) {
//                try {
//                    Connect.IMResponse imResponse = Connect.IMResponse.parseFrom(response.getBody().toByteArray());
//                    Connect.StructData structData = DecryptionUtil.decodeAESGCMStructData(imResponse.getCipherData());
//                    WalletOuterClass.RequestCreateCoinInfo createCoinInfo = WalletOuterClass.RequestCreateCoinInfo.parseFrom(structData.getPlainData());
//                    if (ProtoBufUtil.getInstance().checkProtoBuf(createCoinInfo)) {
//                        currencyEntity.setMasterAddress(addressEntity.getAddress());
//                        CurrencyHelper.getInstance().updateCurrency(currencyEntity);
//                    }
//                } catch (InvalidProtocolBufferException e) {
//                    e.printStackTrace();
//                }
//            }
//
//            @Override
//            public void onError(Connect.HttpResponse response) {
//
//            }
//        });
    }
}
