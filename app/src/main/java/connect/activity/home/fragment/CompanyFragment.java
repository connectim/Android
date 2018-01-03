package connect.activity.home.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.protobuf.ByteString;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import connect.activity.base.BaseFragment;
import connect.activity.company.DepartmentActivity;
import connect.activity.home.adapter.CompanyAdapter;
import connect.activity.home.view.LineDecoration;
import connect.ui.activity.R;
import connect.utils.UriUtil;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import connect.widget.TopToolBar;
import protos.Connect;

/**
 * Created by Administrator on 2018/1/3 0003.
 */

public class CompanyFragment extends BaseFragment {

    @Bind(R.id.toolbar_top)
    TopToolBar toolbarTop;
    @Bind(R.id.recyclerview)
    RecyclerView recyclerview;

    private FragmentActivity mActivity;
    private CompanyAdapter adapter;

    public static CompanyFragment startFragment() {
        CompanyFragment companyFragment = new CompanyFragment();
        return companyFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_company, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        initView();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mActivity = getActivity();
    }

    private void initView() {
        toolbarTop.setBlackStyle();
        toolbarTop.setTitle(null, R.string.Company_company);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mActivity);
        recyclerview.setLayoutManager(linearLayoutManager);
        recyclerview.addItemDecoration(new LineDecoration(mActivity));
        adapter = new CompanyAdapter(mActivity);
        adapter.setItemClickListener(onItemClickListener);
        recyclerview.setAdapter(adapter);
        requestWorkmate();
    }

    CompanyAdapter.OnItemClickListener onItemClickListener = new CompanyAdapter.OnItemClickListener(){
        @Override
        public void itemClick(ArrayList<Connect.Workmate> itemList) {
            DepartmentActivity.lunchActivity(mActivity, itemList);
        }
    };

    private void requestWorkmate(){
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.CONNECT_V3_SYNC_WORKMATE,
                ByteString.copyFrom(new byte[]{}), new ResultCall<Connect.HttpNotSignResponse>() {
            @Override
            public void onResponse(Connect.HttpNotSignResponse response) {
                try{
                    Connect.StructData structData = Connect.StructData.parseFrom(response.getBody());
                    Connect.Workmates workmates = Connect.Workmates .parseFrom(structData.getPlainData());
                    sortList(workmates);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Connect.HttpNotSignResponse response) {
                int a = 1;
            }
        });
    }

    private void sortList(Connect.Workmates workmates){
        ArrayList<ArrayList<Connect.Workmate>> list = new ArrayList<>();
        for(Connect.Workmate workmate : workmates.getListList()){
            boolean isHave = false;
            for(ArrayList<Connect.Workmate> itemList : list){
                if(itemList != null && itemList.get(0).getOU().equals(workmate.getOU())){
                    itemList.add(workmate);
                    isHave = true;
                    break;
                }
            }
            if(!isHave){
                ArrayList<Connect.Workmate> itemList = new ArrayList<>();
                itemList.add(workmate);
                list.add(itemList);
            }
        }
        adapter.setNotify(list);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }
}
