package connect.activity.contact;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.protobuf.InvalidProtocolBufferException;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.activity.base.BaseActivity;
import connect.activity.contact.adapter.SearchResultAdapter;
import connect.activity.contact.bean.SourceType;
import connect.activity.home.view.LineDecoration;
import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.bean.ContactEntity;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.ToastEUtil;
import connect.utils.UriUtil;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import connect.widget.TopToolBar;
import protos.Connect;

/**
 * Search for friends results
 */
public class SearchFriendResultActivity extends BaseActivity {

    @Bind(R.id.toolbar)
    TopToolBar toolbar;
    @Bind(R.id.search_edit)
    EditText searchEdit;
    @Bind(R.id.del_tv)
    ImageView delTv;
    @Bind(R.id.no_result_tv)
    TextView noResultTv;
    @Bind(R.id.recyclerview)
    RecyclerView recyclerview;

    private SearchFriendResultActivity mActivity;
    private SearchResultAdapter adapter;

    public static void startActivity(Activity activity, String text) {
        Bundle bundle = new Bundle();
        bundle.putString("text", text);
        ActivityUtil.next(activity, SearchFriendResultActivity.class, bundle);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_search_server);
        ButterKnife.bind(this);
        initView();
    }

    @Override
    public void initView() {
        mActivity = this;
        toolbar.setBlackStyle();
        toolbar.setLeftImg(R.mipmap.back_white);
        toolbar.setTitle(null, R.string.Link_Search_friends);
        //toolbar.setRightImg(R.mipmap.search3x);

        searchEdit.setOnKeyListener(keyListener);
        searchEdit.addTextChangedListener(textWatcher);
        String text = getIntent().getExtras().getString("text");
        searchEdit.setText(text);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mActivity);
        recyclerview.setLayoutManager(linearLayoutManager);
        adapter = new SearchResultAdapter(mActivity);
        recyclerview.addItemDecoration(new LineDecoration(mActivity));
        adapter.setOnItemChildListence(onItemChildClickListener);
        recyclerview.setAdapter(adapter);

        requestSearch();
    }

    @OnClick(R.id.left_img)
    void goBack(View view) {
        ActivityUtil.goBack(mActivity);
    }

    /*@OnClick(R.id.right_lin)
    void searchUser(View view) {
        requestSearch();
    }*/

    @OnClick(R.id.del_tv)
    void delEdit(View view) {
        searchEdit.setText("");
    }

    private View.OnKeyListener keyListener = new View.OnKeyListener() {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
                ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
                        .hideSoftInputFromWindow(SearchFriendResultActivity.this.getCurrentFocus()
                                .getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                requestSearch();
            }
            return false;
        }
    };

    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (TextUtils.isEmpty(s.toString())) {
                delTv.setVisibility(View.GONE);
            } else {
                delTv.setVisibility(View.VISIBLE);
            }
        }
    };

    SearchResultAdapter.OnItemChildClickListener onItemChildClickListener = new SearchResultAdapter.OnItemChildClickListener() {
        @Override
        public void itemClick(int position, Connect.UserInfo userInfo) {
            ContactEntity friendEntity = ContactHelper.getInstance().loadFriendEntity(userInfo.getUid());
            if (friendEntity != null) {
                FriendInfoActivity.startActivity(mActivity, userInfo.getUid());
            } else {
                StrangerInfoActivity.startActivity(mActivity, userInfo.getUid(), SourceType.SEARCH);
            }
            mActivity.finish();
        }
    };

    private void requestSearch() {
        String searchContext = searchEdit.getText().toString().trim();
        if (TextUtils.isEmpty(searchContext)) {
            recyclerview.setVisibility(View.GONE);
            noResultTv.setVisibility(View.VISIBLE);
            return;
        }

        Connect.SearchUser searchUser = Connect.SearchUser.newBuilder()
                .setCriteria(searchContext)
                .build();
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.CONNECT_V1_USER_SEARCH, searchUser, new ResultCall<Connect.HttpNotSignResponse>() {
            @Override
            public void onResponse(Connect.HttpNotSignResponse response) {
                try {
                    Connect.StructData structData = Connect.StructData.parseFrom(response.getBody());
                    Connect.UsersInfo usersInfo = Connect.UsersInfo.parseFrom(structData.getPlainData());
                    if (usersInfo.getUsersList().size() > 0) {
                        noResultTv.setVisibility(View.GONE);
                        recyclerview.setVisibility(View.VISIBLE);
                        adapter.setDataNotify(usersInfo.getUsersList());
                    } else {
                        noResultTv.setVisibility(View.VISIBLE);
                        recyclerview.setVisibility(View.GONE);
                    }
                } catch (InvalidProtocolBufferException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Connect.HttpNotSignResponse response) {
                noResultTv.setVisibility(View.VISIBLE);
                recyclerview.setVisibility(View.GONE);

                String errorMessage = response.getMessage();
                if (TextUtils.isEmpty(errorMessage)) {
                    errorMessage = mActivity.getString(R.string.Network_equest_failed_please_try_again_later);
                }
                ToastEUtil.makeText(mActivity, errorMessage, 2).show();
            }
        });
    }

}
