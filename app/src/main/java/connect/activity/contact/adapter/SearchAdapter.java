package connect.activity.contact.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import connect.database.green.bean.ContactEntity;
import connect.ui.activity.R;
import connect.utils.glide.GlideUtil;

import static connect.activity.contact.adapter.SearchAdapter.ViewType.VIEW_TYP_NOSEARCHS;

public class SearchAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Activity activity;
    private ArrayList<ContactEntity> mDataList = new ArrayList<>();
    private OnItemClickListener onItemClickListener;

    public enum ViewType {
        VIEW_TYP_NOSEARCHS,
        VIEW_TYP_SERVER,
        VIEW_TYP_LOCAL
    }

    public SearchAdapter(Activity activity) {
        this.activity = activity;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(activity);
        View view = null;
        RecyclerView.ViewHolder viewHolder = null;
        if (viewType == ViewType.VIEW_TYP_NOSEARCHS.ordinal()) {
            view = inflater.inflate(R.layout.item_contact_search_default, parent, false);
            viewHolder = new NosearchHolder(view);
        } else if (viewType == ViewType.VIEW_TYP_SERVER.ordinal()) {
            view = inflater.inflate(R.layout.item_contact_search_server, parent, false);
            viewHolder = new ServerHolder(view);
        } else if (viewType == ViewType.VIEW_TYP_LOCAL.ordinal()) {
            view = inflater.inflate(R.layout.item_contact_search_local, parent, false);
            viewHolder = new LocalHolder(view);
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        int viewType = getItemViewType(position);
        if (mDataList != null && mDataList.size() > 0) {
            final ContactEntity friendEntity = mDataList.get(position);
            if (viewType == ViewType.VIEW_TYP_NOSEARCHS.ordinal()) {

            } else if (viewType == ViewType.VIEW_TYP_SERVER.ordinal()) {
                ((ServerHolder)holder).nameTv.setText(friendEntity.getConnectId());
                ((ServerHolder)holder).contentRela.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onItemClickListener.itemClick(position, mDataList.get(position), 1);
                    }
                });
            } else if (viewType == ViewType.VIEW_TYP_LOCAL.ordinal()) {
                if (position > 0 && TextUtils.isEmpty(mDataList.get(position - 1).getUsername())) {
                    ((LocalHolder)holder).txt.setVisibility(View.VISIBLE);
                    ((LocalHolder)holder).txt.setText(R.string.Link_Contacts);
                } else {
                    ((LocalHolder)holder).txt.setVisibility(View.GONE);
                }
                GlideUtil.loadAvatarRound(((LocalHolder)holder).avatar, mDataList.get(position).getAvatar());
                ((LocalHolder)holder).nameTv.setText(mDataList.get(position).getUsername());
                ((LocalHolder)holder).contentRela.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onItemClickListener.itemClick(position, mDataList.get(position), 2);
                    }
                });
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (!TextUtils.isEmpty(mDataList.get(position).getUsername())) {
            return ViewType.VIEW_TYP_LOCAL.ordinal();
        } else if (!TextUtils.isEmpty(mDataList.get(position).getUid())) {
            return ViewType.VIEW_TYP_SERVER.ordinal();
        } else {
            return VIEW_TYP_NOSEARCHS.ordinal();
        }
    }

    class NosearchHolder extends RecyclerView.ViewHolder {

        public NosearchHolder(View itemView) {
            super(itemView);
        }
    }

    class ServerHolder extends RecyclerView.ViewHolder {

        TextView nameTv;
        RelativeLayout contentRela;

        public ServerHolder(View itemView) {
            super(itemView);
            contentRela = (RelativeLayout) itemView.findViewById(R.id.content_rela);
            nameTv = (TextView) itemView.findViewById(R.id.name_tv);
        }
    }

    class LocalHolder extends RecyclerView.ViewHolder {

        TextView nameTv;
        TextView txt;
        ImageView avatar;
        RelativeLayout contentRela;

        public LocalHolder(View itemView) {
            super(itemView);
            contentRela = (RelativeLayout) itemView.findViewById(R.id.content_rela);
            nameTv = (TextView) itemView.findViewById(R.id.name_tv);
            txt = (TextView) itemView.findViewById(R.id.txt);
            avatar = (ImageView) itemView.findViewById(R.id.avatar_rimg);
        }
    }

    public void setDataNotify(List<ContactEntity> list) {
        mDataList.clear();
        mDataList.addAll(list);
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {

        void itemClick(int position,ContactEntity list,int type);

    }

}
