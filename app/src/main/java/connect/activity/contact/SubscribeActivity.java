package connect.activity.contact;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.google.protobuf.ByteString;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.activity.base.BaseActivity;
import connect.activity.contact.adapter.SubscribeAdapter;
import connect.activity.contact.bean.RssBean;
import connect.activity.home.view.LineDecoration;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.UriUtil;
import connect.utils.cryption.DecryptionUtil;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import connect.widget.TopToolBar;
import protos.Connect;

/**
 * Subscribe number
 */

public class SubscribeActivity extends BaseActivity {

    @Bind(R.id.toolbar)
    TopToolBar toolbar;
    @Bind(R.id.recyclerview)
    RecyclerView recyclerview;

    private SubscribeActivity mActivity;
    private SubscribeAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_subscribe);
        ButterKnife.bind(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        initView();
    }

    @Override
    public void initView() {
        mActivity = this;
        toolbar.setBlackStyle();
        toolbar.setLeftImg(R.mipmap.back_white);
        toolbar.setTitle(null, R.string.Link_Subscribe_to_the_center);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mActivity);
        recyclerview.setLayoutManager(linearLayoutManager);
        recyclerview.addItemDecoration(new LineDecoration(mActivity));
        adapter = new SubscribeAdapter(mActivity);
        adapter.setOnItemListener(onItemListener);
        recyclerview.setAdapter(adapter);

        getSubscribeList();
    }

    @OnClick(R.id.left_img)
    void goBack(View view) {
        ActivityUtil.goBack(mActivity);
    }

    SubscribeAdapter.OnItemListener onItemListener = new SubscribeAdapter.OnItemListener(){
        @Override
        public void itemClick(int position, Connect.RSS entity) {
            RssBean rssBean = new RssBean(entity.getRssId(), entity.getIcon(), entity.getTitle(), entity.getDesc(), entity.getSubRss());
            SubscribeDetailActivity.startActivity(mActivity, rssBean);
        }
    };

    private void getSubscribeList(){
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.CONNECT_V2_RSS, ByteString.copyFrom(new byte[]{}), new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                try {
                    Connect.IMResponse imResponse = Connect.IMResponse.parseFrom(response.getBody().toByteArray());
                    Connect.StructData structData = DecryptionUtil.decodeAESGCMStructData(imResponse.getCipherData());
                    Connect.RSSList rssList = Connect.RSSList.parseFrom(structData.getPlainData());
                    adapter.setNotify(rssList.getRssListList());
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Connect.HttpResponse response) {}
        });
    }

}
