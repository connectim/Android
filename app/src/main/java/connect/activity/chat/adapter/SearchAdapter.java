package connect.activity.chat.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import connect.activity.chat.fragment.bean.SearchBean;
import connect.ui.activity.R;
import connect.utils.glide.GlideUtil;

/**
 * Created by Administrator on 2018/1/31 0031.
 */

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {

    private final Activity activity;
    private ArrayList<SearchBean> mDataList = new ArrayList<>();
    private OnItemChildClickListener childListener;

    public SearchAdapter(Activity activity) {
        this.activity = activity;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(activity);
        View view = inflater.inflate(R.layout.item_chat_search, parent, false);
        SearchAdapter.ViewHolder holder = new SearchAdapter.ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final SearchBean searchBean = mDataList.get(position);
        if(position == 0 || mDataList.get(position - 1).getStyle() != searchBean.getStyle()){
            if(position == 0){
                holder.topLine.setVisibility(View.GONE);
            }else{
                holder.topLine.setVisibility(View.VISIBLE);
            }
            holder.titleText.setVisibility(View.VISIBLE);
            setTitleText(holder.titleText, searchBean.getStyle());
        }else{
            holder.topLine.setVisibility(View.GONE);
            holder.titleText.setVisibility(View.GONE);
        }

        if(searchBean.getStyle() == 1){
            holder.hintTv.setVisibility(View.GONE);
        }else if(searchBean.getStyle() == 2){
            holder.hintTv.setVisibility(View.VISIBLE);
            holder.hintTv.setText(activity.getString(R.string.Link_Contains, searchBean.getSearchStr()));
        }else if(searchBean.getStyle() == 3){
            holder.hintTv.setVisibility(View.VISIBLE);
            holder.hintTv.setText(activity.getString(R.string.Link_Relevant_record, searchBean.getHinit()));
        }

        GlideUtil.loadAvatarRound(holder.avatarImage, searchBean.getAvatar());
        holder.nicknameTv.setText(searchBean.getName());
        holder.contentLinear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                childListener.itemClick(position, searchBean);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {


        private final View topLine;
        private final TextView titleText;
        private final LinearLayout contentLinear;
        private final ImageView avatarImage;
        private final TextView nicknameTv;
        private final TextView hintTv;

        ViewHolder(View itemView) {
            super(itemView);
            topLine = itemView.findViewById(R.id.top_line);
            titleText = (TextView) itemView.findViewById(R.id.title_text);
            contentLinear = (LinearLayout) itemView.findViewById(R.id.content_linear);
            avatarImage = (ImageView) itemView.findViewById(R.id.avatar_image);
            nicknameTv = (TextView) itemView.findViewById(R.id.nickname_tv);
            hintTv = (TextView) itemView.findViewById(R.id.hint_tv);
        }
    }

    private void setTitleText(TextView text, int style){
        if(style == 1){
            text.setText(R.string.Link_Contacts);
        } else if(style == 2){
            text.setText(R.string.Chat_Group_chat);
        } else if(style == 3){
            text.setText(R.string.Link_Chat_record);
        }
    }

    public void setDataNotify(ArrayList<SearchBean> list){
        mDataList.clear();
        mDataList.addAll(list);
        notifyDataSetChanged();
    }

    public void setOnItemChildListence(OnItemChildClickListener childListener){
        this.childListener = childListener;
    }

    public interface OnItemChildClickListener {
        void itemClick(int position, SearchBean searchBean);
    }
}
