package connect.activity.home.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.protobuf.ByteString;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import connect.activity.base.BaseFragment;
import connect.activity.company.adapter.DepartmentBean;
import connect.activity.home.adapter.WorkbenchMenuAdapter;
import connect.activity.workbench.VisitorsActivity;
import connect.activity.workbench.data.MenuBean;
import connect.activity.workbench.data.MenuData;
import connect.ui.activity.R;
import connect.utils.ToastEUtil;
import connect.utils.ToastUtil;
import connect.utils.UriUtil;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import connect.widget.cyclepager.CycleViewPager;
import protos.Connect;

/**
 * Created by Administrator on 2018/1/15 0015.
 */

public class WorkbenchFragment extends BaseFragment {

    @Bind(R.id.search_relative)
    RelativeLayout searchRelative;
    @Bind(R.id.manager_tv)
    TextView managerTv;
    @Bind(R.id.cycle_ViewPager)
    CycleViewPager cycleViewPager;
    @Bind(R.id.application_menu_recycler)
    RecyclerView applicationMenuRecycler;
    @Bind(R.id.my_menu_recycler)
    RecyclerView myMenuRecycler;

    private FragmentActivity activity;
    private WorkbenchMenuAdapter appMenuAdapter;
    private WorkbenchMenuAdapter myAppMenuAdapter;

    public static WorkbenchFragment startFragment() {
        WorkbenchFragment workbenchFragment = new WorkbenchFragment();
        return workbenchFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_workbench, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        activity = getActivity();
        initView();
    }

    private void initView() {
        initCyclePager();
        initAppMune();
        initMyAppMune();
    }

    private void initCyclePager(){
        cycleViewPager.setOnItemClickListener(new CycleViewPager.ItemSyscleClickListener() {
            @Override
            public void itemClickL(Connect.Banner banner, int position) {
                ToastUtil.getInstance().showToast("ddianjile" + position);
            }
        });
        getBanners();
    }

    private void initAppMune(){
        appMenuAdapter = new WorkbenchMenuAdapter(activity);
        appMenuAdapter.setOnItemClickListence(onItemMuneClickListener);
        applicationMenuRecycler.setLayoutManager(new GridLayoutManager(activity, 4));
        applicationMenuRecycler.setAdapter(appMenuAdapter);
        getApplication();
    }

    private void initMyAppMune(){
        ArrayList<MenuBean> mymenuList = new ArrayList<>();
        mymenuList.add(MenuData.getInstance().getData("visitors"));
        mymenuList.add(MenuData.getInstance().getData("add"));
        myAppMenuAdapter = new WorkbenchMenuAdapter(activity);
        myAppMenuAdapter.setOnItemClickListence(onItemMyMuneClickListener);
        myMenuRecycler.setLayoutManager(new GridLayoutManager(activity, 4));
        myMenuRecycler.setAdapter(myAppMenuAdapter);
        myAppMenuAdapter.setNotify(mymenuList);
    }

    WorkbenchMenuAdapter.OnItemMuneClickListener onItemMuneClickListener = new WorkbenchMenuAdapter.OnItemMuneClickListener() {
        @Override
        public void itemClick(int position, MenuBean item) {

        }
    };

    WorkbenchMenuAdapter.OnItemMuneClickListener onItemMyMuneClickListener = new WorkbenchMenuAdapter.OnItemMuneClickListener() {
        @Override
        public void itemClick(int position, MenuBean item) {
            if(item.getCode().equals("visitors")){
                VisitorsActivity.lunchActivity(activity);
            }
        }
    };

    private void getBanners(){
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.CONNECT_V3_API_BANNERS,
                ByteString.copyFrom(new byte[]{}), new ResultCall<Connect.HttpNotSignResponse>() {
            @Override
            public void onResponse(Connect.HttpNotSignResponse response) {
                try {
                    Connect.StructData structData = Connect.StructData.parseFrom(response.getBody());
                    Connect.Banners banners = Connect.Banners.parseFrom(structData.getPlainData());
                    List<Connect.Banner> list = banners.getListList();
                    cycleViewPager.notifyData(activity, list);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Connect.HttpNotSignResponse response) {}
        });
    }

    private void getApplication(){
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.CONNECT_V3_API_APPLICATIONS,
                ByteString.copyFrom(new byte[]{}), new ResultCall<Connect.HttpNotSignResponse>() {
                    @Override
                    public void onResponse(Connect.HttpNotSignResponse response) {
                        try {
                            Connect.StructData structData = Connect.StructData.parseFrom(response.getBody());
                            Connect.Applications applications = Connect.Applications.parseFrom(structData.getPlainData());
                            List<Connect.Application> list = applications.getListList();
                            ArrayList<MenuBean> listMenu = new ArrayList<>();
                            for(Connect.Application application : list){
                                MenuBean menuBean = MenuData.getInstance().getData(application.getCode());
                                listMenu.add(menuBean);
                            }
                            appMenuAdapter.setNotify(listMenu);
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }
}
