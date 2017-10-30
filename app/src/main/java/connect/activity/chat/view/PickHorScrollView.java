package connect.activity.chat.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import connect.activity.chat.adapter.PickHoriScrollAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gtq on 2016/11/28.
 */
public class PickHorScrollView extends HorizontalScrollView {

    private PickHoriScrollAdapter pickAdapter;
    private LinearLayout linearLayout;

    public PickHorScrollView(Context context) {
        super(context);
    }

    public PickHorScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PickHorScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    protected void initView(){

    }

    public void setPickAdapter(PickHoriScrollAdapter adapter) {
        this.pickAdapter = adapter;
        linearLayout = (LinearLayout) getChildAt(0);

        for (int i = 0; i < adapter.getCount(); i++) {
            View view = adapter.getView(i, null, linearLayout);
            view.setOnClickListener(listener);
            view.setTag(adapter.getItemObj(i));
            linearLayout.addView(view);
        }
    }

    private List<String> clickLists = new ArrayList<>();

    public List<String> getClickLists(){
        return clickLists;
    }

    OnClickListener listener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            String path = (String) v.getTag();
            if (v.isSelected()) {
                v.setSelected(false);
                clickLists.remove(path);
            } else {
                v.setSelected(true);
                clickLists.add(path);
            }
            itemClickListener.itemOnClick(clickLists);
        }
    };

    private OnItemClickListener itemClickListener;

    public interface OnItemClickListener {
        void itemOnClick(List<String> paths);
    }

    public void setItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
    }
}