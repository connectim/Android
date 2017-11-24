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

import com.google.protobuf.ByteString;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import connect.activity.contact.adapter.RecommendAdapter;
import connect.activity.contact.adapter.SubscribeMarketCapAdapter;
import connect.activity.contact.adapter.SubscribeMarketItemAdapter;
import connect.activity.home.view.LineDecoration;
import connect.ui.activity.R;
import connect.utils.UriUtil;
import connect.utils.cryption.DecryptionUtil;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import protos.Connect;

/**
 * Created by Administrator on 2017/11/23 0023.
 */

public class SubscribeMarketFragment extends Fragment {


    @Bind(R.id.recyclerview)
    RecyclerView recyclerview;
    @Bind(R.id.refreshview)
    SwipeRefreshLayout refreshview;

    private Activity mActivity;
    private SubscribeMarketItemAdapter adapter;
    private Connect.Exchange exchange;
    private SubscribeMarketCapAdapter adapterCap;

    public static SubscribeMarketFragment newInstance(Connect.Exchange data) {
        SubscribeMarketFragment fragment = new SubscribeMarketFragment();
        Bundle args = new Bundle();
        args.putSerializable("data", data);
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
            exchange = (Connect.Exchange) getArguments().getSerializable("data");
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
        if(exchange.getName().equals("0")){
            adapterCap = new SubscribeMarketCapAdapter(mActivity);
            recyclerview.setAdapter(adapterCap);
            getCapitalizations();
        }else{
            adapter = new SubscribeMarketItemAdapter(mActivity);
            recyclerview.setAdapter(adapter);
            getMarketTicker(exchange.getIdentifier());
        }

    }

    SwipeRefreshLayout.OnRefreshListener onRefreshListener = new SwipeRefreshLayout.OnRefreshListener(){
        @Override
        public void onRefresh() {
            getMarketTicker(exchange.getIdentifier());
        }
    };

    private void getCapitalizations(){
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.CONNECT_V2_MARKET_CAPITALIZATIONS, ByteString.copyFrom(new byte[]{}), new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                refreshview.setRefreshing(false);
                try{
                    Connect.IMResponse imResponse = Connect.IMResponse.parseFrom(response.getBody().toByteArray());
                    Connect.StructData structData = DecryptionUtil.decodeAESGCMStructData(imResponse.getCipherData());
                    Connect.Capitalizations  capitalizations = Connect.Capitalizations.parseFrom(structData.getPlainData());
                    List<Connect.Capitalization> list = capitalizations.getListList();
                    adapterCap.setNotify(list);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Connect.HttpResponse response) {
                refreshview.setRefreshing(false);
            }
        });
    }

    private void getMarketTicker(String id){
        String url = String.format(UriUtil.CONNECT_V2_MARKET_ID, id);
        OkHttpUtil.getInstance().postEncrySelf(url, ByteString.copyFrom(new byte[]{}), new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                if(refreshview != null)
                    refreshview.setRefreshing(false);
                try{
                    Connect.IMResponse imResponse = Connect.IMResponse.parseFrom(response.getBody().toByteArray());
                    Connect.StructData structData = DecryptionUtil.decodeAESGCMStructData(imResponse.getCipherData());
                    Connect.Tickers tickers = Connect.Tickers.parseFrom(structData.getPlainData());
                    List<Connect.Ticker> list = tickers.getListList();
                    adapter.setNotify(list);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Connect.HttpResponse response) {
                refreshview.setRefreshing(false);
            }
        });
    }

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
