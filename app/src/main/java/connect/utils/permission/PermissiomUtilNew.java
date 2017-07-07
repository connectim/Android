package connect.utils.permission;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;

import connect.ui.activity.R;
import connect.utils.DialogUtil;

/**
 * Rights management tools
 * Created by Administrator on 2017/3/21.
 */

public class PermissiomUtilNew {

    private static PermissiomUtilNew instance;
    public static final int PERMISSIONS_REQUEST_CODE = 10;
    private final String PACKAGE_URL_SCHEME = "package:";
    private final String NOT_ALLOW = "NOT_ALLOW";
    /** Camera rights group */
    public static final String PERMISSIM_CAMERA = Manifest.permission.CAMERA;
    /** Recording rights group */
    public static final String PERMISSIM_RECORD_AUDIO = Manifest.permission.RECORD_AUDIO;
    /** Read and write external memory card authorization group */
    public static final String PERMISSIM_STORAGE = Manifest.permission.READ_EXTERNAL_STORAGE;
    /** Contact group */
    public static final String PERMISSIM_CONTACTS = Manifest.permission.READ_CONTACTS;
    /** Location permission group */
    public static final String PERMISSIM_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    /** SMS group */
    public static final String PERMISSIM_SMS = Manifest.permission.SEND_SMS;
    /** Mobile status rights group */
    public static final String PERMISSIM_PHONE = Manifest.permission.READ_PHONE_STATE;

    public static PermissiomUtilNew getInstance() {
        if (instance == null) {
            instance = new PermissiomUtilNew();
        }
        return instance;
    }

    /**
     * Request permission
     * @param activity
     * @param permissions
     * @param resultCallBack
     */
    public void requestPermissom(Activity activity,String[] permissions,ResultCallBack resultCallBack){
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            checkPermissiomLow(activity,permissions,resultCallBack);
            return;
        }

        ArrayList<String> permissinNotAllowed = new ArrayList<>();
        for(String name : permissions){
            if (ContextCompat.checkSelfPermission(activity, name) != PackageManager.PERMISSION_GRANTED){
                permissinNotAllowed.add(name);
            }
        }

        if(permissinNotAllowed.size() > 0){
            String [] string = new String [permissinNotAllowed.size()] ;
            permissinNotAllowed.toArray(string);
            ActivityCompat.requestPermissions(activity, string,PERMISSIONS_REQUEST_CODE);
        }else{
            resultCallBack.granted(permissions);
        }
    }

    /**
     * To deal with the callback returns the result
     * @param target
     * @param requestCode
     * @param permissions
     * @param grantResults
     * @param resultCallBack
     */
    public void onRequestPermissionsResult(Activity target, int requestCode, String[] permissions, int[] grantResults,ResultCallBack resultCallBack){
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE:
                if(verifyPermissions(grantResults)){
                    resultCallBack.granted(permissions);
                }else{
                    resultCallBack.deny(permissions);
                    if(permissions != null){
                        for(String name : permissions){
                            if (ContextCompat.checkSelfPermission(target, name) != PackageManager.PERMISSION_GRANTED){
                                showDialog(target,name);
                                return;
                            }
                        }

                    }
                    showDialog(target,NOT_ALLOW);
                }
                break;
            default:
                break;
        }
    }

    /**
     * whether permissions are allowed
     * @param grantResults
     * @return
     */
    private boolean verifyPermissions(int... grantResults) {
        if (grantResults == null || grantResults.length == 0) {
            return false;
        }
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /**
     * Tick is not a reminder to remind the listener
     * @param activity
     * @param permissions
     * @return
     */
    public String shouldShowRequestPermissionRationale(Activity activity, String... permissions) {
        for (String permission : permissions) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                return permission;
            }
        }
        return "";
    }


    /**
     * 6.0 before check special privileges (mainly for the third party system custom permissions manager)
     * @param activity
     * @param permissions
     * @param resultCallBack
     */
    private void checkPermissiomLow(Activity activity,String[] permissions,ResultCallBack resultCallBack){
        boolean isAllow = true;
        for(String name : permissions){
            switch (name){
                case Manifest.permission.CAMERA:
                    isAllow = checkCamera(activity);
                    break;
                case Manifest.permission.RECORD_AUDIO:
                    isAllow = chechVoice(activity);
                    break;
            }
            //If one item is refused to directly not through
            if(!isAllow){
                resultCallBack.deny(permissions);
                return;
            }
        }
        if(isAllow){
            resultCallBack.granted(permissions);
        }
    }

    /**
     * Version 6.0 to determine whether a camera available before (now mainly rely on the try... catch... catch exceptions)
     * @param activity
     * @return
     */
    private boolean checkCamera(Activity activity) {
        boolean canUse = true;
        Camera mCamera = null;
        try {
            mCamera = Camera.open();
            Camera.Parameters mParameters = mCamera.getParameters();
            mCamera.setParameters(mParameters);
        } catch (Exception e) {
            canUse = false;
        }
        if (mCamera != null) {
            mCamera.release();
        }
        if(!canUse){
            showDialog(activity,Manifest.permission.CAMERA);
        }
        return canUse;
    }

    /**
     * To determine whether the recording permission is available
     * @param activity
     * @return
     */
    private boolean chechVoice(Activity activity){
        boolean isAllow = CheckAudioPermission.isHasPermission();
        if(!isAllow){
            showDialog(activity,Manifest.permission.RECORD_AUDIO);
        }
        return isAllow;
    }

    /**
     * No permission prompt
     * @param activity
     * @param permissom
     */
    private void showDialog(final Activity activity, String permissom){
        String message = "";
        switch (permissom){
            case PERMISSIM_CAMERA:
                message = activity.getString(R.string.Link_Unable_to_get_the_camera_data);
                break;
            case PERMISSIM_RECORD_AUDIO:
                message = activity.getString(R.string.Link_Unable_to_get_the_voice_data);
                break;
            case PERMISSIM_STORAGE:
                message = activity.getString(R.string.Link_Unable_to_access_permissom_storage);
                break;
            case PERMISSIM_CONTACTS:
                message = activity.getString(R.string.Link_contact_loading_failed_check_the_contact_pression);
                break;
            case PERMISSIM_SMS:
                message = activity.getString(R.string.Link_Unable_to_get_the_sms_data);
                break;
            case NOT_ALLOW:
                message = activity.getString(R.string.Link_Unable_to_access_permissom);
                break;
        }
        DialogUtil.showAlertTextView(activity, activity.getString(R.string.Set_tip_title), message,
                "", activity.getString(R.string.Set_Setting), false, new DialogUtil.OnItemClickListener() {
                    @Override
                    public void confirm(String value) {
                        startSystemSettings(activity);
                    }

                    @Override
                    public void cancel() {

                    }
                }, false);
    }

    /**
     * Jump to permission settings page
     * @param activity
     */
    private void startSystemSettings(Activity activity) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse(PACKAGE_URL_SCHEME + activity.getPackageName()));
        activity.startActivity(intent);
    }

    /**
     * Authorization check result callback
     */
    public interface ResultCallBack{
        void granted(String[] permissions);

        void deny(String[] permissions);
    }
}
