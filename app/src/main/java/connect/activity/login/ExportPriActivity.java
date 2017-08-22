package connect.activity.login;

import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.activity.home.HomeActivity;
import connect.database.MemoryDataManager;
import connect.database.SharedPreferenceUtil;
import connect.ui.activity.R;
import connect.activity.login.bean.UserBean;
import connect.activity.set.SafetyPatternActivity;
import connect.activity.base.BaseActivity;
import connect.utils.BitmapUtil;
import connect.utils.ToastEUtil;
import connect.widget.HeightEqWidthImage;
import connect.widget.TopToolBar;
import connect.widget.zxing.utils.CreateScan;

/**
 * After login to backup the private key.
 */
public class ExportPriActivity extends BaseActivity {
    @Bind(R.id.toolbar_top)
    TopToolBar toolbarTop;
    @Bind(R.id.backup_img)
    HeightEqWidthImage backupImg;
    @Bind(R.id.backup_private_key)
    TextView backupPrivateKey;
    @Bind(R.id.next_btn)
    Button nextBtn;

    private ExportPriActivity mActivity;
    private Bitmap bitmap;
    private MediaScannerConnection scanner;
    private String pathDcim;
    private View viewBackUp;
    private UserBean userBean;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_export_private);
        ButterKnife.bind(this);
        initView();
    }

    @Override
    public void initView() {
        mActivity = this;
        toolbarTop.setBlackStyle();
        toolbarTop.setTitle(null, R.string.Set_Backup_Private_Key);

        userBean = SharedPreferenceUtil.getInstance().getUser();
        userBean.setBack(true);
        SharedPreferenceUtil.getInstance().putUser(userBean);

        CreateScan createScan = new CreateScan();
        bitmap = createScan.generateQRCode(MemoryDataManager.getInstance().getPriKey(), getResources().getColor(R.color.color_ffffff));
        backupImg.setImageBitmap(bitmap);

        scanner = new MediaScannerConnection(mActivity, new MediaScannerConnection.MediaScannerConnectionClient() {
            @Override
            public void onMediaScannerConnected() {
                if (pathDcim != null) {
                    scanner.scanFile(pathDcim,"media/*");
                }
            }

            @Override
            public void onScanCompleted(String path, Uri uri) {
                scanner.disconnect();
            }
        });
    }

    @OnClick(R.id.next_btn)
    void goNext(View view) {
        SafetyPatternActivity.startActivity(mActivity,SafetyPatternActivity.LOGIN_TYPE);
    }

    @OnClick(R.id.backup_private_key)
    void gobackup(View view) {
        viewBackUp = LayoutInflater.from(mActivity).inflate(R.layout.prikey_backup_photo,null);
        ((ImageView)viewBackUp.findViewById(R.id.scan_imag)).setImageBitmap(bitmap);
        ((TextView)viewBackUp.findViewById(R.id.name_tv)).setText(userBean.getName());
        ((TextView)viewBackUp.findViewById(R.id.address_tv)).setText(userBean.getAddress());
        Bitmap bitmap = BitmapUtil.createViewBitmap(viewBackUp);

        File file = BitmapUtil.getInstance().bitmapSavePathDCIM(bitmap);
        pathDcim = file.getAbsolutePath();
        try {
            MediaStore.Images.Media.insertImage(getContentResolver(), pathDcim, "", null);
            scanner.connect();
            ToastEUtil.makeText(mActivity,R.string.Login_Save_successful).show();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            HomeActivity.startActivity(mActivity);
            mActivity.finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
