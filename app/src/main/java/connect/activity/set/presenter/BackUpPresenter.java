package connect.activity.set.presenter;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;

import connect.ui.activity.R;
import connect.activity.login.bean.UserBean;
import connect.activity.set.contract.BackUpContract;
import connect.utils.BitmapUtil;
import connect.utils.system.SystemDataUtil;
import connect.utils.system.SystemUtil;
import connect.utils.ToastEUtil;
import protos.Connect;

/**
 * Created by Administrator on 2017/4/17 0017.
 */

public class BackUpPresenter implements BackUpContract.Presenter {

    private BackUpContract.View mView;
    private MediaScannerConnection scanner;
    private String pathDcim;
    public static String scanHead = "connect://";

    public BackUpPresenter(BackUpContract.View mView) {
        this.mView = mView;
    }

    @Override
    public void start() {
        scanner = new MediaScannerConnection(mView.getActivity(), new MediaScannerConnection.MediaScannerConnectionClient() {
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

    @Override
    public void saveBackup(Bitmap scanBitmap,UserBean userBean) {
        View viewBackUp = LayoutInflater.from(mView.getActivity()).inflate(R.layout.prikey_backup_photo,null);
        ((ImageView)viewBackUp.findViewById(R.id.scan_imag)).setImageBitmap(scanBitmap);
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
        textPaint.setColor(mView.getActivity().getResources().getColor(R.color.color_767a82));
        textPaint.setTextSize(30);
        textPaint.setAntiAlias(true);
        /*String text = getString(R.string.Set_Private_key_backup);
        canvas.drawText(getString(R.string.Set_Private_key_backup),
                width/2-15*text.length(),
                height/2 + SystemUtil.dipToPx(20),textPaint);*/
        int paddingLeft = SystemUtil.dipToPx(30);
        canvas.drawText(mView.getActivity().getString(R.string.Set_Name), paddingLeft, startH,textPaint);
        canvas.drawText(userBean.getName(), paddingLeft, startH + 40,textPaint);
        canvas.drawText(mView.getActivity().getString(R.string.Set_Id_address), paddingLeft, startH + 100,textPaint);
        canvas.drawText(userBean.getAddress(), paddingLeft, startH + 140,textPaint);
        canvas.drawText(mView.getActivity().getString(R.string.app_name_im), width*3/4 - SystemUtil.dipToPx(10), height - 80,textPaint);

        File file = BitmapUtil.getInstance().bitmapSavePathDCIM(bitmap);
        pathDcim = file.getAbsolutePath();
        try {
            MediaStore.Images.Media.insertImage(mView.getActivity().getContentResolver(), pathDcim, "", null);
            scanner.connect();
            ToastEUtil.makeText(mView.getActivity(),R.string.Login_Save_successful).show();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getEncryStr(UserBean userBean) {
        Connect.ExoprtPrivkeyQrcode.Builder builder = Connect.ExoprtPrivkeyQrcode.newBuilder();
        if(!TextUtils.isEmpty(userBean.getConnectId()))
            builder.setConnectId(userBean.getConnectId());
        if(!TextUtils.isEmpty(userBean.getPassHint()))
            builder.setPasswordHint(userBean.getPassHint());
        if(!TextUtils.isEmpty(userBean.getPhone())){
            String phone = userBean.getPhone();
            String subStr = phone.replace(phone.substring(2, phone.length()-4), "**");
            builder.setPhone(subStr);
        }
        Uri uri = Uri.parse(userBean.getAvatar());
        String avatarPath = uri.getPath().replace(".jpg","");
        String[] strArray = avatarPath.split("/");
        builder.setAvatar(strArray[strArray.length-1]);
        builder.setEncriptionPri(userBean.getTalkKey());
        builder.setUsername(userBean.getName());
        builder.setVersion(1);

        Connect.ExoprtPrivkeyQrcode privkeyQrcode = builder.build();
        byte[] byteArray = privkeyQrcode.toByteArray();
        String content = Base64.encodeToString(byteArray, Base64.DEFAULT);
        return scanHead + content.replace("\n","");
    }
}
