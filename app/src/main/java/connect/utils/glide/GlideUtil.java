package connect.utils.glide;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import java.io.File;

import connect.ui.activity.R;
import connect.activity.base.BaseApplication;
import connect.utils.BitmapUtil;

/**
 * Glide Image loading tools
 */
public class GlideUtil {

    public static void loadAvatarRound(ImageView imageView, Object path){
        loadAvatarRound(imageView, path, 6);
    }

    public static void loadAvatarRound(ImageView imageView, Object path, int dpRound){
        if (dpRound <= 0) {
            loadImage(imageView,path,R.mipmap.default_user_avatar);
        } else {
            loadImage(imageView, path, R.mipmap.default_user_avatar, new GlideRoundTransform(BaseApplication.getInstance().getBaseContext(), dpRound));
        }
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
                .into(imageView);
    }

    public static void loadImage(ImageView imageView, Object path, int errorId, BitmapTransformation... transformations){
        Glide.with(BaseApplication.getInstance())
                .load(path)
                .transform(transformations)
                .error(errorId)
                .into(imageView);
    }

    /**
     * Download the pictures
     */
    public static void downloadImage(String path, final OnDownloadTarget listeners){
        Glide.with(BaseApplication.getInstance())
                .load(path)
                .asBitmap()
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                        File file=BitmapUtil.getInstance().bitmapSavePath(resource);
                        String pathLocal = file.getAbsolutePath();
                        listeners.finish(pathLocal);
                    }

                    @Override
                    public void onLoadFailed(Exception e, Drawable errorDrawable) {
                        super.onLoadFailed(e, errorDrawable);
                        listeners.error();
                    }
                });
    }

    public interface OnDownloadTarget{
        void finish(String path);

        void error();
    }

}
