package connect.activity.chat.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import connect.database.green.bean.GroupMemberEntity;
import connect.ui.activity.R;
import connect.utils.PinyinUtil;
import connect.utils.glide.GlideUtil;

/**
 * Created by pujin on 2017/1/18.
 */

public class GroupMemberSelectAdapter extends RecyclerView.Adapter<GroupMemberSelectAdapter.GroupMemberHolder> {

    private LayoutInflater inflater;
    private List<GroupMemberEntity> groupMemEntities = new ArrayList<>();

    public GroupMemberSelectAdapter(Context context,List<GroupMemberEntity> entities) {
        this.inflater = LayoutInflater.from(context);
        this.groupMemEntities = entities;
    }

    @Override
    public GroupMemberSelectAdapter.GroupMemberHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_header_imgtxt, parent, false);
        GroupMemberSelectAdapter.GroupMemberHolder holder = new GroupMemberSelectAdapter.GroupMemberHolder(view);
        view.setOnClickListener(itemClickListener);
        return holder;
    }

    @Override
    public void onBindViewHolder(GroupMemberSelectAdapter.GroupMemberHolder holder, int position) {
        GroupMemberEntity entity = groupMemEntities.get(position);

        GlideUtil.loadAvatarRound(holder.roundimg, entity.getAvatar());
        String curName = TextUtils.isEmpty(entity.getNick()) ? entity.getUsername() : entity.getNick();
        if (TextUtils.isEmpty(curName)) return;

        holder.name.setText(curName);

        String curFirst = PinyinUtil.chatToPinyin(curName.charAt(0));
        if (position == 0) {
            holder.txt.setVisibility(View.VISIBLE);
            holder.txt.setText(curFirst);
        } else {
            GroupMemberEntity lastEntity = groupMemEntities.get(position - 1);
            String lastName = TextUtils.isEmpty(lastEntity.getNick()) ? lastEntity.getUsername() : lastEntity.getNick();
            String lastFirst = PinyinUtil.chatToPinyin(lastName.charAt(0));
            if (lastFirst.equals(curFirst)) {
                holder.txt.setVisibility(View.GONE);
            } else {
                holder.txt.setVisibility(View.VISIBLE);
                holder.txt.setText(curFirst);
            }
        }

        holder.itemView.setTag(position);
        holder.itemView.setOnClickListener(itemClickListener);
    }

    @Override
    public int getItemCount() {
        return groupMemEntities.size();
    }

    public int getPositionForSection(char selectchar) {
        for (int i = 0; i < groupMemEntities.size(); i++) {
            GroupMemberEntity entity = groupMemEntities.get(i);
            String showName = TextUtils.isEmpty(entity.getNick()) ? entity.getUsername() : entity.getNick();
            String firstChar = PinyinUtil.chatToPinyin(showName.charAt(0));
            if (firstChar.charAt(0) >= selectchar) {
                return i;
            }
        }
        return -1;
    }

    private View.OnClickListener itemClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int posi = (int) v.getTag();
            GroupMemberEntity index = groupMemEntities.get(posi);
            transferToListener.transferTo(index);
        }
    };

    public GroupTransferToListener transferToListener;

    public interface GroupTransferToListener{
        void transferTo(GroupMemberEntity memEntity);
    }

    public void setTransferToListener(GroupTransferToListener listener) {
         transferToListener=listener;
    }

    static class GroupMemberHolder extends RecyclerView.ViewHolder {
        TextView txt;
        ImageView roundimg;
        TextView name;

        GroupMemberHolder(View view) {
            super(view);
            txt = (TextView) view.findViewById(R.id.txt);
            roundimg = (ImageView) view.findViewById(R.id.roundimg);
            name = (TextView) view.findViewById(R.id.name);
        }
    }
}