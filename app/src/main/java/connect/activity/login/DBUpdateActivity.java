package connect.activity.login;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import connect.ui.activity.R;
import connect.activity.base.BaseActivity;
import connect.activity.base.BaseApplication;
import connect.widget.DBUpgradeView;
import connect.widget.TopToolBar;

/**
 * database upgrdde
 * Created by pujin on 2017/4/18.
 */
public class DBUpdateActivity extends BaseActivity {

    private String Tag="DBUpdateActivity";
    private DBUpdateActivity activity;

    @Bind(R.id.toolbar_top)
    TopToolBar toolbarTop;
    @Bind(R.id.upgradeview)
    DBUpgradeView upgradeview;
    @Bind(R.id.txt1)
    TextView txt1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dbupdate);
        ButterKnife.bind(this);
        initView();
    }

    public static void startActivity() {
        Context context = BaseApplication.getInstance().getBaseContext();
        Intent intent = new Intent();
        intent.setClass(context, DBUpdateActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 50:
                    Intent intent = new Intent(activity, StartActivity.class);
                    activity.startActivity(intent);
                    break;
            }
        }
    };

    @Override
    public void initView() {
        activity = this;
        toolbarTop.setBlackStyle();
        toolbarTop.setTitle(getString(R.string.Chat_Update_Database));

        ValueAnimator animator = ValueAnimator.ofInt(0, 100);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.setDuration(5000).start();
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (int) animation.getAnimatedValue();
                upgradeview.setProgress(value);
                if (value < 100) {
                    txt1.setText(getString(R.string.Chat_Updating_Database) + "...");
                } else if (value == 100) {
                    txt1.setText(getString(R.string.Login_Update_successful) + "!");
                    handler.sendEmptyMessageDelayed(50, 1000);
                }
            }
        });
    }
}
