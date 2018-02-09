package connect.activity.chat.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import connect.database.green.bean.ContactEntity;
import connect.ui.activity.R;
import connect.utils.PinyinUtil;
import connect.utils.glide.GlideUtil;

/**
 * Created by gtq on 2016/12/13.
 */
public class ContactCardAdapter extends RecyclerView.Adapter<ContactCardAdapter.CardHolder> {

    private LayoutInflater inflater;
    private List<ContactEntity> friendEntities;

    public ContactCardAdapter(Context context, List<ContactEntity> entities) {
        this.inflater = LayoutInflater.from(context);
        this.friendEntities = entities;
    }

    @Override
    public CardHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_contactcard, parent, false);
        CardHolder cardHolder = new CardHolder(view);
        view.setOnClickListener(itemClickListener);
        return cardHolder;
    }

    @Override
    public void onBindViewHolder(CardHolder holder, int position) {
        ContactEntity entity = friendEntities.get(position);

        GlideUtil.loadAvatarRound(holder.roundimg, entity.getAvatar());
        String curName = entity.getName();
        holder.name.setText(curName);

        if (TextUtils.isEmpty(curName)) curName = "#";
        String curFirst = PinyinUtil.chatToPinyin(curName.charAt(0));

        if (position == 0) {
            holder.txt.setVisibility(View.VISIBLE);
            holder.txt.setText(curFirst);
        } else {
            ContactEntity lastEntity = friendEntities.get(position - 1);
            String lastName = lastEntity.getName();
            String lastFirst = PinyinUtil.chatToPinyin(lastName.charAt(0));

            if (lastFirst.equals(curFirst)) {
                holder.txt.setVisibility(View.GONE);
            } else {
                holder.txt.setVisibility(View.VISIBLE);
                holder.txt.setText(curFirst);
            }
        }
        holder.itemView.setTag(entity);
    }

    @Override
    public int getItemCount() {
        return friendEntities.size();
    }

    public int getPositionForSection(char selectchar) {
        for (int i = 0; i < friendEntities.size(); i++) {
            ContactEntity entity = friendEntities.get(i);
            String showName = entity.getName();
            String firstChar = PinyinUtil.chatToPinyin(showName.charAt(0));
            if (firstChar.charAt(0) >= selectchar) {
                return i;
            }
        }
        return -1;
    }

    private OnItemClickListener itemClickListener;

    public interface OnItemClickListener extends View.OnClickListener {
    }

    public void setItemClickListener(OnItemClickListener clickListener) {
        this.itemClickListener = clickListener;
    }

    static class CardHolder extends RecyclerView.ViewHolder {
        TextView txt;
        ImageView roundimg;
        TextView name;

        CardHolder(View view) {
            super(view);
            txt = (TextView) view.findViewById(R.id.txt);
            roundimg = (ImageView) view.findViewById(R.id.roundimg);
            name = (TextView) view.findViewById(R.id.tvName);
        }
    }
}
