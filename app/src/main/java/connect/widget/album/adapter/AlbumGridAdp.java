package connect.widget.album.adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import connect.ui.activity.R;
import connect.utils.glide.GlideUtil;
import connect.widget.album.model.ImageInfo;

/**
 * Created by pujin on 2017/3/22.
 */

public class AlbumGridAdp extends BaseAdapter{

    private List<ImageInfo> imageInfos = new ArrayList<>();
    private AlbumGridListener albumGridListener = null;

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
    public ImageInfo getItem(int position) {
        return imageInfos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        AlbumGridHolder gridHolder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.album_grid_item, null);
            gridHolder = new AlbumGridHolder(convertView);
            convertView.setTag(gridHolder);
        } else {
            gridHolder = (AlbumGridHolder) convertView.getTag();
        }

        ImageInfo imageInfo = imageInfos.get(position);
        String showPath = imageInfo.getImageFile().getAbsolutePath();
        GlideUtil.loadImage(gridHolder.preImg, showPath);

        boolean selectstate = albumGridListener.itemIsSelect(imageInfo);
        gridHolder.checkBox.setSelected(selectstate);

        if (imageInfo.getFileType() == 0) {
            gridHolder.videoImg.setVisibility(View.GONE);
        } else {
            gridHolder.videoImg.setVisibility(View.VISIBLE);
        }

        gridHolder.preImg.setOnClickListener(new View.OnClickListener() {//preview
            @Override
            public void onClick(View v) {
                albumGridListener.ItemClick(position);
            }
        });

        gridHolder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {//The selected
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                ImageInfo imageInfo = imageInfos.get(position);
                boolean selectstate = !albumGridListener.itemIsSelect(imageInfo);
                if (albumGridListener.ItemCheck(selectstate, imageInfo)) {
                    buttonView.setSelected(selectstate);
                }
            }
        });
        return convertView;
    }

    private static class AlbumGridHolder {

        View view;
        ImageView preImg;
        ImageView videoImg;
        CheckBox checkBox;

        public AlbumGridHolder(View view) {
            this.view = view;
            preImg = (ImageView) view.findViewById(R.id.iv_album_item);
            videoImg= (ImageView) view.findViewById(R.id.img2);
            checkBox = (CheckBox) view.findViewById(R.id.ckb_image_select);
        }
    }

    public interface AlbumGridListener {

        boolean itemIsSelect(ImageInfo imageInfo);

        void ItemClick(int position);

        /**
         * The return value true can be operated (the selected number cannot operate the maximum number of optional)
         * @param isChecked
         * @param imageInfo
         * @return
         */
        boolean ItemCheck(boolean isChecked, ImageInfo imageInfo);
    }

    public void setAlbumGridListener(AlbumGridListener albumGridListener) {
        this.albumGridListener = albumGridListener;
    }
}
