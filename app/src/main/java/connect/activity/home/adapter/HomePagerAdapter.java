package connect.activity.home.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

import connect.activity.base.BaseFragment;

/**
 * Created by PuJin on 2018/2/5.
 */

public class HomePagerAdapter extends FragmentPagerAdapter {

    private List<BaseFragment> baseFragments = null;

    public HomePagerAdapter(FragmentManager fm, List<BaseFragment> fragments) {
        super(fm);
        this.baseFragments = fragments;
    }

    @Override
    public Fragment getItem(int position) {
        return baseFragments.get(position);
    }

    @Override
    public int getCount() {
        return baseFragments.size();
    }
}
