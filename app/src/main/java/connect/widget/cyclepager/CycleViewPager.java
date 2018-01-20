package connect.widget.cyclepager;

import android.content.Context;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

import connect.ui.activity.R;
import connect.utils.glide.GlideUtil;
import connect.utils.system.SystemUtil;
import protos.Connect;

/**
 * 轮滚播放器
 */
public class CycleViewPager extends RelativeLayout{

    private ViewPager viewPager;
    /** 底部圆点父容器 */
    private LinearLayout linDot;
    /** 自动轮滚控制器 */
    private CyclePagerHandler pagerHandler;
    /** 填充数据集合 */
    private ArrayList<Connect.Banner> listData;
    /** ViewPage适配器 */
    private MyPagerAdapter myPagerAdapter;
    /** item点击回调 */
    private ItemSyscleClickListener itemSyscleClickListener = null;

    public CycleViewPager(Context context) {
        super(context);
    }

    public CycleViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        View view = LayoutInflater.from(context).inflate(R.layout.cycle_viewpage, this, true);
        initView(context,view);
    }

    private void initView(Context context,View view) {
        viewPager = (ViewPager)view.findViewById(R.id.view_pager);
        linDot = (LinearLayout)view.findViewById(R.id.lin_dot);
        pagerHandler = new CyclePagerHandler(viewPager);
        pagerHandler.sendEmptyMessageDelayed(1, 3000);
        listData = new ArrayList<Connect.Banner>();
        myPagerAdapter = new MyPagerAdapter(listData,pagerHandler,context);
        viewPager.setAdapter(myPagerAdapter);
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int postion) {
                if(listData.size() > 1){
                    for (int i = 0; i < listData.size(); i++) {
                        ImageView img = (ImageView) linDot.getChildAt(i);
                        if (i == postion % listData.size()) {
                            img.setImageResource(R.drawable.shape_guide_point_white);
                        } else {
                            img.setImageResource(R.drawable.shape_guide_point_838383);
                        }
                    }
                }
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {}
            @Override
            public void onPageScrollStateChanged(int arg0) {}
        });
    }

    /** 通知跟新数据 */
    public void notifyData(Context mContext, List<Connect.Banner> listValue){
        listData.clear();
        listData.addAll(listValue);
        if(listData.size() > 1){
            pagerHandler.setStart(true);
        }else{
            pagerHandler.setStart(false);
        }
        initDot(mContext);
        myPagerAdapter.notifyDataSetChanged();
    }

    /** 初始化更新底部圆点 */
    private void initDot(Context mContext) {
        linDot.removeAllViews();
        if(listData.size() == 1){
            return;
        }
        for (int i = 0; i < listData.size(); i++) {
            ImageView img = new ImageView(mContext);
            if (i == 0) {
                img.setImageResource(R.drawable.shape_guide_point_white);
            } else {
                img.setImageResource(R.drawable.shape_guide_point_838383);
            }
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(SystemUtil.dipToPx(8), SystemUtil.dipToPx(8));
            params.leftMargin = SystemUtil.dipToPx(8);
            img.setLayoutParams(params);
            linDot.addView(img);
        }
    }

    class MyPagerAdapter extends PagerAdapter {
        ArrayList<Connect.Banner> listData;
        Context context;
        Handler handler;

        public MyPagerAdapter(ArrayList listData, Handler handler, Context context) {
            this.listData = listData;
            this.handler = handler;
            this.context = context;
        }

        @Override
        public int getCount() {
            if(listData.size() > 1){
                return Integer.MAX_VALUE;
            }else{
                return listData.size();
            }
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

        @Override
        public Object instantiateItem(ViewGroup container,final int position) {
            View v = View.inflate(context, R.layout.view_cycle_page_item, null);
            ImageView img = (ImageView) v.findViewById(R.id.image);
            /** 给img添加触摸监听 当Image有Touch Event时停止轮滚 */
            img.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            handler.removeCallbacksAndMessages(null);
                            break;
                        case MotionEvent.ACTION_UP:
                            if(itemSyscleClickListener != null){
                                itemSyscleClickListener.itemClickL(listData.get(position % listData.size()),position % listData.size());
                            }
                        case MotionEvent.ACTION_CANCEL:
                            handler.sendEmptyMessageDelayed(CyclePagerHandler.CODE, CyclePagerHandler.CYCLE_TIME);
                            break;
                    }
                    return true;
                }
            });

            String uri = listData.get(position % listData.size()).getImg();
            GlideUtil.loadImage(img,uri);
            container.addView(v);
            return v;
        }


        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            // TODO Auto-generated method stub
            container.removeView((View) object);
        }
    }

    /**
     * 设置Item点击监听
     */
    public void setOnItemClickListener(ItemSyscleClickListener itemListener){
        itemSyscleClickListener = itemListener;
    }

    /**
     * 界面切换时停止轮滚
     */
    public void setStart(Boolean isStart){
        pagerHandler.setStart(isStart);
        if(!isStart){
            pagerHandler.removeMessages(CyclePagerHandler.CODE);
        }
    }

    /** item 点击回调 */
    public interface ItemSyscleClickListener {

        void itemClickL(Connect.Banner banner, int position);

    }

}
