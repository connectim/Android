package connect.widget.imagewatcher;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import connect.ui.activity.R;
import connect.activity.base.BaseActivity;
import connect.utils.ActivityUtil;
import connect.utils.glide.GlideUtil;
import connect.widget.album.ui.widget.HackyViewPager;
import connect.widget.photoview.PhotoView;
import connect.widget.photoview.PhotoViewAttacher;

/**
 * Image viewer
 * Created by gtq on 2017/1/3.
 */
public class ImageViewerActivity extends BaseActivity {

    @Bind(R.id.viewpager)
    HackyViewPager viewpager;

    private ImageViewerActivity activity;
    private static String IMG_POSITION = "IMG_POSITION";
    private static String IMG_LIST = "IMG_LIST";
    private String firstPath;
    private List<String> imgList = null;
    private PreviewPagerAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imgviewer);
        int flag = WindowManager.LayoutParams.FLAG_FULLSCREEN;
        Window window = this.getWindow();
        window.setFlags(flag, flag);

        ButterKnife.bind(this);
        initView();
    }

    public static void startActivity(Activity activity, String path, ArrayList<String> imgs) {
        Bundle bundle = new Bundle();
        bundle.putString(IMG_POSITION, path);
        bundle.putSerializable(IMG_LIST, imgs);
        ActivityUtil.next(activity, ImageViewerActivity.class, bundle);
    }

    @Override
    public void initView() {
        activity = this;

        firstPath = getIntent().getStringExtra(IMG_POSITION);
        imgList = (List<String>) getIntent().getSerializableExtra(IMG_LIST);

        pagerAdapter = new PreviewPagerAdapter();
        viewpager.setAdapter(pagerAdapter);
        if (imgList != null && imgList.contains(firstPath)) {
            int initShowPosition = imgList.indexOf(firstPath);
            viewpager.setCurrentItem(initShowPosition);
        }
    }

    /**
     * To monitor PhotoView click event
     */
    private PhotoViewAttacher.OnViewTapListener mOnPreviewTapListener = new PhotoViewAttacher.OnViewTapListener() {
        @Override
        public void onViewTap(View view, float v, float v1) {
            ActivityUtil.goBack(activity);
        }
    };

    private class PreviewPagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return imgList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            PhotoView galleryPhotoView = (PhotoView) view.findViewById(R.id.iv_show_image);
            galleryPhotoView.setScale(1.0f);//Allow the image to revert to the size of the original image before the zoom operation
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View galleryItemView = View.inflate(activity, R.layout.preview_image_item, null);
            String path = imgList.get(position);

            PhotoView galleryPhotoView = (PhotoView) galleryItemView.findViewById(R.id.iv_show_image);
            galleryPhotoView.setOnViewTapListener(mOnPreviewTapListener);

            GlideUtil.loadImage(galleryPhotoView, path);
            container.addView(galleryItemView);

            galleryItemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ActivityUtil.goBack(activity);
                }
            });
            return galleryItemView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }
}