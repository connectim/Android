package connect.ui.activity.set.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import connect.ui.activity.R;
import connect.utils.glide.GlideUtil;
import connect.view.roundedimageview.RoundedImageView;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import protos.Connect;

/**
 * Created by Administrator on 2017/1/4.
 */
public class BlackAdapter extends BaseAdapter {

    private ArrayList<Connect.UserInfo> mDataList = new ArrayList<>();
    private OnItemChildClickListence childListence;

    @Override
    public int getCount() {
        return mDataList.size();
    }

    @Override
    public Object getItem(int position) {
        return mDataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_set_black_list, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final Connect.UserInfo userInfo = mDataList.get(position);
        GlideUtil.loadAvater(viewHolder.avatarRimg,userInfo.getAvatar());
        viewHolder.nicknameTv.setText(userInfo.getUsername());
        viewHolder.statusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                childListence.remove(position,userInfo);
            }
        });

        return convertView;
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

    static class ViewHolder {
        @Bind(R.id.avatar_rimg)
        RoundedImageView avatarRimg;
        @Bind(R.id.nickname_tv)
        TextView nicknameTv;
        @Bind(R.id.status_btn)
        Button statusBtn;
        @Bind(R.id.content_rela)
        RelativeLayout contentRela;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    public interface OnItemChildClickListence{
        void remove(int position,Connect.UserInfo userInfo);
    }

}
