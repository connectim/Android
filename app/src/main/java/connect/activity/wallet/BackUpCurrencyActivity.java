package connect.activity.wallet;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.activity.base.BaseActivity;
import connect.activity.login.bean.UserBean;
import connect.activity.set.contract.BackUpContract;
import connect.activity.set.presenter.BackUpPresenter;
import connect.activity.wallet.bean.CurrencyBean;
import connect.activity.wallet.bean.WalletBean;
import connect.database.SharePreferenceUser;
import connect.database.SharedPreferenceUtil;
import connect.database.green.DaoHelper.CurrencyHelper;
import connect.database.green.bean.CurrencyEntity;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.DialogUtil;
import connect.widget.TopToolBar;
import connect.widget.zxing.utils.CreateScan;

public class BackUpCurrencyActivity extends BaseActivity implements BackUpContract.View{

    @Bind(R.id.toolbar_top)
    TopToolBar toolbarTop;
    @Bind(R.id.backup_img)
    ImageView backupImg;
    @Bind(R.id.status_tv)
    TextView statusTv;
    @Bind(R.id.describe_tv)
    TextView describeTv;
    @Bind(R.id.save_tv)
    TextView saveTv;

    private BackUpCurrencyActivity mActivity;
    private BackUpContract.Presenter presenter;
    private UserBean userBean;
    private Bitmap bitmap;

    private CurrencyBean currencyBean;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_prikey_backup);
        ButterKnife.bind(this);
        initView();
    }

    public static void startActivity(Activity activity, CurrencyBean currencyBean) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("Currency", currencyBean);
        ActivityUtil.next(activity, BackUpCurrencyActivity.class, bundle);
    }

    @Override
    public void initView() {
        mActivity = this;
        toolbarTop.setBlackStyle();
        toolbarTop.setLeftImg(R.mipmap.back_white);
        toolbarTop.setTitle(null, R.string.Set_Export_Private_Key);
        toolbarTop.setRightImg(R.mipmap.menu_white);
        userBean = SharedPreferenceUtil.getInstance().getUser();

        currencyBean= (CurrencyBean) getIntent().getSerializableExtra("Currency");
        new BackUpPresenter(this).start();
        switchPriKey(1);
    }

    @OnClick(R.id.left_img)
    void goback(View view){
        ActivityUtil.goBack(mActivity);
    }

    @OnClick(R.id.save_tv)
    void saveBackup(View view){
        presenter.saveBackup(bitmap,userBean);
    }

    @OnClick(R.id.right_lin)
    void otherLoginClick(View view) {
        ArrayList<String> list = new ArrayList<>();
        list.add(mActivity.getResources().getString(R.string.Login_Encrypted_private_key));
        list.add(mActivity.getResources().getString(R.string.Login_Decrypted_private_key));
        DialogUtil.showBottomListView(mActivity,list,new DialogUtil.DialogListItemClickListener(){
            @Override
            public void confirm(AdapterView<?> parent, View view, int position) {
                switch (position) {
                    case 0:
                        switchPriKey(2);
                        break;
                    case 1:
                        switchPriKey(1);
                        break;
                    default:
                        break;
                }
            }
        });
    }

    @Override
    public void setPresenter(BackUpContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public Activity getActivity() {
        return mActivity;
    }

    /**
     * change state
     * @param type 1: unencrypted 2：encrypted
     */
    private void switchPriKey(int type){
        CreateScan createScan = new CreateScan();
        switch (type) {
            case 1:
                bitmap = createScan.generateQRCode(currencyName(currencyBean), getResources().getColor(R.color.color_f0f0f6));
                backupImg.setImageBitmap(bitmap);
                statusTv.setText(R.string.Login_Decrypted_private_key);
                saveTv.setText(R.string.Set_Backup_Private_Key);
                describeTv.setText(R.string.Login_export_prikey_explain);
                break;
            case 2:
                bitmap = createScan.generateQRCode(encryCurrencyName(), getResources().getColor(R.color.color_f0f0f6));
                backupImg.setImageBitmap(bitmap);
                statusTv.setText(R.string.Login_Encrypted_private_key);
                saveTv.setText(R.string.Set_Backup_encrypted_private_key);
                describeTv.setText(R.string.Login_export_encrypted_prikey_explain);
                break;
            default:
                break;
        }
    }

    public String currencyName(CurrencyBean bean) {
        String name = "";
        switch (bean) {
            case BTC:
                name = "BTC";
                CurrencyEntity currencyEntity = CurrencyHelper.getInstance().loadCurrency(name);
                if (currencyEntity == null) {
                    break;
                }
                name = name + ":" + currencyEntity.getCurrency();
                break;
        }
        return name;
    }

    public String encryCurrencyName() {
        StringBuffer stringBuffer = new StringBuffer();
        WalletBean walletBean = SharePreferenceUser.getInstance().getWalletInfo();
        stringBuffer.append("Payload:" + walletBean.getPayload());
        stringBuffer.append(",");
        stringBuffer.append("Salt:" + walletBean.getSalt());
        stringBuffer.append(",");
        stringBuffer.append("N:" + walletBean.getN());
        stringBuffer.append(";");

        List<CurrencyEntity> currencyList = CurrencyHelper.getInstance().loadCurrencyList();
        if (currencyList != null && currencyList.size() > 0) {
            for (CurrencyEntity entity : currencyList) {
                stringBuffer.append(entity.getCurrency() + ":");
                stringBuffer.append(entity.getCurrency());
                stringBuffer.append(";");
            }
        }
        return stringBuffer.toString();
    }
}
