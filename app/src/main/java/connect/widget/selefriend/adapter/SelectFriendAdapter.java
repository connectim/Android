package connect.widget.selefriend.adapter;

import android.app.Activity;
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
import connect.utils.PinyinUtil;
import connect.utils.glide.GlideUtil;


public class SelectFriendAdapter extends RecyclerView.Adapter<SelectFriendAdapter.Holder> {

    private Activity activity;
    /** Have chosen */
    private List<String> selectList=new ArrayList<>();
    /** Already exists */
    private List<String> existList;
    private ArrayList<ContactEntity> dataList = new ArrayList<>();
    private OnSelectFriendListener onSelectFriendListener;

    public SelectFriendAdapter(Activity activity) {
        this.activity = activity;
    }

    @Override
    public SelectFriendAdapter.Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.item_mulcontact, parent, false);
        SelectFriendAdapter.Holder holder = new SelectFriendAdapter.Holder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(final SelectFriendAdapter.Holder holder, int position) {
        final ContactEntity entity = dataList.get(position);
        GlideUtil.loadAvatarRound(holder.avatar, entity.getAvatar());
        String curName = entity.getName();
        holder.name.setText(curName);

        // According to the head alphabetical order
        showTopView(entity, position, holder);

        if (selectList.contains(entity.getUid()) || existList.contains(entity.getUid())) {
            holder.secView.setSelected(true);
        } else {
            holder.secView.setSelected(false);
        }

        holder.contentLin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(existList.contains(entity.getUid())){
                    return;
                }

                if(selectList.contains(entity.getUid())){
                    selectList.remove(entity.getUid());
                    holder.secView.setSelected(false);
                }else{
                    selectList.add(entity.getUid());
                    holder.secView.setSelected(true);
                }

                if (onSelectFriendListener != null) {
                    onSelectFriendListener.selectFriend(selectList);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    private void showTopView(ContactEntity entity, int  position, SelectFriendAdapter.Holder holder){
        String curName = entity.getName();
        String curFirstPing = PinyinUtil.chatToPinyin(curName.charAt(0));
        if (position == 0) {
            holder.txt.setVisibility(View.VISIBLE);
            holder.txt.setText(curFirstPing);
        } else {
            ContactEntity lastEntity = dataList.get(position - 1);
            String lastName = lastEntity.getName();
            String lastFirstPing = PinyinUtil.chatToPinyin(lastName.charAt(0));
            if (lastFirstPing.equals(curFirstPing)) {
                holder.txt.setVisibility(View.GONE);
            } else {
                holder.txt.setVisibility(View.VISIBLE);
                holder.txt.setText(curFirstPing);
            }
        }
    }

    public int getPositionForSection(char selectChar) {
        for (int i = 0; i < dataList.size(); i++) {
            ContactEntity entity = dataList.get(i);
            String showName = entity.getName();
            String firstChar = PinyinUtil.chatToPinyin(showName.charAt(0));
            if (firstChar.charAt(0) >= selectChar) {
                return i;
            }
        }
        return -1;
    }

    static class Holder extends RecyclerView.ViewHolder {
        TextView txt;
        View secView;
        ImageView avatar;
        TextView name;
        LinearLayout contentLin;

        Holder(View view) {
            super(view);
            txt = (TextView) view.findViewById(R.id.txt);
            secView = view.findViewById(R.id.select);
            avatar = (ImageView) view.findViewById(R.id.roundimg);
            name = (TextView) view.findViewById(R.id.name);
            contentLin = (LinearLayout) view.findViewById(R.id.content_lin);
        }
    }

    public void setDataNotify(List<ContactEntity> list, List<String> selectList, List<String> existList) {
        if (selectList != null) {
            this.selectList = selectList;
        }
        if (existList != null) {
            this.existList = existList;
        }

        dataList.clear();
        dataList.addAll(list);
        notifyDataSetChanged();
    }

    public ArrayList<ContactEntity> getSelectList(){
        ArrayList<ContactEntity> list = new ArrayList<>();
        for(ContactEntity contactEntity : dataList){
            if(selectList.contains(contactEntity.getUid())){
                list.add(contactEntity);
            }
        }
        return list;
    }

    public void setOnSelectFriendListener(OnSelectFriendListener onSelectFriendListener){
        this.onSelectFriendListener = onSelectFriendListener;
    }

    public interface OnSelectFriendListener {

        void selectFriend(List<String> list);

    }

}
