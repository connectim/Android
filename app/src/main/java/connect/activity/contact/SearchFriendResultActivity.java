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
    private Connect.UserInfo userInfoBase;

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

        searchEdit.setOnKeyListener(keyListener);
        searchEdit.addTextChangedListener(textWatcher);
        String text = getIntent().getExtras().getString("text");
        searchEdit.setText(text);
        requestSearch();
    }

    @OnClick(R.id.left_img)
    void goBack(View view) {
        ActivityUtil.goBack(mActivity);
    }

    @OnClick(R.id.right_lin)
    void searchUser(View view) {
        requestSearch();
    }

    @OnClick(R.id.del_tv)
    void delEdit(View view) {
        searchEdit.setText("");
    }

    private void updateView(Connect.UserInfo userInfo){
        resultLin.removeAllViews();
        if(userInfo == null){
            resultLin.removeAllViews();
            noResultTv.setVisibility(View.VISIBLE);
            return;
        }else{
            userInfoBase = userInfo;
            noResultTv.setVisibility(View.GONE);
        }

        View view = LayoutInflater.from(mActivity).inflate(R.layout.item_contact_search_result,null);
        ImageView avatar = (ImageView)view.findViewById(R.id.avatar_rimg);
        TextView nickname = (TextView)view.findViewById(R.id.nickname_tv);
        Button statusBtn = (Button)view.findViewById(R.id.status_btn);
        statusBtn.setText(R.string.Link_Add);
        GlideUtil.loadAvatarRound(avatar,userInfo.getAvatar());
        nickname.setText(userInfo.getUsername());

        ContactEntity friendEntity = ContactHelper.getInstance().loadFriendEntity(userInfo.getUid());
        if(userInfo.getUid().equals(SharedPreferenceUtil.getInstance().getUser().getUid())){
            // Query the user themselves
            resultLin.removeAllViews();
            noResultTv.setVisibility(View.VISIBLE);
        }else if(friendEntity != null){
            // Query the user is already a good friend relationship
            statusBtn.setVisibility(View.GONE);
            view.setOnClickListener(onClickListener);
            view.setTag(1);
            resultLin.addView(view);
        }else{
            // Query the user is a stranger
            statusBtn.setVisibility(View.VISIBLE);
            view.setOnClickListener(onClickListener);
            statusBtn.setOnClickListener(onClickListener);
            view.setTag(2);
            statusBtn.setTag(2);
            resultLin.addView(view);
        }
    }

    private View.OnKeyListener keyListener = new View.OnKeyListener(){
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

    View.OnClickListener onClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            int tag = (int)v.getTag();
            if(tag == 1){
                FriendInfoActivity.startActivity(mActivity, userInfoBase.getUid());
            }else if(tag == 2){
                StrangerInfoActivity.startActivity(mActivity, userInfoBase.getUid(), SourceType.SEARCH);
            }
        }
    };

    private void requestSearch(){
        String searchContext = searchEdit.getText().toString().trim();
        if (TextUtils.isEmpty(searchContext)) {
            resultLin.removeAllViews();
            noResultTv.setVisibility(View.VISIBLE);
            return;
        }

        Connect.SearchUser searchUser = Connect.SearchUser.newBuilder()
                .setTyp(2)
                .setCriteria(searchContext)
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
