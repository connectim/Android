package connect.ui.activity.chat.model.emoji;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import connect.ui.activity.R;
import connect.ui.activity.chat.bean.StickPagerBean;
import connect.ui.activity.chat.model.EmoManager;
import connect.utils.glide.GlideUtil;

public class EmojiGridAdapter extends BaseAdapter {

    private Context context;
    private StickPagerBean stickBean = null;
    private List<String> stickers = null;

    public EmojiGridAdapter(Context mContext, StickPagerBean stick) {
        this.context = mContext;
        this.stickBean = stick;

        this.stickers = new ArrayList<>();
        this.stickers.addAll(stickBean.getStrings());
        this.stickers.add("DEL");
    }

    public int getCount() {
        return stickers.size();
    }

    @Override
    public Object getItem(int position) {
        String noSuffix = stickers.get(position).replace(".png", "");
        return "[" + noSuffix + "]";
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        EmjHolder holder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_chat_emoji, null);
            holder = new EmjHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (EmjHolder) convertView.getTag();
        }
        String iconName=stickers.get(position);
        if (iconName.equals("DEL")) {
            GlideUtil.loadImage(holder.img, R.mipmap.emoji_del);
        } else {
            GlideUtil.loadImageAssets(holder.img, EmoManager.EMOJI_PATH + File.separator + stickBean.getName() + File.separator + iconName);
        }
        return convertView;
    }

    static class EmjHolder {
        @Bind(R.id.img)
        ImageView img;

        EmjHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}