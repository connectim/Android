package connect.activity.contact;

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

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.activity.base.BaseActivity;
import connect.activity.contact.adapter.SearchAdapter;
import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.bean.ContactEntity;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.system.SystemUtil;

/**
 * Search add friends
 */
public class SearchFriendActivity extends BaseActivity {

    @Bind(R.id.search_edit)
    EditText searchEdit;
    @Bind(R.id.del_tv)
    ImageView delTv;
    @Bind(R.id.cancel_tv)
    TextView cancelTv;
    @Bind(R.id.left_img)
    ImageView leftImg;
    @Bind(R.id.recyclerview)
    RecyclerView recyclerview;

    private SearchFriendActivity mActivity;
    private SearchAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_search);
        ButterKnife.bind(this);
        initView();
    }

    @Override
    public void initView() {
        mActivity = this;
        searchEdit.addTextChangedListener(textWatcher);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mActivity);
        recyclerview.setLayoutManager(linearLayoutManager);
        adapter = new SearchAdapter(mActivity);
        recyclerview.setAdapter(adapter);
        adapter.setOnItemClickListener(itemClickListener);
        SystemUtil.showKeyBoard(mActivity, searchEdit);

        searchEdit.setOnKeyListener(keyListener);
    }

    @OnClick(R.id.cancel_tv)
    void goBack(View view) {
        ActivityUtil.goBackWithResult(mActivity, 0, null, android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @OnClick(R.id.del_tv)
    void delEdit(View view) {
        searchEdit.setText("");
    }

    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}
        @Override
        public void afterTextChanged(Editable s) {
            if (TextUtils.isEmpty(s.toString())) {
                delTv.setVisibility(View.GONE);
                ArrayList arrayList = new ArrayList<>();
                adapter.setDataNotify(arrayList);
            } else {
                delTv.setVisibility(View.VISIBLE);
                ArrayList arrayList = new ArrayList<>();
                ContactEntity friendEntity = new ContactEntity();
                friendEntity.setUid(s.toString());
                arrayList.add(friendEntity);

                List<ContactEntity> list = ContactHelper.getInstance().loadFriendEntityFromText(s.toString());
                arrayList.addAll(list);
                adapter.setDataNotify(arrayList);
            }
        }
    };

    private SearchAdapter.OnItemClickListener itemClickListener = new SearchAdapter.OnItemClickListener() {
        @Override
        public void itemClick(int position, ContactEntity list, int type) {
            switch (type) {
                case 1:
                    SearchFriendResultActivity.startActivity(mActivity, list.getUid());
                    mActivity.finish();
                    break;
                case 2:
                    //FriendInfoActivity.startActivity(mActivity, list.getUid());
                    ContactInfoActivity.lunchActivity(mActivity, list);
                    mActivity.finish();
                    break;
                default:
                    break;
            }
        }
    };

    private View.OnKeyListener keyListener = new View.OnKeyListener(){
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
                ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
                        .hideSoftInputFromWindow(SearchFriendActivity.this.getCurrentFocus()
                                .getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                SearchFriendResultActivity.startActivity(mActivity, searchEdit.getText().toString().trim());
                mActivity.finish();
            }
            return false;
        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            ActivityUtil.goBackBottom(this);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(R.anim.activity_0_to_0, R.anim.dialog_bottom_dismiss);
    }
}
