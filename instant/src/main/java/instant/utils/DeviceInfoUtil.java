package instant.utils;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;

import java.util.Locale;
import java.util.UUID;

import instant.ui.InstantSdk;

/**
 * Created by puin on 17-10-8.
 */

public class DeviceInfoUtil {

    /**
     * android device id
     *
     * @return
     */
    public static String getDeviceId() {
        String deviceId = null;
        Context context = InstantSdk.getInstance().getBaseContext();
        try {
            deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        } catch (Exception e) {
            e.printStackTrace();
            deviceId = "";
        }
        return StringUtil.cdHash256(deviceId);
    }

    /**
     * Pseudo-Unique ID
     *
     * @return
     */
    public static String getLocalUid() {
        String serial = null;
        String deviceID = "35" +
                Build.BOARD.length() % 10 + Build.BRAND.length() % 10 +
                Build.CPU_ABI.length() % 10 + Build.DEVICE.length() % 10 +
                Build.DISPLAY.length() % 10 + Build.HOST.length() % 10 +
                Build.ID.length() % 10 + Build.MANUFACTURER.length() % 10 +
                Build.MODEL.length() % 10 + Build.PRODUCT.length() % 10 +
                Build.TAGS.length() % 10 + Build.TYPE.length() % 10 +
                Build.USER.length() % 10;

        try {
            serial = Build.class.getField("SERIAL").get(null).toString();
        } catch (Exception exception) {
            serial = "serial";
        }
        serial = new UUID(deviceID.hashCode(), serial.hashCode()).toString();
        return StringUtil.cdHash256(serial);
    }

    /**
     * Gets the current language
     */
    public static String getDeviceLanguage() {
        Locale locale = Locale.getDefault();
        return locale.getLanguage();
    }
}
