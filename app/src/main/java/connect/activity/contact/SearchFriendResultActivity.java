package connect.activity.contact;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.protobuf.InvalidProtocolBufferException;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.database.SharedPreferenceUtil;
import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.bean.ContactEntity;
import connect.ui.activity.R;
import connect.activity.contact.bean.SourceType;
import connect.activity.base.BaseActivity;
import connect.utils.ActivityUtil;
import connect.utils.ProtoBufUtil;
import connect.utils.ToastEUtil;
import connect.utils.UriUtil;
import connect.utils.cryption.DecryptionUtil;
import connect.utils.glide.GlideUtil;
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
    @Bind(R.id.result_lin)
    LinearLayout resultLin;

    private SearchFriendResultActivity mActivity;

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
        toolbar.setRightImg(R.mipmap.search3x);
        toolbar.setRightListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                search();
            }
        });

        searchEdit.setOnKeyListener(keyListener);
        searchEdit.addTextChangedListener(textWatcher);
        String text = getIntent().getExtras().getString("text");
        searchEdit.setText(text);
        if(!TextUtils.isEmpty(text)){
            requestSearch(text);
        }
        search();
    }

    private void updateView(final Connect.UserInfo userInfo){
        resultLin.removeAllViews();
        if(userInfo != null){
            noResultTv.setVisibility(View.GONE);
            View view = LayoutInflater.from(mActivity).inflate(R.layout.item_contact_search_result,null);
            ImageView avatar = (ImageView)view.findViewById(R.id.avatar_rimg);
            TextView nickname = (TextView)view.findViewById(R.id.nickname_tv);
            Button statusBtn = (Button)view.findViewById(R.id.status_btn);
            statusBtn.setText(R.string.Link_Add);
            GlideUtil.loadAvatarRound(avatar,userInfo.getAvatar());
            nickname.setText(userInfo.getUsername());

            ContactEntity friendEntity = ContactHelper.getInstance().loadFriendEntity(userInfo.getPubKey());
            if(userInfo.getPubKey().equals(SharedPreferenceUtil.getInstance().getUser().getPubKey())){
                resultLin.removeAllViews();
                noResultTv.setVisibility(View.VISIBLE);
            }else if(friendEntity != null){
                statusBtn.setVisibility(View.GONE);
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FriendInfoActivity.startActivity(mActivity,userInfo.getPubKey());
                    }
                });
                resultLin.addView(view);
            }else{
                statusBtn.setVisibility(View.VISIBLE);
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        StrangerInfoActivity.startActivity(mActivity,userInfo.getUid(), SourceType.SEARCH);
                    }
                });
                statusBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        StrangerInfoActivity.startActivity(mActivity,userInfo.getUid(), SourceType.SEARCH);
                    }
                });
                resultLin.addView(view);
            }
        }else{
            resultLin.removeAllViews();
            noResultTv.setVisibility(View.VISIBLE);
        }
    }

    @OnClick(R.id.left_img)
    void goBack(View view) {
        ActivityUtil.goBack(mActivity);
    }

    @OnClick(R.id.del_tv)
    void delEdit(View view) {
        searchEdit.setText("");
    }

    private View.OnKeyListener keyListener = new View.OnKeyListener(){
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
                ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
                        .hideSoftInputFromWindow(SearchFriendResultActivity.this.getCurrentFocus()
                                .getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                search();
            }
            return false;
        }
    };

    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}
        @Override
        public void afterTextChanged(Editable s) {
            if(TextUtils.isEmpty(s.toString())){
                delTv.setVisibility(View.GONE);
            }else{
                delTv.setVisibility(View.VISIBLE);
            }
        }
    };

    private void search() {
        String searchContext = searchEdit.getText().toString().trim();
        if (TextUtils.isEmpty(searchContext)) {
            resultLin.removeAllViews();
            noResultTv.setVisibility(View.VISIBLE);
            return;
        }
        requestSearch(searchContext);
    }

    private void requestSearch(String text){
        Connect.SearchUser searchUser = Connect.SearchUser.newBuilder()
                .setTyp(2)
                .setCriteria(text)
                .build();
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.CONNECT_V1_USER_SEARCH, searchUser, new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                try {
                    Connect.IMResponse imResponse = Connect.IMResponse.parseFrom(response.getBody().toByteArray());
                    Connect.StructData structData = DecryptionUtil.decodeAESGCMStructData(imResponse.getCipherData());
                    Connect.UserInfo userInfo = Connect.UserInfo.parseFrom(structData.getPlainData());
                    if(ProtoBufUtil.getInstance().checkProtoBuf(userInfo)){
                        updateView(userInfo);
                    }
                } catch (InvalidProtocolBufferException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Connect.HttpResponse response) {
                updateView(null);

                String errorMessage = response.getMessage();
                if (TextUtils.isEmpty(errorMessage)) {
                    errorMessage = mActivity.getString(R.string.Network_equest_failed_please_try_again_later);
                }
                ToastEUtil.makeText(mActivity, errorMessage, 2).show();
            }
        });
    }

}
