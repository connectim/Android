package connect.activity.login.presenter;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import connect.activity.login.contract.StartContract;
import connect.database.SharedPreferenceUtil;
import connect.utils.FileUtil;
import connect.utils.RegularUtil;
import connect.utils.scan.ResolveUrlUtil;
import connect.utils.system.SystemDataUtil;

public class StartPagePresenter implements StartContract.Presenter {

    private StartContract.View mView;

    public StartPagePresenter(StartContract.View mView) {
        this.mView = mView;
        mView.setPresenter(this);
    }

    @Override
    public void start() {
        FileUtil.deleteDirectory(FileUtil.tempPath);

        String languageCode = SharedPreferenceUtil.getInstance().getStringValue(SharedPreferenceUtil.APP_LANGUAGE_CODE);
        SystemDataUtil.setAppLanguage(mView.getActivity(),languageCode);

        goInActivity(mView.getActivity());
    }

    private void goInActivity(final Activity mActivity) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (!SharedPreferenceUtil.getInstance().containsUser()) {
                    mView.goIntoLoginForPhone();
                } else {
                    //openFromWeb(mActivity);
                    mView.goIntoHome();
                }

                /*if (!SharedPreferenceUtil.getInstance().isContains(SharedPreferenceUtil.FIRST_INTO_APP)) {
                    mView.goIntoLoginForPhone();
                } else if (!SharedPreferenceUtil.getInstance().containsUser()) {
                    mView.goIntoLoginForPhone();
                } else {
                    openFromWeb(mActivity);
                    mView.goIntoHome();
                }*/
                mActivity.finish();
            }
        }).start();
    }

    /**
     * Save url with open the App.
     * @param activity The activity reference
     */
    /*private void openFromWeb(Activity activity) {
        Intent intent = activity.getIntent();
        String action = intent.getAction();
        Uri uri = intent.getData();
        if (Intent.ACTION_VIEW.equals(action) && RegularUtil.matches(uri.toString(), ResolveUrlUtil.Web_Url)) {
            SharedPreferenceUtil.getInstance().putValue(SharedPreferenceUtil.WEB_OPEN_APP,uri.toString());
        }
    }*/
}
