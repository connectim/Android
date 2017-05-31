package connect.utils.glide;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import connect.ui.activity.R;
import connect.ui.base.BaseApplication;
import connect.utils.BitmapUtil;

/**
 * Glide Image loading tools
 */
public class GlideUtil {

    public static void loadAvater(ImageView imageView, Object path){
        loadImage(imageView,path,R.mipmap.default_user_avatar);
    }

    public static void loadImageAssets(ImageView imageView, Object path){
        loadImage(imageView,"file:///android_asset/" + path);
    }

    public static void loadImage(ImageView imageView, Object path){
        loadImage(imageView,path,R.mipmap.img_default);
    }

    public static void loadImage(ImageView imageView, Object path, int errorId){
        Glide.with(BaseApplication.getInstance())
                .load(path)
                .error(errorId)
                //.placeholder(errorId)
                //.thumbnail(0.2f)
                .into(imageView);
    }

    public static void loadImage(ImageView imageView, Object path,BitmapTransformation... transformations){
        Glide.with(BaseApplication.getInstance())
                .load(path)
                .transform(transformations)
                //.placeholder(R.mipmap.img_default)
                .error(R.mipmap.img_default)
                //.thumbnail(0.2f)
                .into(imageView);
    }

    /**
     * Download the pictures
     * @param path
     * @param listeners
     */
    public static void downloadImage(String path, final OnDownloadTarget listeners){
        Glide.with(BaseApplication.getInstance())
                .load(path)
                .asBitmap()
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                        String pathLocal = BitmapUtil.bitmapSavePath(resource,null,100);
                        listeners.finish(pathLocal);
                    }

                    @Override
                    public void onLoadFailed(Exception e, Drawable errorDrawable) {
                        super.onLoadFailed(e, errorDrawable);
                        listeners.error();
                    }
                });
    }

}
