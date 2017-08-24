package connect.widget.album.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import java.io.File;
import java.util.List;
import connect.ui.activity.R;
import connect.utils.glide.GlideUtil;
import connect.widget.album.model.AlbumFolderInfo;
import connect.widget.album.model.AlbumType;
import connect.widget.album.model.ImageInfo;

/**
 * Created by Administrator on 2017/8/21.
 */
public class AlbumFolderAdapter extends RecyclerView.Adapter<AlbumFolderAdapter.ViewHolder> {

    private List<AlbumFolderInfo> folderInfos;

    public AlbumFolderAdapter(List<AlbumFolderInfo> folderInfos) {
        this.folderInfos = folderInfos;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.album_directory_item, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        AlbumFolderInfo albumFolderInfo = folderInfos.get(position);

        File frontCover = albumFolderInfo.getFrontCover();
        GlideUtil.loadAvater(holder.ivAlbumCover, frontCover.getAbsolutePath());
        String folderName = albumFolderInfo.getFolderName();
        folderName = folderName.length() > 8 ? folderName.substring(0, 8) + "..." : folderName;
        holder.tvDirectoryName.setText(folderName);

        if (albumFolderInfo.getAlbumType() == AlbumType.Photo) {
            holder.videoStateImg.setVisibility(View.GONE);
        } else {
            holder.videoStateImg.setVisibility(View.VISIBLE);
        }

        List<ImageInfo> imageInfoList = albumFolderInfo.getImageInfoList();
        holder.tvChildCount.setText(imageInfoList.size() + "");

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemClickListener.itemClick(position);
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
        if (folderInfos != null) {
            size = folderInfos.size();
        }
        return size;
    }

    private OnItemClickListener itemClickListener;

    public interface OnItemClickListener {
        void itemClick(int position);
    }

    public void setItemClickListener(OnItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }


    static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView ivAlbumCover;
        ImageView videoStateImg;
        TextView tvDirectoryName;
        TextView tvChildCount;

        public ViewHolder(View itemView) {
            super(itemView);
            ivAlbumCover = (ImageView) itemView.findViewById(R.id.iv_album_cover);
            videoStateImg = (ImageView) itemView.findViewById(R.id.img2);
            tvDirectoryName = (TextView) itemView.findViewById(R.id.tv_directory_name);
            tvChildCount = (TextView) itemView.findViewById(R.id.tv_child_count);
        }
    }
}