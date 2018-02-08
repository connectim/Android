package connect.activity.chat;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.activity.base.BaseActivity;
import connect.activity.base.BaseFragmentActivity;
import connect.activity.chat.fragment.SearchContentFragment;
import connect.activity.chat.fragment.SearchMainFragment;
import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.bean.ContactEntity;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;

/**
 * Created by Administrator on 2018/1/31 0031.
 */

public class SearchActivity extends BaseFragmentActivity {

    @Bind(R.id.left_img)
    ImageView leftImg;
    @Bind(R.id.search_edit)
    EditText searchEdit;
    @Bind(R.id.del_tv)
    ImageView delTv;
    @Bind(R.id.search_tv)
    TextView searchTv;
    @Bind(R.id.content_fragment)
    FrameLayout contentFragment;

    private SearchActivity mActivity;
    private SearchContentFragment searchContentFragment;
    private SearchMainFragment searchMainFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_search);
        ButterKnife.bind(this);
        initView();
    }

    @Override
    public void initView() {
        mActivity = this;

        searchEdit.setHint(" " + getString(R.string.Link_Search));
        searchEdit.addTextChangedListener(textWatcher);

        searchMainFragment = SearchMainFragment.startFragment();
        searchContentFragment = SearchContentFragment.startFragment();
        switchFragment(1);
        switchFragment(0);
    }

    @OnClick(R.id.left_img)
    void goBack(View view) {
        ActivityUtil.goBack(mActivity);
    }

    @OnClick(R.id.del_tv)
    void clearEdit(View view) {
        searchEdit.setText("");
    }

    @OnClick(R.id.search_tv)
    void search(View view) {
        String value = searchEdit.getText().toString().trim();
        if(!TextUtils.isEmpty(value)){
            switchFragment(1);
            searchContentFragment.updateView(value, 0);
        }
    }

    TextWatcher textWatcher = new TextWatcher(){
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}
        @Override
        public void afterTextChanged(Editable s) {
            if (TextUtils.isEmpty(s.toString())) {
                delTv.setVisibility(View.GONE);
                switchFragment(0);
                searchMainFragment.initView();
            } else {
                delTv.setVisibility(View.VISIBLE);
            }
        }
    };

    public void commonltGoSearch(String value){
        searchEdit.setText(value);
        switchFragment(1);
        searchContentFragment.updateView(value, 0);
    }

    public void switchFragment(int code) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        List<Fragment> fragments = fragmentManager.getFragments();
        if (fragments != null) {
            for (Fragment fragment : fragments) {
                if (fragment.isVisible()) {
                    fragmentTransaction.hide(fragment);
                }
            }
        }
        switch (code) {
            case 0:
                if (!searchMainFragment.isAdded()) {
                    fragmentTransaction.add(R.id.content_fragment, searchMainFragment);
                } else {
                    fragmentTransaction.show(searchMainFragment);
                }
                break;
            case 1:
                if (!searchContentFragment.isAdded()) {
                    fragmentTransaction.add(R.id.content_fragment, searchContentFragment);
                } else {
                    fragmentTransaction.show(searchContentFragment);
                }
                break;
        }

        //commit :IllegalStateException: Can not perform this action after onSaveInstanceState
        fragmentTransaction.commitAllowingStateLoss();
    }

}
