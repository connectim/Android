package connect.activity.contact;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import connect.activity.contact.adapter.RecommendAdapter;
import connect.activity.contact.adapter.SubscribeMarketItemAdapter;
import connect.activity.home.view.LineDecoration;
import connect.ui.activity.R;

/**
 * Created by Administrator on 2017/11/23 0023.
 */

public class SubscribeMarketFragment extends Fragment {


    @Bind(R.id.recyclerview)
    RecyclerView recyclerview;
    @Bind(R.id.refreshview)
    SwipeRefreshLayout refreshview;

    private Activity mActivity;
    private String name;
    private SubscribeMarketItemAdapter adapter;

    public static SubscribeMarketFragment newInstance(String name) {
        SubscribeMarketFragment fragment = new SubscribeMarketFragment();
        Bundle args = new Bundle();
        args.putSerializable("data", name);
        fragment.setArguments(args);
        return fragment;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_subscribe_market, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getArguments() != null) {
            name = (String) getArguments().getSerializable("data");
        }
        this.mActivity = getActivity();
        initListView();
    }

    private void initListView() {
        refreshview.setColorSchemeResources(R.color.color_ebecee, R.color.color_c8ccd5, R.color.color_lightgray);
        refreshview.setOnRefreshListener(onRefreshListener);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mActivity);
        recyclerview.setLayoutManager(linearLayoutManager);
        recyclerview.addItemDecoration(new LineDecoration(mActivity));
        adapter = new SubscribeMarketItemAdapter(mActivity);
        recyclerview.setAdapter(adapter);

        ArrayList<String> list = new ArrayList<>();
        list.add(name + "1");
        list.add(name + "2");
        list.add(name + "3");
        list.add(name + "4");
        list.add(name + "5");
        adapter.setNotify(list);
    }

    SwipeRefreshLayout.OnRefreshListener onRefreshListener = new SwipeRefreshLayout.OnRefreshListener(){
        @Override
        public void onRefresh() {

        }
    };

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
