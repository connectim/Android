package connect.activity.base;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;

import connect.utils.ActivityUtil;
import connect.utils.ToastEUtil;

/**
 * Activity base class
 */
public abstract class BaseActivity extends Activity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        BaseApplication.getInstance().getActivityList().add(this);
    }

    public abstract void initView();

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK){
            ActivityUtil.goBack(this);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ToastEUtil.reset();
        BaseApplication.getInstance().getActivityList().remove(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}
