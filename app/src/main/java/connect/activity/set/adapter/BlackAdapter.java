package connect.activity.set.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import connect.ui.activity.R;
import connect.utils.glide.GlideUtil;
import connect.widget.roundedimageview.RoundedImageView;
import protos.Connect;

/**
 * Created by Administrator on 2017/1/4.
 */
public class BlackAdapter extends RecyclerView.Adapter<BlackAdapter.ViewHolder> {

    private ArrayList<Connect.UserInfo> mDataList = new ArrayList<>();
    private OnItemChildClickListence childListence;

    private Activity activity;

    public BlackAdapter(Activity activity) {
        this.activity = activity;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(activity);
        View view = inflater.inflate(R.layout.item_set_black_list, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder,final int position) {
        final Connect.UserInfo userInfo = mDataList.get(position);
        GlideUtil.loadAvater(holder.avatarRimg, userInfo.getAvatar());
        holder.nicknameTv.setText(userInfo.getUsername());
        holder.statusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                childListence.remove(position, userInfo);
            }
        });
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    public void setDataNotify(List<Connect.UserInfo> list) {
        mDataList.clear();
        mDataList.addAll(list);
        notifyDataSetChanged();
    }

    public void removeDataNotify(int position){
        mDataList.remove(position);
        notifyDataSetChanged();
    }

    public void setOnItemChildListence(OnItemChildClickListence childListence){
        this.childListence = childListence;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.avatar_rimg)
        RoundedImageView avatarRimg;
        @Bind(R.id.nickname_tv)
        TextView nicknameTv;
        @Bind(R.id.status_btn)
        Button statusBtn;
        @Bind(R.id.content_rela)
        RelativeLayout contentRela;

        ViewHolder(View itemview) {
            super(itemview);
            avatarRimg = (RoundedImageView) itemview.findViewById(R.id.avatar_rimg);
            nicknameTv = (TextView) itemview.findViewById(R.id.nickname_tv);
            statusBtn = (Button) itemview.findViewById(R.id.status_btn);
            contentRela = (RelativeLayout) itemview.findViewById(R.id.content_rela);
        }
    }

    public interface OnItemChildClickListence{
        void remove(int position,Connect.UserInfo userInfo);
    }
}
