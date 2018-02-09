package connect.service;

import android.app.DownloadManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.webkit.MimeTypeMap;

import java.io.File;

import connect.ui.activity.R;
import connect.utils.FileUtil;

/**
 * upload update
 */
public class UpdateAppService extends Service {

    DownloadManager manager;
    DownloadCompleteReceiver receiver;
    File file;
    private String pathDown = "iWork/download/iWork.apk";

    private void initDownManager(String downLoadUrl) {
        manager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        receiver = new DownloadCompleteReceiver();
        DownloadManager.Request down = new DownloadManager.Request(Uri.parse(downLoadUrl));
        down.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE
                | DownloadManager.Request.NETWORK_WIFI);
        down.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
        down.setVisibleInDownloadsUi(true);
        down.setTitle(getString(R.string.app_name));
        down.setDescription(getString(R.string.Common_Download_App,"iWork"));
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
            //在下载过程中通知栏会一直显示该下载的Notification
            //在下载完成后该Notification会继续显示，直到用户点击该Notification或者消除该Notification
            down.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        }

        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            // 解决没有内存卡的手机升级
            down.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "iWork.apk");
        }else{
            FileUtil.deleteFile(Environment.getExternalStorageDirectory() + pathDown);
            file = new File(Environment.getExternalStorageDirectory() + pathDown);
            down.setDestinationUri(Uri.fromFile(file));
        }
        manager.enqueue(down);
        registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(null != intent){
            String downLoadUrl = intent.getStringExtra("downLoadUrl");
            initDownManager(downLoadUrl);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        if (receiver != null)
            unregisterReceiver(receiver);
        super.onDestroy();
    }

    class DownloadCompleteReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {


            /*if (intent.getAction().equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
                installAPK(Uri.fromFile(file),context);
                UpdateAppService.this.stopSelf();
            }*/
            /*if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
                long completeId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
                DownloadManager dManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
                Intent install = new Intent(Intent.ACTION_VIEW);
                Uri downloadFileUri = dManager.getUriForDownloadedFile(completeId);
                if (downloadFileUri != null) {
                    install.setDataAndType(downloadFileUri, "application/vnd.android.package-archive");
                    install.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(install);
                }
            }else{
                if (intent.getAction().equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
                    installAPK(Uri.fromFile(file),context);
                    UpdateAppService.this.stopSelf();
                }
            }*/
        }

        private void installAPK(Uri apk,Context context) {
            if(Build.VERSION.SDK_INT < 23) {
                Intent intents = new Intent();
                intents.setAction("android.intent.action.VIEW");
                intents.addCategory("android.intent.category.DEFAULT");
                intents.setType("application/vnd.android.package-archive");
                intents.setData(apk);
                intents.setDataAndType(apk, "application/vnd.android.package-archive");
                intents.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intents);
                android.os.Process.killProcess(android.os.Process.myPid());
            }else{
                //7.0 FileProvider
                if(file.exists()){
                    openFile(file,context);
                }
            }
        }

        public void openFile(File file, Context context) {
            Intent intent = new Intent();
            // intent.addFlags(268435456);  268435456 --> 0x10000000
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setAction("android.intent.action.VIEW");
            String type = getMIMEType(file);
            intent.setDataAndType(Uri.fromFile(file), type);
            try {
                context.startActivity(intent);
            } catch (Exception var5) {
                var5.printStackTrace();
            }
        }

        public String getMIMEType(File var0) {
            String var1 = "";
            String var2 = var0.getName();
            String var3 = var2.substring(var2.lastIndexOf(".") + 1, var2.length()).toLowerCase();
            var1 = MimeTypeMap.getSingleton().getMimeTypeFromExtension(var3);
            return var1;
        }
    }

}