package connect.widget.album.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import connect.ui.activity.R;
import connect.utils.glide.GlideUtil;
import connect.widget.album.model.AlbumFile;

/**
 * Created by pujin on 2017/3/22.
 */

public class AlbumListAdapter extends RecyclerView.Adapter<AlbumListAdapter.ViewHolder>{

    private List<AlbumFile> albumFiles = new ArrayList<>();
    private AlbumGridListener albumGridListener = null;

    public void setData(List<AlbumFile> albumFiles){
        this.albumFiles = albumFiles;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.album_item_list, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final AlbumFile albumFile = albumFiles.get(position);
        String showPath = albumFile.getPath();
        GlideUtil.loadImage(holder.preImg, showPath);

        boolean selectstate = albumFile.isChecked();
        holder.checkBox.setSelected(selectstate);

        if (albumFile.getMediaType() == AlbumFile.TYPE_IMAGE) {
            holder.videoImg.setVisibility(View.GONE);
        } else {
            holder.videoImg.setVisibility(View.VISIBLE);
        }

        holder.preImg.setOnClickListener(new View.OnClickListener() {//preview
            @Override
            public void onClick(View v) {
                albumGridListener.ItemClick(position);
            }
        });

        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {//The selected
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                boolean selectstate = !albumFile.isChecked();
                if (albumGridListener.ItemCheck(selectstate, albumFile)) {
                    buttonView.setSelected(selectstate);
                }
            }
        });
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        int size = 0;
        if (albumFiles != null) {
            size = albumFiles.size();
        }
        return size;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView preImg;
        ImageView videoImg;
        CheckBox checkBox;

        ViewHolder(View view) {
            super(view);
            preImg = (ImageView) view.findViewById(R.id.iv_album_item);
            videoImg = (ImageView) view.findViewById(R.id.img2);
            checkBox = (CheckBox) view.findViewById(R.id.ckb_image_select);
        }
    }

    public interface AlbumGridListener {

        boolean itemIsSelect(AlbumFile albumFile);

        void ItemClick(int position);

        /**
         * The return value true can be operated (the selected number cannot operate the maximum number of optional)
         * @param isChecked
         * @param imageInfo
         * @return
         */
        boolean ItemCheck(boolean isChecked, AlbumFile imageInfo);
    }

    public void setAlbumGridListener(AlbumGridListener albumGridListener) {
        this.albumGridListener = albumGridListener;
    }
}
