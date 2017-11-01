package connect.widget.bottominput.emoji;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import connect.ui.activity.R;
import connect.widget.bottominput.EmoManager;
import connect.widget.bottominput.bean.StickPagerBean;
import connect.widget.bottominput.inter.IEmojiClickListener;
import connect.activity.chat.view.PopWindowImg;
import connect.utils.FileUtil;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import connect.utils.glide.GlideUtil;

public class EmotionGridAdapter extends BaseAdapter {

    private Context context;
    private StickPagerBean stickBean = null;
    private IEmojiClickListener itemClickListener;
    private List<String> stickers = null;

    public EmotionGridAdapter(Context mContext, StickPagerBean stick, IEmojiClickListener listener) {
        this.context = mContext;
        this.stickBean = stick;
        this.itemClickListener = listener;
        this.stickers = stickBean.getStrings();
    }

    @Override
    public int getCount() {
        return stickers.size();
    }

    @Override
    public Object getItem(int position) {
        return stickers.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        EmoViewHolder holder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_chat_emotion, null);
            holder = new EmoViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (EmoViewHolder) convertView.getTag();
        }

        String path = EmoManager.EMOJI_PATH + "/" + stickBean.getName() + "/" + stickers.get(position);
        String gif = EmoManager.GIF_PATH + "/" + FileUtil.subExtentsion(stickers.get(position)) + ".gif";
        GlideUtil.loadImageAssets(holder.img, path);
        holder.img.setGifPath(gif);
        holder.img.setPopListener(new PopWindowImg.IPopWindowListener() {
            @Override
            public void OnClick(String filePath) {
                filePath = filePath.replace(".gif", "");
                itemClickListener.onEmtClick(filePath);
            }
        });
        return convertView;
    }

    static class EmoViewHolder {
        @Bind(R.id.img)
        PopWindowImg img;

        EmoViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}