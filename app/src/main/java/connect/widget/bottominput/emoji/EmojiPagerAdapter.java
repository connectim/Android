package connect.widget.bottominput.emoji;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import connect.widget.bottominput.bean.StickPagerBean;
import connect.activity.chat.inter.IEmojiClickListener;
import connect.utils.system.SystemUtil;

import java.util.List;

/**
 * Created by gtq on 2016/11/25.
 */
public class EmojiPagerAdapter extends PagerAdapter {

    private Context context;
    private IEmojiClickListener iEmojiClickListener;
    private List<StickPagerBean> stickPagers;

    public EmojiPagerAdapter(Context context, List<StickPagerBean> stickPagers) {
        this.context = context;
        this.stickPagers = stickPagers;
    }

    @Override
    public int getCount() {
        return stickPagers.size();
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        StickPagerBean stickbean = stickPagers.get(position);

        GridView gridView = null;
        if (stickbean.isBigStick()) {//big expression
            gridView = new GridView(context);
            gridView.setPadding(10, SystemUtil.dipToPx(20), 10, 0);
            gridView.setAdapter(new EmotionGridAdapter(context, stickbean, iEmojiClickListener));
            gridView.setNumColumns(5);
            gridView.setHorizontalSpacing(5);
            gridView.setGravity(Gravity.CENTER);
            container.addView(gridView);
            return gridView;
        } else {//small expression
            gridView = new GridView(context);
            gridView.setAdapter(new EmojiGridAdapter(context, stickbean));
            gridView.setNumColumns(7);
            gridView.setHorizontalSpacing(5);
            gridView.setVerticalSpacing(5);
            gridView.setGravity(Gravity.CENTER);
            gridView.setOnItemClickListener(onEmojiClick);
            container.addView(gridView);
            return gridView;
        }
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        View layout = (View) object;
        container.removeView(layout);
    }

    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    /**
     * click small expression
     */
    AdapterView.OnItemClickListener onEmojiClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Object object = parent.getItemAtPosition(position);
            iEmojiClickListener.onEmjClick((String) object);
        }
    };

    public void setiEmojiClickListener(IEmojiClickListener iEmojiClickListener) {
        this.iEmojiClickListener = iEmojiClickListener;
    }
}