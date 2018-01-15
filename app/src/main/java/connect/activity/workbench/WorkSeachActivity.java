package connect.activity.workbench;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;

import butterknife.Bind;
import butterknife.ButterKnife;
import connect.activity.base.BaseActivity;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work_seach);
        ButterKnife.bind(this);
    }

    public static void startActivity(Activity activity) {
        ActivityUtil.next(activity, WorkSeachActivity.class);
    }

    @Override
    public void initView() {

    }

}
