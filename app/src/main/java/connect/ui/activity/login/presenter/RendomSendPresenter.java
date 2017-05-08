package connect.ui.activity.login.presenter;

import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Handler;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import connect.ui.activity.login.contract.RendomSendContract;
import connect.utils.FileUtil;
import connect.utils.permission.PermissiomUtilNew;
import connect.utils.StringUtil;
import connect.utils.cryption.SupportKeyUril;
import connect.wallet.jni.AllNativeMethod;

/**
 * Created by Administrator on 2017/4/14 0014.
 */

public class RendomSendPresenter implements RendomSendContract.Presenter{

    private RendomSendContract.View mView;
    private final int MAX_LENGTH = 5000;
    private Runnable runnable;
    private Handler handler = new Handler();
    private int videoLength;
    private int rateTime = 10;
    private MediaRecorder iMediaRecorder;
    private File file;
    private ArrayList<Double> dbArray;
    private HashMap<String, String> hashMap;

    public RendomSendPresenter(RendomSendContract.View mView) {
        this.mView = mView;
    }

    @Override
    public void start() {
        chechPeission();
    }

    private void chechPeission(){
        PermissiomUtilNew.getInstance().requestPermissom(mView.getActivity(),new String[]{PermissiomUtilNew.PERMISSIM_RECORD_AUDIO,
                PermissiomUtilNew.PERMISSIM_STORAGE},permissomCallBack);
    }

    @Override
    public PermissiomUtilNew.ResultCallBack getPermissomCallBack() {
        return permissomCallBack;
    }

    private PermissiomUtilNew.ResultCallBack permissomCallBack = new PermissiomUtilNew.ResultCallBack(){
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

    private void timing() {
        dbArray = new ArrayList<Double>();
        videoLength = 0;
        runnable = new Runnable() {
            @Override
            public void run() {
                videoLength += rateTime;
                if (videoLength > MAX_LENGTH) {
                    mView.finishSuccess(hashMap);
                } else {
                    mView.setProgressBar(videoLength * ((float) 360 / MAX_LENGTH));
                    handler.postDelayed(this, rateTime);
                }

                if (videoLength < 3000) {
                    int ratio = iMediaRecorder.getMaxAmplitude();
                    double db = 0;
                    if (ratio > 1)
                        db = 20 * Math.log10((double) Math.abs(ratio));
                    dbArray.add(db);
                } else if (videoLength == 3000) {
                    mView.changeViewStatus(1);
                    startCdPri();
                } else if (videoLength == 4000) {
                    mView.changeViewStatus(2);
                }
            }
        };
        handler.postDelayed(runnable, rateTime);
    }

    private void startCdPri() {
        if (!checkVoice()) {
            mView.changeViewStatus(4);
            return;
        }

        new AsyncTask<Void, Void, ArrayList>() {
            @Override
            protected ArrayList doInBackground(Void... params) {
                if(iMediaRecorder != null){
                    iMediaRecorder.stop();
                    iMediaRecorder.release();
                    iMediaRecorder = null;
                }

                ArrayList arrayList = new ArrayList<String>();
                String strForBmp = StringUtil.bytesToHexString(FileUtil.filePathToByteArray(file.getPath()));
                String random = SupportKeyUril.createrPriKeyRandom(strForBmp);
                String prikey = AllNativeMethod.cdGetPrivKeyFromSeedBIP44(random, 44, 0, 0, 0, 0);
                arrayList.add(prikey);

                String pubKey = AllNativeMethod.cdGetPubKeyFromPrivKey(prikey);
                arrayList.add(pubKey);
                String address = AllNativeMethod.cdGetBTCAddrFromPubKey(pubKey);
                arrayList.add(address);
                return arrayList;
            }

            @Override
            protected void onPostExecute(ArrayList arrayList) {
                super.onPostExecute(arrayList);
                hashMap = new HashMap<>();
                hashMap.put("priKey", (String) arrayList.get(0));
                hashMap.put("pubKey", (String) arrayList.get(1));
                hashMap.put("address", (String) arrayList.get(2));
            }
        }.execute();

    }

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
        double Sum = 0;
        for (Double data : list) {
            Sum = data + Sum;
        }
        if (Sum / list.size() > 35) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void releaseResource(){
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
