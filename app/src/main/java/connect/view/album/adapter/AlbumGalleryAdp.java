package connect.view.album.adapter;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import connect.ui.activity.R;
import connect.utils.glide.GlideUtil;
import connect.view.album.entity.ImageInfo;
import connect.view.photoview.PhotoView;

/**
 * Created by pujin on 2017/3/22.
 */
public class AlbumGalleryAdp extends PagerAdapter {

    private List<ImageInfo> imageInfos;

    public void setData(List<ImageInfo> imgs){
        this.imageInfos = imgs;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        int size = 0;
        if (imageInfos != null) {
            size = imageInfos.size();
        }
        return size;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        PhotoView galleryPhotoView = (PhotoView) view.findViewById(R.id.iv_show_image);
        galleryPhotoView.setScale(1.0f);//To restore images in the process of sliding back to the original size before zooming
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View galleryItemView = View.inflate(container.getContext(), R.layout.preview_image_item, null);

        ImageInfo imageInfo = imageInfos.get(position);
        PhotoView galleryPhotoView = (PhotoView) galleryItemView.findViewById(R.id.iv_show_image);
        GlideUtil.loadImage(galleryPhotoView, imageInfo.getImageFile().getAbsolutePath());
        container.addView(galleryItemView);
        return galleryItemView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }
}
