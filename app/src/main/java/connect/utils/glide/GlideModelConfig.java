package connect.utils.glide;

import android.content.Context;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.integration.okhttp3.OkHttpGlideModule;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.bitmap_recycle.LruBitmapPool;
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory;
import com.bumptech.glide.load.engine.cache.LruResourceCache;

/**
 * Glide Basic configuration
 */

public class GlideModelConfig extends OkHttpGlideModule {

    int diskSize = 1024 * 1024 * 100;//Disk cache space, if not set, the default is 100 * 1024 * 1024 100MB
    int memorySize = (int) (Runtime.getRuntime().maxMemory()) / 8; //Take 1/8 as the largest memory cache

    @Override
    public void applyOptions(Context context, GlideBuilder builder) {
        // Define cache size and location
        builder.setDiskCache(new InternalCacheDiskCacheFactory(context, diskSize));  //Mobile disk
        //builder.setDiskCache(new ExternalCacheDiskCacheFactory(context, "cache", diskSize)); //sdcard disk

        // The custom memory pool size and pictures
        builder.setMemoryCache(new LruResourceCache(memorySize));
        builder.setBitmapPool(new LruBitmapPool(memorySize));

        // Define the image format
        //builder.setDecodeFormat(DecodeFormat.PREFER_ARGB_8888);
        builder.setDecodeFormat(DecodeFormat.PREFER_RGB_565);
    }

    @Override
    public void registerComponents(Context context, Glide glide) {

    }

}
