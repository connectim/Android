package connect.activity.set;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import java.util.ArrayList;
import java.util.List;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.activity.base.BaseActivity;
import connect.activity.set.adapter.BlackListAdapter;
import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.bean.ContactEntity;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.ProtoBufUtil;
import connect.utils.UriUtil;
import connect.utils.cryption.DecryptionUtil;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import connect.widget.TopToolBar;
import protos.Connect;

/**
 * The user black list
 */
public class PrivateBlackActivity extends BaseActivity {

    @Bind(R.id.toolbar_top)
    TopToolBar toolbarTop;
    @Bind(R.id.recyclerview)
    RecyclerView recyclerview;

    private PrivateBlackActivity mActivity;
    private BlackListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_black_list);
        ButterKnife.bind(this);
        initView();
    }

    @Override
    public void initView() {
        mActivity = this;
        toolbarTop.setBlackStyle();
        toolbarTop.setLeftImg(R.mipmap.back_white);
        toolbarTop.setTitle(null, R.string.Link_Black_List);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mActivity);
        recyclerview.setLayoutManager(linearLayoutManager);
        adapter = new BlackListAdapter(mActivity);
        adapter.setOnItemChildListence(childClickListener);
        recyclerview.setAdapter(adapter);
        requestList();
    }

    @OnClick(R.id.left_img)
    void goBack(View view) {
        ActivityUtil.goBack(mActivity);
    }

    private BlackListAdapter.OnItemChildClickListener childClickListener = new BlackListAdapter.OnItemChildClickListener() {
        @Override
        public void remove(int position, Connect.UserInfo userInfo) {
            requestBlock(position, userInfo);
        }
    };

    /**
     * To obtain a black list
     */
    private void requestList() {
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.CONNEXT_V1_BLACKLIST_LIST, ByteString.copyFrom(new byte[]{}),
                new ResultCall<Connect.HttpResponse>() {
                    @Override
                    public void onResponse(Connect.HttpResponse response) {
                        try {
                            Connect.IMResponse imResponse = Connect.IMResponse.parseFrom(response.getBody().toByteArray());
                            Connect.StructData structData = DecryptionUtil.decodeAESGCMStructData(imResponse.getCipherData());
                            Connect.UsersInfo usersInfo = Connect.UsersInfo.parseFrom(structData.getPlainData());
                            List<Connect.UserInfo> list = usersInfo.getUsersList();
                            ArrayList<Connect.UserInfo> listCheck = new ArrayList<>();
                            for (Connect.UserInfo userInfo : list) {
                                if (ProtoBufUtil.getInstance().checkProtoBuf(userInfo)) {
                                    listCheck.add(userInfo);
                                }
                            }
                            adapter.setDataNotify(listCheck);
                        } catch (InvalidProtocolBufferException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Connect.HttpResponse response) {}
                });
    }

    /**
     * remove black user
     *
     * @param position
     * @param userInfo
     */
    private void requestBlock(final int position, final Connect.UserInfo userInfo) {
        Connect.UserIdentifier userIdentifier = Connect.UserIdentifier.newBuilder()
                .setAddress(userInfo.getUid())
                .build();
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.CONNEXT_V1_BLACKLIST_REMOVE, userIdentifier, new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                ContactEntity friendEntity = ContactHelper.getInstance().loadFriendEntity(userInfo.getPubKey());
                if (null != friendEntity) {
                    friendEntity.setBlocked(false);
                    ContactHelper.getInstance().updataFriendSetEntity(friendEntity);
                }
                adapter.removeDataNotify(position);
            }

            @Override
            public void onError(Connect.HttpResponse response) {}
        });
    }

}
