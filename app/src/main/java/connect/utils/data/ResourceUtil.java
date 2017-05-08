package connect.utils.data;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;

import connect.ui.base.BaseApplication;

/**
 * system Resource drawable color text
 * Created by gtq on 2016/11/23.
 */
public class ResourceUtil {

    public static Drawable getDrawable(Context context, int drawableId) {
        return getDrawable(context, drawableId, null);
    }

    public static Drawable getDrawable(Context context, int drawableId, Resources.Theme theme) {
        Resources resources = context.getResources();
        Class<?> resourcesClass = resources.getClass();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            try {
                Method getDrawableMethod = resourcesClass.getMethod("getDrawable", int.class, Resources.Theme.class);
                getDrawableMethod.setAccessible(true);
                return (Drawable) getDrawableMethod.invoke(resources, drawableId, theme);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        else
            try {
                Method getDrawableMethod = resourcesClass.getMethod("getDrawable", int.class);
                getDrawableMethod.setAccessible(true);
                return (Drawable) getDrawableMethod.invoke(resources, drawableId);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        return null;
    }

    public static int getColor(Context context, int colorId) {
        return getColor(context, colorId, null);
    }

    public static int getColor(Context context, int colorId, Resources.Theme theme) {
        Resources resources = context.getResources();
        Class<?> resourcesClass = resources.getClass();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            try {
                Method getColorMethod = resourcesClass.getMethod("getColor", int.class, Resources.Theme.class);
                getColorMethod.setAccessible(true);
                return (Integer) getColorMethod.invoke(resources, colorId, theme);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        else
            try {
                Method getColorMethod = resourcesClass.getMethod("getColor", int.class);
                getColorMethod.setAccessible(true);
                return (Integer) getColorMethod.invoke(resources, colorId);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        return Color.BLACK;
    }

    public static void setBackground(View view, int drawableId) {
        setBackground(view, getDrawable(view.getContext(), drawableId));
    }

    public static void setBackground(View view, Drawable background) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            setBackground("setBackground", view, background);
        else
            setBackground("setBackgroundDrawable", view, background);
    }

    public static void setBackground(String method, View view, Drawable background) {
        try {
            Method viewMethod = view.getClass().getMethod(method, Drawable.class);
            viewMethod.setAccessible(true);
            viewMethod.invoke(view, background);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static void setTextAppearance(TextView view, int textAppearance) {
        Class<?> resourcesClass = view.getClass();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            try {
                Method getColorMethod = resourcesClass.getMethod("setTextAppearance", Context.class, int.class);
                getColorMethod.setAccessible(true);
                getColorMethod.invoke(view, view.getContext(), textAppearance);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        else
            try {
                Method getColorMethod = resourcesClass.getMethod("setTextAppearance", int.class);
                getColorMethod.setAccessible(true);
                getColorMethod.invoke(view, textAppearance);
            } catch (Throwable e) {
                e.printStackTrace();
            }
    }

    /**
     * Res id
     */
    public static int getResId(String name) {
        Context context = BaseApplication.getInstance().getBaseContext();
        Resources resources = context.getResources();
        return resources.getIdentifier(name, "mipmap", context.getPackageName());
    }

    public static Drawable getEmotDrawable(String text) {
        Context context = BaseApplication.getInstance().getBaseContext();
        Drawable drawable = new BitmapDrawable(context.getResources(), loadAssetBitmap(text));
        // scale
        if (drawable != null) {
            int width = (int) (drawable.getIntrinsicWidth() * 0.45F);
            int height = (int) (drawable.getIntrinsicHeight() * 0.45F);
            drawable.setBounds(0, 0, width, height);
        }

        return drawable;
    }

    /**
     * assets image
     * @param assetPath
     * @return
     */
    public static Bitmap loadAssetBitmap(String assetPath) {
        InputStream is = null;
        try {
            Context context = BaseApplication.getInstance().getBaseContext();
            Resources resources = context.getResources();
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inDensity = DisplayMetrics.DENSITY_HIGH;
            options.inScreenDensity = resources.getDisplayMetrics().densityDpi;
            options.inTargetDensity = resources.getDisplayMetrics().densityDpi;
            is = context.getAssets().open(assetPath);
            Bitmap bitmap = BitmapFactory.decodeStream(is, new Rect(), options);
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * get Assets inputstream
     * @param filepath
     * @return
     */
    public static InputStream getAssetsStream(String filepath) {
        Context context = BaseApplication.getInstance().getBaseContext();
        try {
            return context.getAssets().open(filepath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
