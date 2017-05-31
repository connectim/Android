package connect.ui.activity.chat.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.NinePatch;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import connect.ui.activity.R;
import connect.ui.activity.chat.bean.MsgDirect;
import connect.ui.activity.chat.inter.FileDownLoad;
import connect.ui.base.BaseApplication;
import connect.utils.FileUtil;
import connect.utils.glide.GlideUtil;
import connect.utils.glide.MaskTransformation;
import connect.utils.system.SystemUtil;
import connect.utils.glide.BlurTransformation;

public class BubbleImg extends RelativeLayout {

    private Context context;

    private boolean openBurn = false;

    private MsgDirect msgDirect;
    private ProgressBar progressBar;
    private ImageView imageView;


    private BubbleType bubbleType;

    private enum BubbleType {
        IMG,
        VIDEO,
        LOCATION
    }

    public BubbleImg(Context context) {
        super(context);
        initView();
    }

    public BubbleImg(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public BubbleImg(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView();
    }

    public void initView() {
        context = getContext();

        RelativeLayout.LayoutParams proParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        proParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        progressBar = new ProgressBar(getContext());
        progressBar.setLayoutParams(proParams);
        addView(progressBar, proParams);

        RelativeLayout.LayoutParams imgParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        imageView = new ImageView(getContext());
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        addView(imageView, imgParams);
    }

    private int width;
    private int height;

    public void calculateSize(String localpath){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(localpath, options);

        width=options.outWidth;
        height=options.outHeight;

        int maxDp = SystemUtil.dipToPx(160);
        if (height != 0 && width != 0) {
            double scale = (width * 1.00) / height;
            if (width >= height) {
                width = maxDp;
                height = (int) (width / scale);
            } else {
                height = maxDp;
                width = (int) (height * scale);
            }
        } else {
            width = maxDp;
            height = maxDp;
        }
    }


    public void loadUri(final MsgDirect direct, final String pukkey, final String msgid, final String url) {
        msgDirect = direct;

        imageView.setImageBitmap(null);
        final String localPath = FileUtil.newContactFileName(pukkey, msgid, FileUtil.FileType.IMG);

        if (FileUtil.islocalFile(url) || FileUtil.isExistFilePath(localPath)) {
            progressBar.setVisibility(GONE);
            String local = FileUtil.islocalFile(url) ? url : localPath;
            calculateSize(local);

            Glide.with(BaseApplication.getInstance())
                    .load(local)
                    .override(width, height)
                    .bitmapTransform(new CenterCrop(context), new MaskTransformation(context, msgDirect == MsgDirect.From ? R.mipmap.message_box_white2x : R.mipmap.message_box_blue2x))
                    .into(imageView);
        } else {
            FileDownLoad.getInstance().downChatFile(url, pukkey, new FileDownLoad.IFileDownLoad() {
                @Override
                public void successDown(byte[] bytes) {
                    progressBar.setVisibility(GONE);
                    FileUtil.byteArrToFilePath(bytes, localPath);
                    loadUri(direct, pukkey, msgid, localPath);
                }

                @Override
                public void failDown() {
                    Glide.with(BaseApplication.getInstance())
                            .load( R.mipmap.img_error)
                            .override(width,height)
                            .bitmapTransform(new CenterCrop(context),new MaskTransformation(context, msgDirect == MsgDirect.From ? R.mipmap.message_box_white2x : R.mipmap.message_box_blue2x))
                            .into(imageView);
                }

                @Override
                public void onProgress(long bytesWritten, long totalSize) {
                    int progress = (int) (bytesWritten * 100 / totalSize);
                    progressBar.setVisibility(VISIBLE);
                    progressBar.setProgress(progress);
                }
            });
        }
    }

    public boolean isOpenBurn() {
        return openBurn;
    }

    public void setOpenBurn(boolean openBurn) {
        this.openBurn = openBurn;
    }

    public void setBubbleType(BubbleType bubbleType) {
        this.bubbleType = bubbleType;
    }
}
