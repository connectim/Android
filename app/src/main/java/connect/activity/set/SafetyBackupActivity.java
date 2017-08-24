package connect.activity.set;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.database.MemoryDataManager;
import connect.database.SharedPreferenceUtil;
import connect.ui.activity.R;
import connect.activity.login.bean.UserBean;
import connect.activity.set.contract.SafetyBackupContract;
import connect.activity.set.presenter.SafetyBackupPresenter;
import connect.activity.base.BaseActivity;
import connect.utils.ActivityUtil;
import connect.utils.DialogUtil;
import connect.widget.TopToolBar;
import connect.widget.zxing.utils.CreateScan;

/**
 * The private key backup
 */
public class SafetyBackupActivity extends BaseActivity implements SafetyBackupContract.View {

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

    private SafetyBackupActivity mActivity;
    private SafetyBackupContract.Presenter presenter;
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
        new SafetyBackupPresenter(this).start();
        switchPriKey(2);
    }

    @OnClick(R.id.left_img)
    void goBack(View view) {
        ActivityUtil.goBack(mActivity);
    }

    @OnClick(R.id.save_tv)
    void saveBackup(View view) {
        presenter.saveBackup(bitmap,userBean);
    }

    @OnClick(R.id.right_lin)
    void otherLoginClick(View view) {
        ArrayList<String> list = new ArrayList<>();
        list.add(mActivity.getResources().getString(R.string.Login_Encrypted_private_key));
        list.add(mActivity.getResources().getString(R.string.Login_Decrypted_private_key));
        DialogUtil.showBottomView(mActivity,list,new DialogUtil.DialogListItemClickListener() {
            @Override
            public void confirm(int position) {
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
    public void setPresenter(SafetyBackupContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public Activity getActivity() {
        return mActivity;
    }

    /**
     * Conversion between the private key and encryption private key.
     *
     * @param type 1: unencrypted 2：encrypted
     */
    private void switchPriKey(int type) {
        CreateScan createScan = new CreateScan();
        switch (type) {
            case 1:
                bitmap = createScan.generateQRCode(MemoryDataManager.getInstance().getPriKey(), getResources().getColor(R.color.color_f0f0f6));
                backupImg.setImageBitmap(bitmap);
                statusTv.setText(R.string.Login_Decrypted_private_key);
                saveTv.setText(R.string.Set_Backup_Private_Key);
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
