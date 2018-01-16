package connect.activity.workbench;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import butterknife.Bind;
import butterknife.ButterKnife;
import connect.activity.base.BaseActivity;
import connect.activity.home.view.LineDecoration;
import connect.activity.workbench.adapter.WorkSearchAdapter;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.widget.TopToolBar;

/**
 * 工作台搜索应用
 */
public class WorkSeachActivity extends BaseActivity {

    @Bind(R.id.toolbar)
    TopToolBar toolbar;
    @Bind(R.id.recyclerview)
    RecyclerView recyclerview;

    private WorkSeachActivity activity;
    private WorkSearchAdapter workSearchAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work_seach);
        ButterKnife.bind(this);
        initView();
    }

    public static void startActivity(Activity activity) {
        ActivityUtil.next(activity, WorkSeachActivity.class);
    }

    @Override
    public void initView() {
        activity = this;
        toolbar.setBlackStyle();
        toolbar.setLeftImg(R.mipmap.back_white);
        toolbar.setLeftListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityUtil.goBack(activity);
            }
        });
        toolbar.setRightText(getResources().getString(R.string.Work_Search));
        toolbar.setSearchTitle(R.mipmap.icon_search_small3x, getResources().getString(R.string.Work_Service_Search));
        toolbar.setRightListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        workSearchAdapter = new WorkSearchAdapter();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(activity);
        recyclerview.addItemDecoration(new LineDecoration(activity));
        recyclerview.setLayoutManager(linearLayoutManager);
        recyclerview.setAdapter(workSearchAdapter);
    }
}
