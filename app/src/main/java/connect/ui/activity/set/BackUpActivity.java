package connect.ui.activity.set;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.db.SharedPreferenceUtil;
import connect.ui.activity.R;
import connect.ui.activity.login.bean.UserBean;
import connect.ui.activity.set.contract.BackUpContract;
import connect.ui.activity.set.presenter.BackUpPresenter;
import connect.ui.base.BaseActivity;
import connect.utils.ActivityUtil;
import connect.utils.DialogUtil;
import connect.view.TopToolBar;
import connect.view.zxing.utils.CreateScan;

/**
 * The private key backup
 * Created by Administrator on 2016/12/6.
 */
public class BackUpActivity extends BaseActivity implements BackUpContract.View{

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

    private BackUpActivity mActivity;
    private BackUpContract.Presenter presenter;
    private UserBean userBean;
    private Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_prikey_backup);
        ButterKnife.bind(this);
        initView();
    }

    @Override
    public void initView() {
        mActivity = this;
        toolbarTop.setBlackStyle();
        toolbarTop.setLeftImg(R.mipmap.back_white);
        toolbarTop.setTitle(null, R.string.Set_Export_Private_Key);
        toolbarTop.setRightImg(R.mipmap.menu_white);
        userBean = SharedPreferenceUtil.getInstance().getUser();
        setPresenter(new BackUpPresenter(this));
        switchPriKey(1);
    }

    @Override
    public void setPresenter(BackUpContract.Presenter presenter) {
        this.presenter = presenter;
        presenter.start();
    }

    @Override
    public Activity getActivity() {
        return mActivity;
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

    /**
     * change state
     * @param type 1: unencrypted 2：encrypted
     */
    private void switchPriKey(int type){
        CreateScan createScan = new CreateScan();
        switch (type){
            case 1:
                bitmap = createScan.generateQRCode(userBean.getPriKey(), getResources().getColor(R.color.color_f0f0f6));
                backupImg.setImageBitmap(bitmap);
                statusTv.setText(R.string.Login_Decrypted_private_key);
                saveTv.setText(R.string.Set_Backup_decrypted_private_key);
                describeTv.setText(R.string.Login_export_prikey_explain);
                break;
            case 2:
                bitmap = createScan.generateQRCode(presenter.getEncryStr(userBean),getResources().getColor(R.color.color_f0f0f6));
                backupImg.setImageBitmap(bitmap);
                statusTv.setText(R.string.Login_Encrypted_private_key);
                saveTv.setText(R.string.Set_Backup_encrypted_private_key);
                describeTv.setText(R.string.Login_export_encrypted_prikey_explain);
                break;
            default:
                break;
        }
    }

}
