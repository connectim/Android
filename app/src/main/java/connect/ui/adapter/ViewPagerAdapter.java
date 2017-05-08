package connect.ui.adapter;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 *
 * Created by Administrator on 2016/8/24.
 */
public class ViewPagerAdapter extends PagerAdapter {

    private List<View> list;

    public ViewPagerAdapter(List<View> list) {
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        System.gc();
        container.removeView((View) object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        container.addView(list.get(position));
        return list.get(position);
    }

}
