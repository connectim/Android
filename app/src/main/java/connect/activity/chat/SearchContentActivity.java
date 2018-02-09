package connect.activity.chat;

import android.app.Activity;
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

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.activity.base.BaseFragmentActivity;
import connect.activity.chat.fragment.SearchContentFragment;
import connect.activity.contact.ContactInfoActivity;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;

/**
 * Created by Administrator on 2018/2/1 0001.
 */

public class SearchContentActivity extends BaseFragmentActivity {

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

    private SearchContentActivity mActivity;
    private SearchContentFragment searchContentFragment;
    private int style;

    public static void lunchActivity(Activity activity, int style) {
        Bundle bundle = new Bundle();
        bundle.putInt("style", style);
        ActivityUtil.next(activity, SearchContentActivity.class, bundle);
    }

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
        style = getIntent().getExtras().getInt("style");

        if(style == 1){
            searchEdit.setHint(" " + getString(R.string.Link_Search_contacts));
        }else if(style == 2){
            searchEdit.setHint(" " + getString(R.string.Link_Search_group));
        }else if(style == 3){
            searchEdit.setHint(" " + getString(R.string.Link_Search_chat_log));
        }
        searchEdit.addTextChangedListener(textWatcher);
        searchContentFragment = SearchContentFragment.startFragment();
        switchFragment(1);
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
            searchContentFragment.updateView(value, style);
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
            } else {
                delTv.setVisibility(View.VISIBLE);
            }
        }
    };

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
