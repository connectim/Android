package connect.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import connect.db.green.bean.ContactEntity;
import connect.ui.activity.R;
import connect.utils.glide.GlideUtil;
import connect.view.roundedimageview.RoundedImageView;

/**
 *
 * Created by pujin on 2017/2/3.
 */
public class TranspondAdapter extends RecyclerView.Adapter<TranspondAdapter.TranspondHolder> {

    private LayoutInflater inflater;
    private List<ContactEntity> friendEntities = new ArrayList<>();

    public TranspondAdapter(Context context) {
        this.inflater = LayoutInflater.from(context);
    }

    public void setData(List<ContactEntity> entities) {
        this.friendEntities = entities;
        notifyDataSetChanged();
    }

    @Override
    public TranspondAdapter.TranspondHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_transpond, parent, false);
        TranspondAdapter.TranspondHolder holder = new TranspondAdapter.TranspondHolder(view);
        view.setOnClickListener(itemClickListener);
        return holder;
    }

    @Override
    public void onBindViewHolder(TranspondAdapter.TranspondHolder holder, int position) {
        ContactEntity entity = friendEntities.get(position);

        GlideUtil.loadAvater(holder.roundedImg, entity.getAvatar());
        String curName = TextUtils.isEmpty(entity.getRemark()) ? entity.getUsername() : entity.getRemark();
        holder.nickTxt.setText(curName);

        holder.itemView.setTag(entity);
    }

    @Override
    public int getItemCount() {
        return friendEntities.size();
    }

    private TranspondAdapter.OnItemClickListener itemClickListener;

    public interface OnItemClickListener extends View.OnClickListener {
    }

    public void setItemClickListener(TranspondAdapter.OnItemClickListener clickListener) {
        this.itemClickListener = clickListener;
    }

    class TranspondHolder extends RecyclerView.ViewHolder {

        RoundedImageView roundedImg;
        TextView nickTxt;

        public TranspondHolder(View itemView) {
            super(itemView);
            roundedImg = (RoundedImageView) itemView.findViewById(R.id.roundimg);
            nickTxt = (TextView) itemView.findViewById(R.id.txt1);
        }
    }
}
