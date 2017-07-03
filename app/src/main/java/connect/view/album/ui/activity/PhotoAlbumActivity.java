package connect.view.album.ui.activity;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import connect.ui.activity.R;
import connect.ui.base.BaseFragmentActivity;
import connect.utils.ActivityUtil;
import connect.utils.DialogUtil;
import connect.utils.permission.PermissiomUtilNew;
import connect.utils.glide.GlideUtil;
import connect.view.album.entity.AlbumFolderInfo;
import connect.view.album.entity.ImageInfo;
import connect.view.album.presenter.ImageScannerPresenter;
import connect.view.album.presenter.ImageScannerPresenterImpl;
import connect.view.album.view.AlbumView;
import connect.view.album.view.entity.AlbumViewData;

/**
 * Photo viewer
 */
public class PhotoAlbumActivity extends BaseFragmentActivity {

    @Bind(R.id.framelayout)
    FrameLayout framelayout;
    @Bind(R.id.txt1)
    TextView txt1;

    private PhotoAlbumActivity activity;
    /** Select type 0: only select picture 1: picture video*/
    private int selectType;
    /** External incoming request code */
    private int requestCode;
    /** Maximum number of choices */
    private int maxSelect;
    /** Image scanning */
    private ImageScannerPresenter mImageScannerPresenter;
    /** Current album information */
    private AlbumFolderInfo albumFolderInfo;
    /** All albums list */
    private   List<AlbumFolderInfo> mAlbumFolderInfoList;
    /** Show the image list */
    private ArrayList<ImageInfo> imageInfoList=new ArrayList<>();
    /** The selected image list */
    private Map<String,ImageInfo> imageInfoMap = new HashMap<>();
    /** request code */
    public final static int OPEN_ALBUM_CODE = 502;
    private AlbumGridFragment gridFragment;
    private AlbumGalleryFragment galleryFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_album);
        ButterKnife.bind(this);
        initView();
    }

    public static void startActivity(Activity activity, int code) {
        startActivity(activity, code, 9);
    }

    public static void startActivity(Activity activity, int code, int maxSelect) {
        Bundle bundle = new Bundle();
        bundle.putInt("CODE_REQUEST", code);
        bundle.putInt("MAX_SELECT", maxSelect);
        ActivityUtil.next(activity, PhotoAlbumActivity.class, bundle, code);
    }

    @Override
    public void initView() {
        activity=this;
        requestCode = getIntent().getIntExtra("CODE_REQUEST", 0);
        maxSelect = getIntent().getIntExtra("MAX_SELECT", 9);
        selectType = (maxSelect == 1 ? 0 : 1);

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Window window = activity.getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(getResources().getColor(R.color.color_161A21));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        PermissiomUtilNew.getInstance().requestPermissom(activity,new String[]{PermissiomUtilNew.PERMISSIM_STORAGE},permissomCallBack);
    }

    private PermissiomUtilNew.ResultCallBack permissomCallBack = new PermissiomUtilNew.ResultCallBack(){
        @Override
        public void granted(String[] permissions) {
            scanPhotoAlbum();
        }

        @Override
        public void deny(String[] permissions) {
        }
    };

    /**
     * Scan photo album
     */
    public void scanPhotoAlbum(){
        mImageScannerPresenter = new ImageScannerPresenterImpl(albumView);
        mImageScannerPresenter.startScanImage(getApplicationContext(), selectType);
    }

    private AlbumView albumView = new AlbumView() {
        /**
         * Album information to complete the scan / switch album photo album
         *
         * @param albumData
         */
        @Override
        public void refreshAlbumData(AlbumViewData albumData) {
            if (albumData != null) {
                mAlbumFolderInfoList = albumData.getAlbumFolderInfoList();
                if (mAlbumFolderInfoList.size() == 0) {
                    gridListInfos();
                } else {
                    albumFolderInfo = mAlbumFolderInfoList.get(0);
                    switchAlbumFolder(albumFolderInfo);
                }
            }
        }

        /**
         * Switch the album
         * @param albumFolderInfo Specify the images directory information
         */
        @Override
        public void switchAlbumFolder(AlbumFolderInfo albumFolderInfo) {
            if (albumFolderInfo != null) {
                imageInfoList = albumFolderInfo.getImageInfoList();
                gridListInfos();
            }
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissiomUtilNew.getInstance().onRequestPermissionsResult(activity,requestCode,permissions,grantResults,permissomCallBack);
    }

    public void sendImgInfos() {
        ArrayList<ImageInfo> imageInfos = new ArrayList<>();
        for (ImageInfo info : imageInfoMap.values()) {
            if (info.getImageFile().length() > 1024 * 1024 * 10) {
                continue;
            }
            imageInfos.add(info);
        }
        Bundle bundle = new Bundle();
        bundle.putSerializable("list", imageInfos);
        ActivityUtil.goBackWithResult(activity, requestCode, bundle);
    }

    public void popUpAlbumSelectDialog(){
        DialogUtil.showAlertListView(activity, null, albumFolderAdapter, new DialogUtil.DialogListItemLongClickListener() {
            @Override
            public void onClick(AdapterView<?> parent, View view, int position) {
                AlbumFolderInfo albumFolderInfo = mAlbumFolderInfoList.get(position);
                gridFragment.setTitle(albumFolderInfo.getFolderName());
                albumView.switchAlbumFolder(albumFolderInfo);
            }

            @Override
            public void onLongClick(AdapterView<?> parent, View view, int position) {

            }
        });
    }

    /**
     * View the photo album pictures
     */
    public void gridListInfos() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        if(galleryFragment!=null){
            fragmentTransaction.hide(galleryFragment);
        }
        if (gridFragment == null) {
            gridFragment = AlbumGridFragment.newInstance();
            fragmentTransaction.add(R.id.framelayout, gridFragment);
        } else {
            gridFragment.setData();
            fragmentTransaction.show(gridFragment);
        }
        //java.lang.IllegalStateException: Can not perform this action after onSaveInstanceState
        fragmentTransaction.commit();
    }

    /**
     * Preview the selected images
     * @param isselect true :Preview the selected image false: Preview all images
     */
    public void preViewInfos(boolean isselect, int postion) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        if (gridFragment != null) {
            fragmentTransaction.hide(gridFragment);
        }
        if (galleryFragment == null) {
            galleryFragment = AlbumGalleryFragment.newInstance();
            fragmentTransaction.add(R.id.framelayout, galleryFragment);
        } else {
            fragmentTransaction.show(galleryFragment);
        }

        galleryFragment.setPreViewState(isselect, postion);
        fragmentTransaction.commit();
    }

    /**
     * A number of selected images
     * @return true:You can also choose false:Can't choose
     */
    public boolean canSelectImg() {
        return maxSelect > getSelectSize();
    }

    public void updateSelectInfos(boolean add, ImageInfo info) {
        if (add) {
            imageInfoMap.put(info.getImageFile().getAbsolutePath(),info);
        } else {
            imageInfoMap.remove(info.getImageFile().getAbsolutePath());
        }
    }

    public int getSelectSize() {
        return imageInfoMap.size();
    }

    public ArrayList<ImageInfo> getImageInfoList() {
        return imageInfoList;
    }

    public boolean isSelectImg(ImageInfo info){
        return imageInfoMap.containsKey(info.getImageFile().getAbsolutePath());
    }

    public List<ImageInfo> getSelectInfoList(){
        List<ImageInfo> selectInfos=new ArrayList<>();
        for(ImageInfo info:imageInfoMap.values()){
            selectInfos.add(info);
        }
        return selectInfos;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            ActivityUtil.goBack(activity);
        }
        return super.onKeyDown(keyCode, event);
    }

    public BaseAdapter albumFolderAdapter = new BaseAdapter() {

        @Override
        public int getCount() {
            int size = 0;
            if (mAlbumFolderInfoList != null) {
                size = mAlbumFolderInfoList.size();
            }
            return size;
        }

        @Override
        public AlbumFolderInfo getItem(int position) {
            return mAlbumFolderInfoList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                convertView = View.inflate(parent.getContext(), R.layout.album_directory_item, null);
                holder = new ViewHolder();
                holder.ivAlbumCover = (ImageView) convertView.findViewById(R.id.iv_album_cover);
                holder.videoStateImg = (ImageView) convertView.findViewById(R.id.img2);
                holder.tvDirectoryName = (TextView) convertView.findViewById(R.id.tv_directory_name);
                holder.tvChildCount = (TextView) convertView.findViewById(R.id.tv_child_count);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            AlbumFolderInfo albumFolderInfo = mAlbumFolderInfoList.get(position);

            File frontCover = albumFolderInfo.getFrontCover();
            GlideUtil.loadAvater(holder.ivAlbumCover, frontCover.getAbsolutePath());
            String folderName = albumFolderInfo.getFolderName();
            holder.tvDirectoryName.setText(folderName);

            if (albumFolderInfo.getAlbumType() == 0) {
                holder.videoStateImg.setVisibility(View.GONE);
            } else {
                holder.videoStateImg.setVisibility(View.VISIBLE);
            }

            List<ImageInfo> imageInfoList = albumFolderInfo.getImageInfoList();
            holder.tvChildCount.setText(imageInfoList.size() + "");
            return convertView;
        }
    };

    private static class ViewHolder {
        ImageView ivAlbumCover;
        ImageView videoStateImg;
        TextView tvDirectoryName;
        TextView tvChildCount;
    }

    public int getMaxSelect() {
        return maxSelect;
    }
}
