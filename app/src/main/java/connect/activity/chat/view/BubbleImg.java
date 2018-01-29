package connect.activity.chat.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import connect.activity.base.BaseApplication;
import connect.ui.activity.R;
import connect.utils.FileUtil;
import connect.utils.chatfile.download.DownLoadFile;
import connect.utils.chatfile.inter.InterFileDown;
import connect.utils.glide.BlurMaskTransformation;
import connect.utils.log.LogManager;
import connect.utils.system.SystemUtil;
import instant.bean.MsgDirect;
import instant.sender.model.RobotChat;
import protos.Connect;

public class BubbleImg extends RelativeLayout {

    private static String TAG="_BubbleImg";
    private Context context;

    private boolean openBurn = false;

    private MsgDirect msgDirect;
    private ProgressBar progressBar;
    private ImageView imageView;
    private String localPath=null;

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

    private int imgWidth;
    private int imgHeight;

    public void calculateSize(float width, float height) {
        int maxDp = SystemUtil.dipToPx(160);
        if (height != 0 && width != 0) {
            double scale = (width * 1.00) / height;
            if (width >= height) {
                imgWidth = maxDp;
                imgHeight = (int) (maxDp / scale);
            } else {
                imgHeight = maxDp;
                imgWidth = (int) (maxDp * scale);
            }
        } else {
            imgWidth = maxDp;
            imgHeight = maxDp;
        }
    }

    public void loadUri(final MsgDirect direct, final Connect.ChatType chatType, final String pukkey, final String msgid, final String filekey,final String url, final float imgwidth, final float imgheight) {
        msgDirect = direct;

        imageView.setImageBitmap(null);
        String msgidToLocal = FileUtil.newContactFileName(pukkey, msgid, FileUtil.FileType.IMG);

        if (pukkey.equals(RobotChat.getInstance().chatKey()) || FileUtil.isLocalFile(url) || FileUtil.isExistFilePath(msgidToLocal)) {
            progressBar.setVisibility(GONE);
            if (pukkey.equals(RobotChat.getInstance().chatKey())) {
                localPath = url;
            } else {
                localPath = FileUtil.isLocalFile(url) ? url : msgidToLocal;
            }
            calculateSize(imgwidth,imgheight);

            Glide.with(BaseApplication.getInstance())
                    .load(localPath)
                    .override(imgWidth, imgHeight)
                    .crossFade(1000)
                    .error(R.mipmap.img_default)
                    .listener(listener)
                    .bitmapTransform(new CenterCrop(context), new BlurMaskTransformation(context, msgDirect == MsgDirect.From ? R.mipmap.message_box_white2x : R.mipmap.message_box_blue2x, (openBurn && msgDirect == MsgDirect.From) ? 16 : 0))
                    .into(imageView);
        } else {
            DownLoadFile loadFile = new DownLoadFile(url, new InterFileDown() {

                @Override
                public void successDown(byte[] bytes) {
                    progressBar.setVisibility(GONE);
                    String localPath = FileUtil.newContactFileName(pukkey, msgid, FileUtil.FileType.IMG);

                    bytes = decodeFile(filekey, bytes);
                    FileUtil.byteArrToFilePath(bytes, localPath);
                    loadUri(direct, chatType, pukkey, msgid, filekey,localPath, imgwidth, imgheight);
                }

                @Override
                public void failDown() {
                    Glide.with(BaseApplication.getInstance())
                            .load(R.mipmap.img_error)
                            .crossFade(1000)
                            .bitmapTransform(new CenterCrop(context), new BlurMaskTransformation(context, msgDirect == MsgDirect.From ? R.mipmap.message_box_white2x : R.mipmap.message_box_blue2x, (openBurn && msgDirect == MsgDirect.From) ? 16 : 0))
                            .into(imageView);
                }

                @Override
                public void onProgress(long bytesWritten, long totalSize) {
                    int progress = (int) (bytesWritten * 100 / totalSize);
                    progressBar.setVisibility(VISIBLE);
                    progressBar.setProgress(progress);
                }
            });
            loadFile.downFile();
        }
    }

    public void setOpenBurn(boolean openBurn) {
        this.openBurn = openBurn;
    }

    public String getLocalPath() {
        return localPath;
    }

    RequestListener listener = new RequestListener<String, GlideDrawable>() {
        @Override
        public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
            LogManager.getLogger().d(TAG, "isFromMemoryCache:"+e.getMessage()+"  model:"+model+" isFirstResource: "+isFirstResource);
            return false;
        }

        @Override
        public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
            //imageView.setImageDrawable(resource);
            return false;
        }
    };
}
