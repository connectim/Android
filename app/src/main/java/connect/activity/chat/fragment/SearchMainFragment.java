package connect.activity.chat.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.activity.base.BaseFragment;
import connect.activity.chat.SearchActivity;
import connect.activity.chat.SearchContentActivity;
import connect.database.green.DaoHelper.ParamManager;
import connect.ui.activity.R;
import connect.widget.XCFlowLayout;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;

/**
 * Created by Administrator on 2018/1/31 0031.
 */

public class SearchMainFragment extends BaseFragment {

    @Bind(R.id.contact_text)
    TextView contactText;
    @Bind(R.id.group_text)
    TextView groupText;
    @Bind(R.id.chat_record_text)
    TextView chatRecordText;
    @Bind(R.id.xCF_layout)
    XCFlowLayout xCFLayout;

    private SearchActivity mActivity;

    public static SearchMainFragment startFragment() {
        SearchMainFragment searchMainFragment = new SearchMainFragment();
        return searchMainFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_main, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mActivity = (SearchActivity)getActivity();
        initView();
    }

    public void initView() {
        ArrayList<String> list = ParamManager.getInstance().getCommonlySearch();
        xCFLayout.removeAllViews();
        ViewGroup.MarginLayoutParams lp = new ViewGroup.MarginLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        lp.leftMargin = 20;
        lp.rightMargin = 20;
        lp.topMargin = 15;
        lp.bottomMargin = 15;
        for(String string : list){
            View view = LayoutInflater.from(mActivity).inflate(R.layout.item_commonly_search, xCFLayout, false);
            TextView text = (TextView)view.findViewById(R.id.text);
            ImageView image = (ImageView)view.findViewById(R.id.image);
            text.setText(string);
            text.setTag(string);
            image.setTag(string);
            text.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String value = (String) v.getTag();
                    mActivity.commonltGoSearch(value);
                }
            });
            image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String value = (String) v.getTag();
                    ParamManager.getInstance().removeCommonlyString(value);
                    initView();
                }
            });
            xCFLayout.addView(view, lp);
        }
    }

    @OnClick(R.id.contact_text)
    void searchContact(View view) {
        SearchContentActivity.lunchActivity(mActivity, 1);
    }

    @OnClick(R.id.group_text)
    void searchGroup(View view) {
        SearchContentActivity.lunchActivity(mActivity, 2);
    }

    @OnClick(R.id.chat_record_text)
    void searchChatRecord(View view) {
        SearchContentActivity.lunchActivity(mActivity, 3);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }
}
