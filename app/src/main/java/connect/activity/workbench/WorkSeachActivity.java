package connect.activity.workbench;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;

import com.google.protobuf.ByteString;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import connect.activity.base.BaseActivity;
import connect.activity.home.view.LineDecoration;
import connect.activity.workbench.adapter.WorkSearchAdapter;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.DialogUtil;
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
                String searchTxt = toolbar.getSearchTxt().trim();
                searchAppsWorks(searchTxt);

                toolbar.clearSearchTxt();
            }
        });

        workSearchAdapter = new WorkSearchAdapter();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(activity);
        recyclerview.addItemDecoration(new LineDecoration(activity));
        recyclerview.setLayoutManager(linearLayoutManager);
        recyclerview.setAdapter(workSearchAdapter);
        workSearchAdapter.setInterWorksearch(new WorkSearchAdapter.InterWorksearch() {

            @Override
            public void itemClick(String code) {
                DialogUtil.showAlertTextView(activity,
                        getString(R.string.Set_tip_title),
                        getString(R.string.Link_Function_Under_Development),
                        "", "", true, new DialogUtil.OnItemClickListener() {
                            @Override
                            public void confirm(String value) {

                            }

                            @Override
                            public void cancel() {

                            }
                        });
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
}
