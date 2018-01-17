package connect.activity.workbench;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;

import com.google.protobuf.ByteString;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import connect.activity.base.BaseActivity;
import connect.activity.home.view.LineDecoration;
import connect.activity.workbench.adapter.WorkSearchAdapter;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.UriUtil;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import connect.widget.TopToolBar;
import protos.Connect;

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
    private boolean isManager=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work_seach);
        ButterKnife.bind(this);
        initView();
    }

    public static void startActivity(Activity activity, boolean isManager) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("Is_Manager", isManager);
        ActivityUtil.next(activity, WorkSeachActivity.class, bundle);
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


        isManager = getIntent().getBooleanExtra("Is_Manager", false);
        if(isManager){
            toolbar.setTitle(getResources().getString(R.string.Link_Function_Manager));
        }else{
            toolbar.setRightText(getResources().getString(R.string.Work_Search));
            toolbar.setSearchTitle(R.mipmap.icon_search_small3x, getResources().getString(R.string.Work_Service_Search));
            toolbar.setRightListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String searchTxt = toolbar.getSearchTxt().trim();
                    searchAppsWorks(searchTxt);

                    toolbar.clearSearchTxt();
                }
            });
        }

        workSearchAdapter = new WorkSearchAdapter();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(activity);
        recyclerview.addItemDecoration(new LineDecoration(activity));
        recyclerview.setLayoutManager(linearLayoutManager);
        recyclerview.setAdapter(workSearchAdapter);
        workSearchAdapter.setInterWorksearch(new WorkSearchAdapter.InterWorksearch() {

            @Override
            public void itemClick(boolean isAdd ,String code) {
                updateAppsAddState(isAdd, code);
            }
        });

        searchAppsWorks("");
    }

    public void searchAppsWorks(String content) {
        ByteString byteString = null;
        if (TextUtils.isEmpty(content)) {
            byteString = ByteString.copyFrom(new byte[]{});
        } else {
            Connect.Application application = Connect.Application.newBuilder()
                    .setName(content)
                    .build();

            byteString = application.toByteString();
        }

        OkHttpUtil.getInstance().postEncrySelf(UriUtil.CONNECT_V3_API_APPLICATIONS, byteString, new ResultCall<Connect.HttpNotSignResponse>() {
            @Override
            public void onResponse(Connect.HttpNotSignResponse response) {
                try {
                    Connect.StructData structData = Connect.StructData.parseFrom(response.getBody());
                    Connect.Applications applications = Connect.Applications.parseFrom(structData.getPlainData());
                    List<Connect.Application> applications1 = applications.getListList();

                    if (isManager) {
                        List<Connect.Application> filterApplications = new ArrayList<Connect.Application>();
                        for (Connect.Application application : applications1) {
                            if (application.getCategory() <= 2) {
                                filterApplications.add(application);
                            }
                        }
                        applications1 = filterApplications;
                    }
                    workSearchAdapter.setDatas(applications1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Connect.HttpNotSignResponse response) {
            }
        });
    }

    /**
     * 更改应用的添加状态
     * @param isAdd
     * @param code
     */
    public void updateAppsAddState(boolean isAdd, String code) {
        Connect.Application application = Connect.Application.newBuilder()
                .setAdded(isAdd)
                .setCode(code)
                .build();

        String uid = isAdd?UriUtil.CONNECT_V3_API_APPLICATIONS_ADD:UriUtil.CONNECT_V3_API_APPLICATIONS_DEL;
        OkHttpUtil.getInstance().postEncrySelf(uid, application, new ResultCall<Connect.HttpNotSignResponse>() {
            @Override
            public void onResponse(Connect.HttpNotSignResponse response) {
                searchAppsWorks("");
            }

            @Override
            public void onError(Connect.HttpNotSignResponse response) {
            }
        });
    }
}
