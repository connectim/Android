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
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import connect.ui.activity.R;
import connect.ui.activity.chat.bean.MsgDirect;
import connect.ui.activity.chat.inter.FileDownLoad;
import connect.ui.base.BaseApplication;
import connect.utils.FileUtil;
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

    public void loadUri(MsgDirect direct, final String pukkey, String msgid, String url) {
        msgDirect = direct;

        imageView.setImageBitmap(null);
        final String localPath = FileUtil.newContactFileName(pukkey, msgid, FileUtil.FileType.IMG);

        if (FileUtil.islocalFile(url) || FileUtil.isExistFilePath(localPath)) {
            progressBar.setVisibility(GONE);
            String local = FileUtil.islocalFile(url) ? url : localPath;
            Glide.with(BaseApplication.getInstance())
                    .load(local)
                    .asBitmap()
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                            loadBackImg(resource);
                        }

                        @Override
                        public void onLoadFailed(Exception e, Drawable errorDrawable) {
                            super.onLoadFailed(e, errorDrawable);
                            loadBackImg(BitmapFactory.decodeResource(context.getResources(), R.mipmap.img_error));
                        }
                    });
        } else {
            FileDownLoad.getInstance().downChatFile(url, pukkey, new FileDownLoad.IFileDownLoad() {
                @Override
                public void successDown(byte[] bytes) {
                    progressBar.setVisibility(GONE);
                    FileUtil.byteArrToFilePath(bytes, localPath);
                    loadBackImg(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
                }

                @Override
                public void failDown() {
                    loadBackImg(BitmapFactory.decodeResource(context.getResources(), R.mipmap.img_error));
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

    protected void loadBackImg(Bitmap source) {
        getRoundCornerImage(source);
    }

    public void getRoundCornerImage(Bitmap bitmap_in) {
        int width = bitmap_in.getWidth();
        int height = bitmap_in.getHeight();
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

        LayoutParams params = (LayoutParams) getLayoutParams();
        params.width = width;
        params.height = height;
        setLayoutParams(params);

        new AsyncTask<Object, Void, Bitmap>() {

            @Override
            protected Bitmap doInBackground(Object... params) {
                int width = (int) params[0];
                int height = (int) params[1];

                Bitmap roundConcerImage = Bitmap.createBitmap(width, height, Config.ARGB_8888);
                Canvas canvas = new Canvas(roundConcerImage);
                Paint paint = new Paint();
                Rect rect = new Rect(0, 0, width, height);
                Rect rectF = new Rect(0, 0, (int) params[2], (int) params[3]);
                paint.setAntiAlias(true);

                Bitmap bitmap_bg = BitmapFactory.decodeResource(context.getResources(),
                        msgDirect == MsgDirect.From ? R.mipmap.message_box_white2x : R.mipmap.message_box_blue2x);

                NinePatch patch = new NinePatch(bitmap_bg, bitmap_bg.getNinePatchChunk(), null);
                patch.draw(canvas, rect);
                paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
                canvas.drawBitmap((Bitmap) params[4], rectF, rect, paint);

                if (openBurn) {
                    BlurTransformation transformation = new BlurTransformation(BaseApplication.getInstance(),20);
                    roundConcerImage = transformation.blur(roundConcerImage);
                }
                return roundConcerImage;
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                super.onPostExecute(bitmap);
                imageView.setImageBitmap(bitmap);
            }
        }.execute(width, height, bitmap_in.getWidth(), bitmap_in.getHeight(), bitmap_in);
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
