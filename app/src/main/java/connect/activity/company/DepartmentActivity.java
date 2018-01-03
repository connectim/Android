package connect.activity.company;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import connect.activity.base.BaseActivity;
import connect.activity.company.adapter.DepartmentAdapter;
import connect.activity.home.adapter.CompanyAdapter;
import connect.activity.home.view.LineDecoration;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.widget.TopToolBar;
import protos.Connect;

/**
 * Created by Administrator on 2018/1/3 0003.
 */

public class DepartmentActivity extends BaseActivity {

    @Bind(R.id.toolbar_top)
    TopToolBar toolbarTop;
    @Bind(R.id.recyclerview)
    RecyclerView recyclerview;
    private DepartmentActivity mActivity;
    private DepartmentAdapter adapter;

    public static void lunchActivity(Activity activity, ArrayList<Connect.Workmate> list){
        Bundle bundle = new Bundle();
        bundle.putSerializable("list", list);
        ActivityUtil.next(activity, DepartmentActivity.class, bundle);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_company_department);
        ButterKnife.bind(this);
        initView();
    }

    @Override
    public void initView() {
        mActivity = this;
        ArrayList<Connect.Workmate> list = (ArrayList<Connect.Workmate>) getIntent().getExtras().getSerializable("list");
        toolbarTop.setTitle(list.get(0).getOU());

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mActivity);
        recyclerview.setLayoutManager(linearLayoutManager);
        recyclerview.addItemDecoration(new LineDecoration(mActivity));
        adapter = new DepartmentAdapter(mActivity);
        adapter.setItemClickListener(departmentAdapter);
        recyclerview.setAdapter(adapter);

        adapter.setNotify(list);
    }

    DepartmentAdapter.OnItemClickListener departmentAdapter = new DepartmentAdapter.OnItemClickListener(){
        @Override
        public void itemClick(Connect.Workmate workmate) {

        }

        @Override
        public void addFriend(int position, Connect.Workmate workmate) {

        }
    };

}
