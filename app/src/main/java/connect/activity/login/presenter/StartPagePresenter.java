package connect.activity.login.presenter;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Random;

import connect.activity.login.bean.StartImagesBean;
import connect.activity.login.bean.UserBean;
import connect.activity.login.contract.StartContract;
import connect.database.SharedPreferenceUtil;
import connect.utils.RegularUtil;
import connect.utils.UriUtil;
import connect.utils.glide.GlideUtil;
import connect.utils.glide.OnDownloadTarget;
import connect.utils.okhttp.HttpRequest;
import connect.utils.scan.ResolveUrlUtil;
import connect.utils.system.SystemDataUtil;
import connect.utils.system.SystemUtil;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class StartPagePresenter implements StartContract.Presenter {

    private StartImagesBean imagesBean;
    private String sharedStr;
    private int imageLoadInt;
    private StartContract.View mView;

    public StartPagePresenter(StartContract.View mView) {
        this.mView = mView;
        mView.setPresenter(this);
    }

    @Override
    public void start() {
        String languageCode = SharedPreferenceUtil.getInstance().getStringValue(SharedPreferenceUtil.APP_LANGUAGE_CODE);
        SystemDataUtil.setAppLanguage(mView.getActivity(),languageCode);

        String path = getImagePath();
        mView.setImage(path);

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M){
            requestImages();
        } else if (ContextCompat.checkSelfPermission(mView.getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            requestImages();
        }
        goInActivity(mView.getActivity());
    }

    private String getImagePath() {
        String addressStr = SharedPreferenceUtil.getInstance().getStringValue(SharedPreferenceUtil.START_IMAGES_ADDRESS);
        if(TextUtils.isEmpty(addressStr)){
            return null;
        } else {
            String[] addressArray = addressStr.split(",");
            int index = 0;
            if(addressArray.length > 0){
                Random rand = new Random();
                index = rand.nextInt(addressArray.length);
            }
            return addressArray[index];
        }
    }

    private void goInActivity(final Activity mActivity) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                UserBean userBean = SharedPreferenceUtil.getInstance().getUser();
                if (!SharedPreferenceUtil.getInstance().isContains(SharedPreferenceUtil.FIRST_INTO_APP)) {
                    mView.goIntoGuide();
                } else if (userBean == null) {
                    mView.goIntoLoginForPhone();
                } else {
                    openFromWeb(mActivity);
                    mView.goIntoHome();
                }
                mActivity.finish();
            }
        }).start();
    }

    private void requestImages() {
        String hash = SharedPreferenceUtil.getInstance().getStringValue(SharedPreferenceUtil.START_IMAGES_HASH);
        if (TextUtils.isEmpty(hash)) {
            hash = "connect";
        }
        String url = String.format(UriUtil.LAUNCH_IMAGES, hash);
        HttpRequest.getInstance().get(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String tempResponse = response.body().string();
                int code;
                try {
                    JSONObject jsonObject = new JSONObject(tempResponse);
                    code = jsonObject.getInt("Code");
                    if (code == 2000) {
                        Type type = new TypeToken<StartImagesBean>() {
                        }.getType();
                        StartImagesBean imagesBean = new Gson().fromJson(jsonObject.optString("Data"), type);
                        if (imagesBean.getImages() == null) {
                            //start(1000);
                        } else {
                            saveImagesData(imagesBean);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void saveImagesData(StartImagesBean images) {
        if (!SystemUtil.isOpenWifi()) {
            return;
        }
        this.imagesBean = images;
        ArrayList<String> imageList = new ArrayList<>();
        for (String path : imagesBean.getImages()) {
            if (!imageList.contains(path)) {
                imageList.add(path);
            }
        }
        imagesBean.setImages(imageList);
        sharedStr = "";
        imageLoadInt = 0;
        for (String path : imageList) {
            GlideUtil.downloadImage(path, new OnDownloadTarget() {
                @Override
                public void finish(String path) {
                    if (!TextUtils.isEmpty(path)) {
                        if (TextUtils.isEmpty(sharedStr)) {
                            sharedStr = path;
                        } else {
                            sharedStr = sharedStr + "," + path;
                        }
                        imageLoadInt ++;
                    }
                    if (imageLoadInt == imagesBean.getImages().size() && !TextUtils.isEmpty(sharedStr)) {
                        SharedPreferenceUtil.getInstance().putValue(SharedPreferenceUtil.START_IMAGES_HASH,imagesBean.getHash());
                        SharedPreferenceUtil.getInstance().putValue(SharedPreferenceUtil.START_IMAGES_ADDRESS, sharedStr);
                    }
                }
            });
        }
    }

    /**
     * Save url with open the App.
     * @param activity The activity reference
     */
    private void openFromWeb(Activity activity) {
        Intent intent = activity.getIntent();
        String action = intent.getAction();
        Uri uri = intent.getData();
        if (Intent.ACTION_VIEW.equals(action) && RegularUtil.matches(uri.toString(), ResolveUrlUtil.Web_Url)) {
            SharedPreferenceUtil.getInstance().putValue(SharedPreferenceUtil.WEB_OPEN_APP,uri.toString());
        }
    }
}
