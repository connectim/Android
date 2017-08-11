package connect.activity.chat.exts.contract;

import android.widget.RelativeLayout;

import connect.activity.base.contract.BasePresenter;
import connect.activity.base.contract.BaseView;
import connect.utils.VideoPlayerUtil;

/**
 * Created by Administrator on 2017/8/10.
 */

public interface VideoPlayContract {

    interface BView extends BaseView<VideoPlayContract.Presenter> {

        String getFilePath();

        String getFileLength();

        VideoPlayerUtil.VideoPlayListener getVideoListener();

        void videoTotalLength(String lengthformat);

        void calculateParamlayout(RelativeLayout.LayoutParams params);

        void playBackProgress(int percent,boolean select,String lengthformat);
    }

    interface Presenter extends BasePresenter {

        void startCountDownTimer();

        void pauseCountDownTimer();

        void resumeCountDownTimer();

    }
}
