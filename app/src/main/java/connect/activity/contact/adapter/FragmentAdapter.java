package connect.activity.contact.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.List;

public class FragmentAdapter extends FragmentStatePagerAdapter {
    private List<Fragment> mFragments;
    private List<String> mTitles;
    private int mChildCount = 0;
    private boolean POSITION = false;
    public FragmentAdapter(FragmentManager fm, List<Fragment> fragments, List<String> titles) {
        super(fm);
        this.mFragments = fragments;
        this.mTitles = titles;
        mChildCount = fragments == null ? 0 :fragments.size();
    }
    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    public void refresh(List<Fragment> fragments, List<String> titles, boolean none){
        mFragments = fragments;
        mTitles = titles;
        mChildCount = fragments == null ? 0 :fragments.size();
        POSITION = none;
        notifyDataSetChanged();
    }

    public void setPOSITION(boolean POSITION) {
        this.POSITION = POSITION;
    }

    @Override
    public Fragment getItem(int position) {
        return mFragments == null ? null : mFragments.get(position);
    }

    @Override
    public int getCount() {
        return mFragments == null ? 0 : mFragments.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mTitles.get(position);
    }
}
