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
import connect.utils.PinyinUtil;
import connect.utils.glide.GlideUtil;
import connect.view.roundedimageview.RoundedImageView;

/**
 * Created by gtq on 2016/12/15.
 */
public class MulContactAdapter extends RecyclerView.Adapter<MulContactAdapter.MulHolder> {

    private LayoutInflater inflater;
    private List<String> members;
    private List<ContactEntity> friendEntities;

    private List<ContactEntity> selectEntities = new ArrayList<>();
    private OnSeleFriendListence onSeleFriendListence = null;

    public MulContactAdapter(Context context, List<String> members, List<ContactEntity> entities) {
        this.inflater = LayoutInflater.from(context);
        this.members = members;
        this.friendEntities = entities;
    }

    public List<ContactEntity> getSelectEntities() {
        return selectEntities;
    }

    @Override
    public MulHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_mulcontact, parent, false);
        MulHolder cardHolder = new MulHolder(view);
        return cardHolder;
    }

    @Override
    public void onBindViewHolder(MulHolder holder, int position) {
        ContactEntity entity = friendEntities.get(position);

        GlideUtil.loadAvater(holder.roundimg, entity.getAvatar());
        String curName = TextUtils.isEmpty(entity.getRemark()) ? entity.getUsername() : entity.getRemark();
        if (TextUtils.isEmpty(curName)) return;

        holder.name.setText(entity.getUsername());

        String curFirst = PinyinUtil.chatToPinyin(curName.charAt(0));
        if (position == 0) {
            holder.txt.setVisibility(View.VISIBLE);
            holder.txt.setText(curFirst);
        } else {
            ContactEntity lastEntity = friendEntities.get(position - 1);
            String lastName = TextUtils.isEmpty(lastEntity.getRemark()) ? lastEntity.getUsername() : lastEntity.getRemark();
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

        String curPub = entity.getPub_key();
        if (members.contains(curPub)) {//contact already in group
            holder.secView.setSelected(true);
        } else {
            if (selectEntities.contains(entity)) {
                holder.secView.setSelected(true);
            } else {
                holder.secView.setSelected(false);
            }
        }
    }

    @Override
    public int getItemCount() {
        return friendEntities.size();
    }

    public int getPositionForSection(char selectchar) {
        for (int i = 0; i < friendEntities.size(); i++) {
            ContactEntity entity = friendEntities.get(i);
            String showName = TextUtils.isEmpty(entity.getRemark()) ? entity.getUsername() : entity.getRemark();
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
            ContactEntity index = friendEntities.get(posi);
            String pubkey = index.getPub_key();
            if (!members.contains(pubkey)) {
                View secview = v.findViewById(R.id.select);
                if (selectEntities.contains(index)) {
                    selectEntities.remove(index);
                    secview.setSelected(false);
                } else {
                    selectEntities.add(index);
                    secview.setSelected(true);
                }
            }

            if(onSeleFriendListence != null){
                onSeleFriendListence.seleFriend(selectEntities);
            }

        }
    };

    public void setOnSeleFriendListence(OnSeleFriendListence onSeleFriendListence){
        this.onSeleFriendListence = onSeleFriendListence;
    }

    static class MulHolder extends RecyclerView.ViewHolder {
        TextView txt;
        View secView;
        RoundedImageView roundimg;
        TextView name;

        MulHolder(View view) {
            super(view);
            txt = (TextView) view.findViewById(R.id.txt);
            secView = view.findViewById(R.id.select);
            roundimg = (RoundedImageView) view.findViewById(R.id.roundimg);
            name = (TextView) view.findViewById(R.id.name);
        }
    }

    public interface OnSeleFriendListence {

        void seleFriend(List<ContactEntity> list);

    }
}