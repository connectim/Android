package connect.activity.chat.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import connect.database.green.bean.ContactEntity;
import connect.ui.activity.R;
import connect.utils.glide.GlideUtil;
import connect.widget.DepartmentAvatar;

/**
 * Created by PuJin on 2018/1/11.
 */

public class BaseGroupSelectAdapter extends RecyclerView.Adapter<BaseGroupSelectAdapter.FavoriteHolder> {

    private List<ContactEntity> contactEntities = new ArrayList<>();
    private Context context;
    private String friendUid = "";

    public BaseGroupSelectAdapter() {

    }

    public void setData(List<ContactEntity> contactEntities) {
        this.contactEntities = contactEntities;
        notifyDataSetChanged();
    }

    @Override
    public FavoriteHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_basegroup_favorite, parent, false);
        FavoriteHolder holder = new FavoriteHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(final FavoriteHolder holder, int position) {
        final ContactEntity entity = contactEntities.get(position);

        String avatar = entity.getAvatar();
        String name = entity.getName();

        if (position == 1) {
            holder.topTv.setVisibility(View.VISIBLE);
            holder.topTv.setText(context.getString(R.string.Link_Favorite_Friend));
        } else {
            holder.topTv.setVisibility(View.GONE);
        }

        if (TextUtils.isEmpty(avatar)) {
            if (entity.getGender() == 3) {
                holder.selectView.setVisibility(View.GONE);
                holder.nameTv.setVisibility(View.VISIBLE);
                holder.avatarImg.setVisibility(View.VISIBLE);
                holder.departmentAvatar.setVisibility(View.GONE);

                holder.nameTv.setText(name);
                GlideUtil.loadAvatarRound(holder.avatarImg, R.mipmap.department);
                holder.contentLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        groupSelectListener.organizeClick();
                    }
                });
            } else {
                holder.selectView.setVisibility(View.VISIBLE);
                holder.nameTv.setVisibility(View.VISIBLE);
                holder.avatarImg.setVisibility(View.GONE);
                holder.departmentAvatar.setVisibility(View.VISIBLE);

                holder.selectView.setSelected(groupSelectListener.isContains(entity.getUid()));
                holder.nameTv.setText(name);
                holder.departmentAvatar.setAvatarName(name, false, entity.getGender());
                holder.contentLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String uid = entity.getUid();
                        if (!friendUid.equals(uid)) {
                            boolean isselect = holder.selectView.isSelected();
                            isselect = !isselect;
                            groupSelectListener.itemClick(isselect, entity);
                            holder.selectView.setSelected(isselect);
                        }
                    }
                });
            }
        } else {
            holder.selectView.setVisibility(View.VISIBLE);
            holder.nameTv.setVisibility(View.VISIBLE);
            holder.avatarImg.setVisibility(View.VISIBLE);
            holder.departmentAvatar.setVisibility(View.GONE);

            holder.selectView.setSelected(groupSelectListener.isContains(entity.getUid()));
            holder.nameTv.setText(name);
            GlideUtil.loadAvatarRound(holder.avatarImg, entity.getAvatar());
            holder.contentLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String uid = entity.getUid();
                    if (!friendUid.equals(uid)) {
                        boolean isselect = holder.selectView.isSelected();
                        isselect = !isselect;
                        groupSelectListener.itemClick(isselect, entity);
                        holder.selectView.setSelected(isselect);
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return contactEntities.size();
    }

    static class FavoriteHolder extends RecyclerView.ViewHolder {

        LinearLayout contentLayout;
        TextView topTv;
        View selectView;
        DepartmentAvatar departmentAvatar;
        ImageView avatarImg;
        TextView nameTv;

        FavoriteHolder(View view) {
            super(view);
            topTv = (TextView) view.findViewById(R.id.top_tv);
            contentLayout = (LinearLayout) view.findViewById(R.id.linearlayout);
            selectView = view.findViewById(R.id.select);
            departmentAvatar = (DepartmentAvatar) view.findViewById(R.id.avatar_lin);
            avatarImg = (ImageView) view.findViewById(R.id.avatar_rimg);
            nameTv = (TextView) view.findViewById(R.id.text_view);
        }
    }

    private BaseGroupSelectListener groupSelectListener;

    public interface BaseGroupSelectListener {

        boolean isContains(String selectKey);

        void organizeClick();

        void itemClick(boolean isSelect, ContactEntity contactEntity);
    }

    public void setGroupSelectListener(BaseGroupSelectListener groupSelectListener) {
        this.groupSelectListener = groupSelectListener;
    }

    public void setFriendUid(String friendUid) {
        this.friendUid = friendUid;
    }
}
