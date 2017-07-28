package connect.activity.common.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import connect.database.green.bean.ContactEntity;
import connect.ui.activity.R;
import connect.utils.PinyinUtil;
import connect.utils.glide.GlideUtil;
import connect.widget.roundedimageview.RoundedImageView;

/**
 * Created by gtq on 2016/12/15.
 */
public class MulContactAdapter extends RecyclerView.Adapter<MulContactAdapter.MulHolder> {

    private LayoutInflater inflater;
    private ArrayList<String> oldMemberList = new ArrayList<>();
    private ArrayList<String> memberList = new ArrayList<>();
    private List<ContactEntity> friendEntities;

    private ArrayList<ContactEntity> selectEntities = new ArrayList<>();
    private OnSeleFriendListence onSeleFriendListence = null;

    public MulContactAdapter(Context context, List<String> members, List<ContactEntity> entities,ArrayList<ContactEntity> seledFriend) {
        this.inflater = LayoutInflater.from(context);
        this.friendEntities = entities;

        if (members != null) {
            oldMemberList.addAll(members);
            memberList.addAll(members);
        }
        if(seledFriend != null){
            memberList.clear();
            selectEntities.clear();
            selectEntities.addAll(seledFriend);
            for(ContactEntity contactEntity : seledFriend){
                memberList.add(contactEntity.getPub_key());
            }
        }
    }

    public ArrayList<ContactEntity> getSelectEntities() {
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
        if (memberList.contains(curPub)) {//contact already in group
            holder.secView.setSelected(true);
        } else {
            holder.secView.setSelected(false);
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
            View secview = v.findViewById(R.id.select);
            String pubkey = index.getPub_key();
            if (oldMemberList.contains(pubkey)) {
                secview.setSelected(true);
                return;
            }

            if (memberList.contains(pubkey)) {
                memberList.remove(pubkey);
                for (ContactEntity contactEntity : selectEntities) {
                    if (contactEntity.getPub_key().equals(index.getPub_key())) {
                        selectEntities.remove(contactEntity);
                        break;
                    }
                }
                secview.setSelected(false);
            } else {
                memberList.add(pubkey);
                selectEntities.add(index);
                secview.setSelected(true);
            }

            if (onSeleFriendListence != null) {
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