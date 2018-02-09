package connect.activity.login.presenter;

import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.io.File;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;

import connect.activity.login.bean.UserBean;
import connect.activity.login.contract.RegisterGetRandomContract;
import connect.utils.FileUtil;
import connect.utils.StringUtil;
import connect.utils.permission.PermissionUtil;

/**
 * Registered mobile phone number presenter.
 */
public class RegisterGetRandomPresenter implements RegisterGetRandomContract.Presenter {

    private RegisterGetRandomContract.View mView;
    /**
     * The longest collection time
     */
    private final int MAX_LENGTH = 5000;
    private Runnable runnable;
    private Handler handler = new Handler();
    /**
     * Collection time clock
     */
    private int videoLength;
    /**
     * Update frequency interface
     */
    private final int RATE_TIME = 10;
    private MediaRecorder iMediaRecorder;
    private File file;
    /**
     * Sound amplitude
     */
    private ArrayList<Double> dbArray;
    private HashMap<String, String> hashMap;

    public RegisterGetRandomPresenter(RegisterGetRandomContract.View mView) {
        this.mView = mView;
        mView.setPresenter(this);
    }

    @Override
    public void start() {
        chechPeission();
    }

    private void chechPeission() {
        PermissionUtil.getInstance().requestPermission(mView.getActivity(),
                new String[]{PermissionUtil.PERMISSION_RECORD_AUDIO, PermissionUtil.PERMISSION_STORAGE}, permissionCallBack);
    }

    @Override
    public PermissionUtil.ResultCallBack getPermissomCallBack() {
        return permissionCallBack;
    }

    private PermissionUtil.ResultCallBack permissionCallBack = new PermissionUtil.ResultCallBack() {
        @Override
        public void granted(String[] permissions) {
            startRecorder();
        }

        @Override
        public void deny(String[] permissions) {
            mView.denyPression();
        }
    };

    private void startRecorder() {
        try {
            file = FileUtil.newTempFile(FileUtil.FileType.VOICE);
            iMediaRecorder = new MediaRecorder();

            iMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            iMediaRecorder.setOutputFile(file.getPath());
            iMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            iMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

            try {
                iMediaRecorder.prepare();
                iMediaRecorder.start();
                timing();
                mView.changeViewStatus(0);
            } catch (Exception e) {
                iMediaRecorder = null;
                mView.denyPressionDialog();
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Collect voice countdown
     */
    private void timing() {
        if (iMediaRecorder == null) {
            return;
        }
        dbArray = new ArrayList<Double>();
        videoLength = 0;
        runnable = new Runnable() {
            @Override
            public void run() {
                videoLength += RATE_TIME;
                if (videoLength > MAX_LENGTH) {
                    finishSuccess(hashMap);
                } else {
                    mView.setProgressBar(videoLength * ((float) 360 / MAX_LENGTH));
                    handler.postDelayed(this, RATE_TIME);
                }

                if (videoLength < 3000) {
                    int ratio = iMediaRecorder.getMaxAmplitude();
                    double db = 0;
                    if (ratio > 1) {
                        db = 20 * Math.log10((double) Math.abs(ratio));
                    }
                    dbArray.add(db);
                } else if (videoLength == 3000) {
                    mView.changeViewStatus(1);
                    createPri();
                } else if (videoLength == 4000) {
                    mView.changeViewStatus(2);
                }
            }
        };
        handler.postDelayed(runnable, RATE_TIME);
    }

    @Override
    public void finishSuccess(final HashMap<String, String> hashMap) {
        if (hashMap != null && hashMap.size() == 2) {
            mView.changeViewStatus(3);
            Handler handler = new Handler(Looper.getMainLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    UserBean userBean = new UserBean();
                    mView.goinRegister(userBean);
                }
            };
            handler.sendEmptyMessageDelayed(1, 1000);
        } else {
            mView.changeViewStatus(4);
        }
    }

    /**
     * Voice of random Numbers generated the private key
     */
    private void createPri() {
        if (!checkVoice()) {
            mView.changeViewStatus(4);
            return;
        }

        new AsyncTask<Void, Void, ArrayList>() {
            @Override
            protected ArrayList doInBackground(Void... params) {
                if (iMediaRecorder != null) {
                    iMediaRecorder.stop();
                    iMediaRecorder.release();
                    iMediaRecorder = null;
                }
                ArrayList arrayList = new ArrayList<String>();
//                byte[] valueByte = SupportKeyUril.byteSHA512(FileUtil.filePathToByteArray(file.getPath()));
//                String random = StringUtil.bytesToHexString(SupportKeyUril.xor(valueByte, SecureRandom.getSeed(64)));
                String prikey = "";
                arrayList.add(prikey);
                String pubKey = "";
                arrayList.add(pubKey);
                return arrayList;
            }

            @Override
            protected void onPostExecute(ArrayList arrayList) {
                super.onPostExecute(arrayList);
                hashMap = new HashMap<>();
                hashMap.put("priKey", (String) arrayList.get(0));
                hashMap.put("pubKey", (String) arrayList.get(1));
            }
        }.execute();
    }

    /**
     * Check if the voice randomness enough
     *
     * @return Whether or not enough
     */
    private boolean checkVoice() {
        ArrayList<Double> list = new ArrayList<Double>();
        for (Double db : dbArray) {
            if (db != 0.0) {
                list.add(db);
            }
        }
        if (list.size() < 20) {
            mView.changeViewStatus(4);
            return false;
        }
        double sum = 0;
        for (Double data : list) {
            sum = data + sum;
        }
        if (sum / list.size() > 35) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void releaseResource() {
        try {
            if (handler != null && runnable != null) {
                handler.removeCallbacks(runnable);
                runnable = null;
            }

            if (iMediaRecorder != null) {
                iMediaRecorder.setOnErrorListener(null);
                iMediaRecorder.setOnInfoListener(null);
                iMediaRecorder.setPreviewDisplay(null);
                iMediaRecorder.stop();
                iMediaRecorder.release();
                iMediaRecorder = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
