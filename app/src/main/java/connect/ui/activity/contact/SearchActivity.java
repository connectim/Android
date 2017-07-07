package connect.ui.activity.contact;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.db.green.DaoHelper.ContactHelper;
import connect.db.green.bean.ContactEntity;
import connect.ui.activity.R;
import connect.ui.activity.contact.adapter.SearchAdapter;
import connect.ui.base.BaseActivity;
import connect.utils.ActivityUtil;
import connect.utils.system.SystemUtil;

/**
 * Created by Administrator on 2016/12/26.
 */
public class SearchActivity extends BaseActivity {

    @Bind(R.id.search_edit)
    EditText searchEdit;
    @Bind(R.id.del_tv)
    ImageView delTv;
    @Bind(R.id.list_view)
    ListView listView;
    @Bind(R.id.cancel_tv)
    TextView cancelTv;

    private SearchActivity mActivity;
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
        adapter = new SearchAdapter();
        listView.setAdapter(adapter);
        adapter.setOnItemClickListence(itemClickListence);
        SystemUtil.showKeyBoard(mActivity,searchEdit);
    }

    @OnClick(R.id.cancel_tv)
    void goBack(View view) {
        ActivityUtil.goBackWithResult(mActivity, 0,null,android.R.anim.fade_in,android.R.anim.fade_out);
        //ActivityUtil.goBackBottom(mActivity);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            ActivityUtil.goBackBottom(this);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @OnClick(R.id.del_tv)
    void delEdit(View view) {
        searchEdit.setText("");
    }

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
                ArrayList arrayList = new ArrayList<>();
                adapter.setDataNotify(arrayList);
            } else {
                delTv.setVisibility(View.VISIBLE);
                ArrayList arrayList = new ArrayList<>();
                ContactEntity friendEntity = new ContactEntity();
                friendEntity.setAddress(s.toString());
                arrayList.add(friendEntity);

                List<ContactEntity> list = ContactHelper.getInstance().loadFriendEntityFromText(s.toString());
                arrayList.addAll(list);
                adapter.setDataNotify(arrayList);
            }
        }
    };

    private SearchAdapter.OnItemClickListence itemClickListence = new SearchAdapter.OnItemClickListence() {
        @Override
        public void itemClick(int position, ContactEntity list, int type) {
            switch (type) {
                case 1:
                    SearchServerActivity.startActivity(mActivity, list.getAddress());
                    break;
                case 2:
                    FriendInfoActivity.startActivity(mActivity, list.getPub_key());
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(R.anim.activity_0_to_0, R.anim.dialog_bottom_dismiss);
    }

}
