package connect.activity.chat.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import connect.activity.base.BaseApplication;
import connect.activity.chat.exts.OuterWebsiteActivity;
import connect.activity.chat.subscribe.SubscribeMessageActivity;
import connect.activity.chat.view.BaseContainer;
import connect.database.green.bean.SubscribeDetailEntity;
import connect.ui.activity.R;
import connect.utils.TimeUtil;
import connect.utils.cryption.SupportKeyUril;
import connect.utils.glide.GlideUtil;
import instant.utils.StringUtil;
import protos.Connect;

/**
 * Created by Administrator on 2017/11/22.
 */
public class SubscribeMessageAdapter extends RecyclerView.Adapter<SubscribeMessageAdapter.SubscribeHolder> {

    private List<SubscribeDetailEntity> detailEntities = new ArrayList<>();
    private SubscribeMessageActivity activity;

    public SubscribeMessageAdapter(SubscribeMessageActivity activity) {
        this.activity = activity;
    }

    public void setData(List<SubscribeDetailEntity> detailEntities) {
        this.detailEntities = detailEntities;
        notifyDataSetChanged();
    }

    public void insertMoreEnties(List<SubscribeDetailEntity> detailEntities){
        this.detailEntities.addAll(0,detailEntities);
        notifyDataSetChanged();
    }

    public long lastMessageId(){
        if(detailEntities.size()==0){
            return 0;
        }
        SubscribeDetailEntity lastEntity =detailEntities.get(detailEntities.size()-1);
        return lastEntity.getMessageId();
    }

    @Override
    public int getItemViewType(int position) {
        SubscribeDetailEntity detailEntity = detailEntities.get(position);
        return detailEntity.getCategory();
    }

    @Override
    public SubscribeHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        SubscribeHolder subscribeHolder = null;
        if (viewType == 1) {//Rss
            BaseContainer container = new BaseContainer(activity, R.layout.item_subscribe_message_rss);
            subscribeHolder = new SubscribeRssHolder(container);
        } else {//Article
            BaseContainer container = new BaseContainer(activity, R.layout.item_subscribe_message_article);
            subscribeHolder = new SubscribeArtcleHolder(container);
        }
        return subscribeHolder;
    }

    @Override
    public void onBindViewHolder(SubscribeHolder holder, int position) {
        SubscribeDetailEntity detailEntity = detailEntities.get(position);

        switch (detailEntity.getCategory()) {
            case 1://Rss
                try {
                    byte[] content = StringUtil.hexStringToBytes(detailEntity.getContent());
                    Connect.RSSMessage rssMessage = Connect.RSSMessage.parseFrom(content);

                    SubscribeRssHolder rssHolder = (SubscribeRssHolder) holder;
                    rssHolder.showTimeTv.setText(activity.getString(R.string.Chat_Time) + ":" +
                            TimeUtil.getTime(rssMessage.getTime(), TimeUtil.DATE_FORMAT_MONTH_HOUR));
                    rssHolder.timeTv.setText(TimeUtil.getTime(rssMessage.getTime(), TimeUtil.DATE_FORMAT_MONTH_HOUR));
                    SpannableStringBuilder builder = new SpannableStringBuilder();
                    String contentTxt =TextUtils.isEmpty(rssMessage.getDetail())?"":rssMessage.getDetail();
                    SpannableStringBuilder stringBuilder = new SpannableStringBuilder(contentTxt);
                    builder.append(stringBuilder);

                    final String urlTxt = TextUtils.isEmpty(rssMessage.getSourceUrl()) ? "" : rssMessage.getSourceUrl();
                    if (!TextUtils.isEmpty(urlTxt)) {
                        SpannableStringBuilder colorBuilder = new SpannableStringBuilder(urlTxt);
                        Context context = BaseApplication.getInstance().getBaseContext();
                        ForegroundColorSpan colorSpan = new ForegroundColorSpan(context.getResources().getColor(R.color.color_blue));
                        colorBuilder.setSpan(colorSpan, 0, colorBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        builder.append(colorBuilder);
                    }
                    rssHolder.contentTv.setText(builder);
                    rssHolder.contentTv.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            OuterWebsiteActivity.startActivity(activity, urlTxt);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case 2://Article
                try {
                    byte[] content = StringUtil.hexStringToBytes(detailEntity.getContent());
                    Connect.Article article = Connect.Article.parseFrom(content);

                    SubscribeArtcleHolder artcleHolder = (SubscribeArtcleHolder) holder;
                    artcleHolder.showTimeTv.setText(TimeUtil.getTime(article.getTime(), TimeUtil.DATE_FORMAT_MONTH_HOUR));
                    artcleHolder.titleTv.setText(article.getTitle());
                    GlideUtil.loadImage(artcleHolder.roundimg, article.getImage());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    @Override
    public int getItemCount() {
        return detailEntities.size();
    }

    static class SubscribeHolder extends RecyclerView.ViewHolder {

        public SubscribeHolder(View itemView) {
            super(itemView);
        }
    }

    static class SubscribeArtcleHolder extends SubscribeHolder {

        TextView showTimeTv;
        TextView titleTv;
        ImageView roundimg;

        SubscribeArtcleHolder(View view) {
            super(view);
            showTimeTv = (TextView) view.findViewById(R.id.showtime);
            titleTv = (TextView) view.findViewById(R.id.txt1);
            roundimg = (ImageView) view.findViewById(R.id.roundimg);
        }
    }

    static class SubscribeRssHolder extends SubscribeHolder {

        TextView showTimeTv;
        TextView timeTv;
        TextView contentTv;

        SubscribeRssHolder(View view) {
            super(view);
            timeTv = (TextView) view.findViewById(R.id.showtime);
            showTimeTv = (TextView) view.findViewById(R.id.txt3);
            contentTv = (TextView) view.findViewById(R.id.txt2);
        }
    }
}
