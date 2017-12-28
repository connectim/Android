package connect.utils.system;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.DisplayMetrics;

import java.util.ArrayList;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import connect.activity.base.BaseApplication;
import connect.activity.contact.bean.PhoneContactBean;
import connect.utils.GlobalLanguageUtil;
import connect.utils.StringUtil;
import connect.utils.log.LogManager;

public class SystemDataUtil {

    /**
     * Get the APP version Name
     *
     * @param context
     * @return
     */
    public static String getVersionName(Context context) {
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info;
            info = manager.getPackageInfo(context.getPackageName(), 0);
            return info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            LogManager.getLogger().e("Exception", e.toString());
        }
        return null;
    }

    /**
     * screen width
     *
     * @return
     */
    public static int getScreenWidth() {
        Context context = BaseApplication.getInstance().getBaseContext();
        return context.getResources().getDisplayMetrics().widthPixels;
    }

    /**
     * screen height
     *
     * @return
     */
    public static int getScreenHeight() {
        Context context = BaseApplication.getInstance().getBaseContext();
        return context.getResources().getDisplayMetrics().heightPixels;
    }

    /**
     * android device id
     *
     * @return
     */
    public static String getDeviceId() {
        String deviceId = null;
        Context context = BaseApplication.getInstance().getBaseContext();
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
            serial = android.os.Build.class.getField("SERIAL").get(null).toString();
        } catch (Exception exception) {
            serial = "serial";
        }
        serial = new UUID(deviceID.hashCode(), serial.hashCode()).toString();
        return StringUtil.cdHash256(serial);
    }

    /**
     * Gets the current national code
     */
    public static String getCountry() {
        Locale locale = Locale.getDefault();
        return locale.getCountry();
    }

    /**
     * Gets the current language
     */
    public static String getDeviceLanguage() {
        Locale locale = Locale.getDefault();
        return locale.getLanguage();
    }

    /**
     * Gets the current national currency code
     */
    public static String getCountryCode() {
        Locale locale = Locale.getDefault();
        Currency currency;
        try {
            currency = Currency.getInstance(locale);
        } catch (Exception e) {
            // IllegalArgumentException:Unsupported ISO 3166 country: en
            return "";
        }
        return currency.getCurrencyCode();
    }

    /**
     * Set up the language
     */
    public static void setAppLanguage(Context context, String languageCode) {
        Locale myLocale;
        if (TextUtils.isEmpty(languageCode)) {
            myLocale = Locale.getDefault();
        } else if (languageCode.equals("zh")) {
            myLocale = Locale.SIMPLIFIED_CHINESE;
        } else if (languageCode.equals("ru")) {
            myLocale = new Locale("ru", "RU");
        } else {
            myLocale = Locale.ENGLISH;
        }
        Resources resources = context.getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        Configuration config = resources.getConfiguration();
        // user select language
        config.locale = myLocale;
        resources.updateConfiguration(config, dm);

        //Switch the notification bar expression language
        GlobalLanguageUtil.getInstance().transLanguage();
    }

    /**
     * getAddressContacts Access to the phone address book contacts (optimize query speed)
     *
     * @return Object
     * @Exception
     */
    public static List<PhoneContactBean> getLocalAddressBook(Context context) {
        Map<Integer, String> map = new HashMap<>();
        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        String contactIds = "";
        int i = 0;
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String contactId = cursor.getString(cursor.getColumnIndex("_id"));
                map.put(cursor.getInt(cursor.getColumnIndex("_id")), cursor.getString(cursor.getColumnIndex("display_name")));
                if (cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                    if (i != 0) {
                        contactIds += ",";
                    }
                    contactIds += contactId;
                    i++;
                }
            }
            cursor.close();
        }
        Cursor phonesCursor = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " in ( " + contactIds + ")", null, null);
        i = 0;
        List<PhoneContactBean> loacList = new ArrayList<>();
        List<String> tempList = new ArrayList<>();
        PhoneContactBean contacts = null;
        int nowContact = 0;
        if (phonesCursor != null) {
            if (phonesCursor.moveToFirst()) {
                do {
                    String phoneNumber = phonesCursor.getString(phonesCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    if (phoneNumber != null) {
                        phoneNumber = phoneNumber.replaceAll(" ", "");
                    }
                    nowContact = phonesCursor.getInt(phonesCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));
                    contacts = new PhoneContactBean();
                    contacts.setId(nowContact + "");
                    contacts.setPhone(phoneNumber);
                    contacts.setName(map.get(nowContact));
                    loacList.add(contacts);
                    tempList.add(phoneNumber);
                    i++;
                } while (phonesCursor.moveToNext());
            }
            phonesCursor.close();
        }

        //Get Sim card
        ContentResolver cr = context.getContentResolver();
        final String SIM_URI_ADN = "content://icc/adn";// SIM card
        Uri uri = Uri.parse(SIM_URI_ADN);
        cursor = cr.query(uri, null, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                contacts = new PhoneContactBean();
                contacts.setName(cursor.getString(cursor.getColumnIndex("name")));
                contacts.setPhone(cursor.getString(cursor
                        .getColumnIndex("number")));
                if (!tempList.contains(contacts.getPhone())) {
                    loacList.add(contacts);
                }
            }
            cursor.close();
        }
        return loacList;
    }
}
