package connect.ui.activity.contact.adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import connect.db.green.bean.ContactEntity;
import connect.ui.activity.R;
import connect.utils.glide.GlideUtil;
import connect.view.roundedimageview.RoundedImageView;

/**
 * Created by Administrator on 2017/1/3.
 */
public class SearchAdapter extends BaseAdapter {

    private ArrayList<ContactEntity> mDataList = new ArrayList<>();
    private final int VIEW_TYP_NOSEARCHS = 100;
    private final int VIEW_TYP_SERVER = 101;
    private final int VIEW_TYP_LOCAL = 102;
    private OnItemClickListence onItemClickListence;

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
    public int getItemViewType(int position) {
        if (!TextUtils.isEmpty(mDataList.get(position).getUsername())) {
            return VIEW_TYP_LOCAL;
        } else if (!TextUtils.isEmpty(mDataList.get(position).getAddress())) {
            return VIEW_TYP_SERVER;
        } else {
            return VIEW_TYP_NOSEARCHS;
        }
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        int type = getItemViewType(position);
        int tmp = 0;
        if (convertView != null) {
            tmp = (Integer) convertView.getTag(R.id.status_key);
        }
        if (convertView == null || tmp != type) {
            holder = new ViewHolder();
            switch (type) {
                case VIEW_TYP_NOSEARCHS:
                    convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact_search_default, parent, false);
                    break;
                case VIEW_TYP_SERVER:
                    convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact_search_server, parent, false);
                    holder.contentRela = (RelativeLayout)convertView.findViewById(R.id.content_rela);
                    holder.nameTv = (TextView)convertView.findViewById(R.id.name_tv);
                    break;
                case VIEW_TYP_LOCAL:
                    convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact_search_local, parent, false);
                    holder.contentRela = (RelativeLayout)convertView.findViewById(R.id.content_rela);
                    holder.nameTv = (TextView)convertView.findViewById(R.id.name_tv);
                    holder.txt = (TextView)convertView.findViewById(R.id.txt);
                    holder.avater = (RoundedImageView)convertView.findViewById(R.id.avatar_rimg);
                    break;
            }
            convertView.setTag(holder);
            convertView.setTag(R.id.status_key, type);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (mDataList != null && mDataList.size() > 0) {
            final ContactEntity friendEntity = mDataList.get(position);
            switch (type) {
                case VIEW_TYP_NOSEARCHS:
                    break;
                case VIEW_TYP_SERVER:
                    holder.nameTv.setText(friendEntity.getAddress());
                    holder.contentRela.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onItemClickListence.itemClick(position,mDataList.get(position),1);
                        }
                    });
                    break;
                case VIEW_TYP_LOCAL:
                    if(position > 0 && TextUtils.isEmpty(mDataList.get(position-1).getUsername())){
                        holder.txt.setVisibility(View.VISIBLE);
                        holder.txt.setText(R.string.Link_Contacts);
                    }else{
                        holder.txt.setVisibility(View.GONE);
                    }
                    GlideUtil.loadAvater(holder.avater,mDataList.get(position).getAvatar());
                    holder.nameTv.setText(mDataList.get(position).getUsername());
                    holder.contentRela.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onItemClickListence.itemClick(position,mDataList.get(position),2);
                        }
                    });
                    break;
            }
        }

        return convertView;
    }

    private static class ViewHolder {
        TextView nameTv;
        TextView txt;
        RoundedImageView avater;
        RelativeLayout contentRela;
    }

    public void setDataNotify(List<ContactEntity> list) {
        mDataList.clear();
        mDataList.addAll(list);
        notifyDataSetChanged();
    }

    public void setOnItemClickListence(OnItemClickListence onItemClickListence){
        this.onItemClickListence = onItemClickListence;
    }

    public interface OnItemClickListence {

        void itemClick(int position,ContactEntity list,int type);

    }

}
