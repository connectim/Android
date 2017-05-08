package connect.ui.activity.login;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextPaint;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.FileNotFoundException;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.db.SharedPreferenceUtil;
import connect.ui.activity.R;
import connect.ui.activity.login.bean.UserBean;
import connect.ui.activity.set.PatternActivity;
import connect.ui.base.BaseActivity;
import connect.utils.BitmapUtil;
import connect.utils.system.SystemDataUtil;
import connect.utils.system.SystemUtil;
import connect.utils.ToastEUtil;
import connect.view.HightEqWidthImage;
import connect.view.TopToolBar;
import connect.view.zxing.utils.CreateScan;

/**
 * After login to backup the private key
 * Created by Administrator on 2017/3/7.
 */

public class ExportPriActivity extends BaseActivity {
    @Bind(R.id.toolbar_top)
    TopToolBar toolbarTop;
    @Bind(R.id.backup_img)
    HightEqWidthImage backupImg;
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
        SharedPreferenceUtil.getInstance().updataUser(userBean);

        CreateScan createScan = new CreateScan();
        bitmap = createScan.generateQRCode(userBean.getPriKey(), getResources().getColor(R.color.color_ffffff));
        backupImg.setImageBitmap(bitmap);

        scanner = new MediaScannerConnection(mActivity, new MediaScannerConnection.MediaScannerConnectionClient() {
            @Override
            public void onMediaScannerConnected() {
                if(pathDcim != null){
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
    void goNext(View view){
        PatternActivity.startActivity(mActivity,PatternActivity.LOGIN_STYPE);
    }

    @OnClick(R.id.backup_private_key)
    void gobackup(View view){
        viewBackUp = LayoutInflater.from(mActivity).inflate(R.layout.prikey_backup_photo,null);
        ((ImageView)viewBackUp.findViewById(R.id.scan_imag)).setImageBitmap(bitmap);
        ((TextView)viewBackUp.findViewById(R.id.name_tv)).setText(userBean.getName());
        ((TextView)viewBackUp.findViewById(R.id.address_tv)).setText(userBean.getAddress());
        viewBackUp.measure(View.MeasureSpec.makeMeasureSpec(256, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(256, View.MeasureSpec.EXACTLY));
        viewBackUp.layout(0, 0, SystemDataUtil.getScreenWidth(), SystemDataUtil.getScreenHeight());

        Bitmap bitmap = Bitmap.createBitmap(SystemDataUtil.getScreenWidth(), SystemDataUtil.getScreenHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        viewBackUp.draw(canvas);

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int startH = height*2/3;
        TextPaint textPaint = new TextPaint();
        textPaint.setColor(getResources().getColor(R.color.color_767a82));
        textPaint.setTextSize(30);
        textPaint.setAntiAlias(true);

        int paddingLeft = SystemUtil.dipToPx(30);
        canvas.drawText(getString(R.string.Set_Name), paddingLeft, startH,textPaint);
        canvas.drawText(userBean.getName(), paddingLeft, startH + 40,textPaint);
        canvas.drawText(getString(R.string.Set_Id_address), paddingLeft, startH + 100,textPaint);
        canvas.drawText(userBean.getAddress(), paddingLeft, startH + 140,textPaint);
        canvas.drawText(getString(R.string.app_name_im), width*3/4 - SystemUtil.dipToPx(10), height - 80,textPaint);

        saveImageToGallery(bitmap);
    }

    public void saveImageToGallery(Bitmap bmp) {
        pathDcim = BitmapUtil.bitmapSavePathDCIM(bmp);
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
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
