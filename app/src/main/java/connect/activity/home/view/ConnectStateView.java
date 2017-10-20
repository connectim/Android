package connect.activity.home.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import connect.ui.activity.R;
import connect.utils.log.LogManager;
import instant.bean.ConnectState;

/**
 * Created by pujin on 2017/2/6.
 */
public class ConnectStateView extends RelativeLayout {

    private String Tag = "ConnectStateView";
    private TextView txt1;
    private ProgressBar probar1;

    public ConnectStateView(Context context) {
        super(context);
        initView();
    }

    public ConnectStateView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public void initView() {
        View view = View.inflate(getContext(), R.layout.view_connectstate, this);
        txt1 = (TextView) view.findViewById(R.id.txt1);
        probar1 = (ProgressBar) view.findViewById(R.id.probar1);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(ConnectState connectState) {
        String showTxt = null;
        switch (connectState.getType()) {
            case DISCONN:
                showTxt = getContext().getString(R.string.Chat_Not_connected);
                probar1.setVisibility(GONE);
                txt1.setText(showTxt);
                break;
            case REFRESH_ING:
                showTxt = getContext().getString(R.string.Chat_Refreshing_Secret_Key);
                probar1.setVisibility(VISIBLE);
                txt1.setText(showTxt);
                break;
            case REFRESH_SUCCESS:
                showTxt = getContext().getString(R.string.Chat_Secret_Key_Refreshed);
                probar1.setVisibility(VISIBLE);
                txt1.setText(showTxt);
                break;
            case CONNECT:
                showTxt = getContext().getString(R.string.Chat_Chats);
                probar1.setVisibility(GONE);
                txt1.setText(showTxt);
                break;
            case OFFLINE_PULL:
                showTxt = getContext().getString(R.string.Chat_Loading);
                probar1.setVisibility(VISIBLE);
                txt1.setText(showTxt);
                break;
        }

        LogManager.getLogger().d(Tag, showTxt);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        EventBus.getDefault().unregister(this);
    }
}
