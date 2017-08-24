package connect.activity.set.presenter;

import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.provider.MediaStore;
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
import connect.activity.set.contract.SafetyBackupContract;
import connect.utils.BitmapUtil;
import connect.utils.ToastEUtil;
import protos.Connect;

public class SafetyBackupPresenter implements SafetyBackupContract.Presenter {

    private SafetyBackupContract.View mView;
    private MediaScannerConnection scanner;
    private String pathDcim;
    public static String scanHead = "connect://";

    public SafetyBackupPresenter(SafetyBackupContract.View mView) {
        this.mView = mView;
        mView.setPresenter(this);
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

    /**
     * Save the qr code to the local
     *
     * @param scanBitmap qr code bitmap
     * @param userBean Need to encrypt the user information
     */
    @Override
    public void saveBackup(Bitmap scanBitmap,UserBean userBean) {
        View viewBackUp = LayoutInflater.from(mView.getActivity()).inflate(R.layout.prikey_backup_photo,null);
        ((ImageView)viewBackUp.findViewById(R.id.scan_imag)).setImageBitmap(scanBitmap);
        ((TextView)viewBackUp.findViewById(R.id.name_tv)).setText(userBean.getName());
        ((TextView)viewBackUp.findViewById(R.id.address_tv)).setText(userBean.getAddress());
        Bitmap bitmap = BitmapUtil.createViewBitmap(viewBackUp);

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

    /**
     * Access to the encrypted private key
     *
     * @param userBean Need to encrypt the user information
     * @return encrypted private key
     */
    @Override
    public String getEncryStr(UserBean userBean) {
        Connect.ExoprtPrivkeyQrcode.Builder builder = Connect.ExoprtPrivkeyQrcode.newBuilder();
        if (!TextUtils.isEmpty(userBean.getConnectId())) {
            builder.setConnectId(userBean.getConnectId());
        }
        if (!TextUtils.isEmpty(userBean.getPassHint())) {
            builder.setPasswordHint(userBean.getPassHint());
        }
        if (!TextUtils.isEmpty(userBean.getPhone())) {
            String phone = userBean.getPhone();
            String subStr = phone.replace(phone.substring(2, phone.length() - 4), "**");
            builder.setPhone(subStr);
        }
        Uri uri = Uri.parse(userBean.getAvatar());
        String avatarPath = uri.getPath().replace(".jpg","");
        String[] strArray = avatarPath.split("/");
        builder.setAvatar(strArray[strArray.length - 1]);
        builder.setEncriptionPri(userBean.getTalkKey());
        builder.setUsername(userBean.getName());
        builder.setVersion(1);

        Connect.ExoprtPrivkeyQrcode privkeyQrcode = builder.build();
        byte[] byteArray = privkeyQrcode.toByteArray();
        String content = Base64.encodeToString(byteArray, Base64.DEFAULT);
        return scanHead + content.replace("\n","");
    }

}
