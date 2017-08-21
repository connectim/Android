package connect.widget.album;

import android.app.Activity;
import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import connect.activity.base.BaseFragmentActivity;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.glide.GlideUtil;
import connect.utils.permission.PermissionUtil;
import connect.utils.system.SystemDataUtil;
import connect.utils.system.SystemUtil;
import connect.widget.album.contract.AlbumContract;
import connect.widget.album.entity.AlbumFolderInfo;
import connect.widget.album.entity.ImageInfo;
import connect.widget.album.presenter.ImageScannerPresenter;
import connect.widget.album.presenter.ImageScannerPresenterImpl;
import connect.widget.album.fragment.AlbumGalleryFragment;
import connect.widget.album.fragment.AlbumGridFragment;
import connect.widget.album.view.AlbumView;
import connect.widget.album.view.entity.AlbumViewData;

/**
 * Created by Administrator on 2017/8/21.
 */

public class AlbumActivity extends BaseFragmentActivity implements AlbumContract.BView{

    @Bind(R.id.framelayout)
    FrameLayout framelayout;
    @Bind(R.id.txt1)
    TextView txt1;

    private AlbumActivity activity;
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
    private List<AlbumFolderInfo> mAlbumFolderInfoList;
    /** Show the image list */
    private ArrayList<ImageInfo> imageInfoList=new ArrayList<>();
    /** The selected image list */
    private Map<String,ImageInfo> imageInfoMap = new HashMap<>();
    /** request code */
    public final static int OPEN_ALBUM_CODE = 502;
    private AlbumGridFragment gridFragment;
    private AlbumGalleryFragment galleryFragment;

    private AlbumContract.Presenter presenter;

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
        ActivityUtil.next(activity, AlbumActivity.class, bundle, code);
    }

    @Override
    public void initView() {
        activity = this;
        requestCode = getIntent().getIntExtra("CODE_REQUEST", 0);
        maxSelect = getIntent().getIntExtra("MAX_SELECT", 9);
        selectType = (maxSelect == 1 ? 0 : 1);

        PermissionUtil.getInstance().requestPermissom(activity, new String[]{PermissionUtil.PERMISSIM_STORAGE}, permissomCallBack);
    }

    private PermissionUtil.ResultCallBack permissomCallBack = new PermissionUtil.ResultCallBack(){

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
        PermissionUtil.getInstance().onRequestPermissionsResult(activity,requestCode,permissions,grantResults,permissomCallBack);
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

    public void popUpAlbumSelectDialog() {
        final Dialog dialog = new Dialog(activity, R.style.Dialog);
        LayoutInflater inflater = LayoutInflater.from(activity);
        View view = inflater.inflate(R.layout.view_list, null);
        dialog.setContentView(view);
        TextView titleTextView = (TextView) view.findViewById(R.id.title_tv);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(activity);
        recyclerView.setLayoutManager(linearLayoutManager);
        LinearLayout list_lin = (LinearLayout) view.findViewById(R.id.list_lin);
        if (albumFolderAdapter != null) {
            list_lin.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.VISIBLE);
            recyclerView.setAdapter(albumFolderAdapter);
        }

        ViewGroup.LayoutParams layoutParams = null;
        if (albumFolderAdapter.getItemCount() >= 5) {
            layoutParams = new LinearLayout.LayoutParams(SystemDataUtil.getScreenWidth(),
                    SystemUtil.dipToPx(450));
        } else {
            layoutParams = new LinearLayout.LayoutParams(SystemDataUtil.getScreenWidth(),
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        list_lin.setLayoutParams(layoutParams);

        titleTextView.setVisibility(View.GONE);
        albumFolderAdapter.setItemClickListener(new AlbumActivity.OnItemClickListener() {
            @Override
            public void itemClick(int position) {
                AlbumFolderInfo albumFolderInfo = mAlbumFolderInfoList.get(position);
                gridFragment.setTitle(albumFolderInfo.getFolderName());
                albumView.switchAlbumFolder(albumFolderInfo);
                dialog.dismiss();
            }
        });

        Window mWindow = dialog.getWindow();
        WindowManager.LayoutParams lp = mWindow.getAttributes();
        lp.width = SystemDataUtil.getScreenWidth();
        mWindow.setGravity(Gravity.BOTTOM);
        mWindow.setWindowAnimations(R.style.DialogAnim);
        mWindow.setAttributes(lp);
        dialog.show();
        dialog.setCanceledOnTouchOutside(true);
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
            gridFragment = new AlbumGridFragment();
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
            galleryFragment = new AlbumGalleryFragment();
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

    public BaseAdapter albumFolderAdapter = new BaseAdapter();


    class BaseAdapter extends RecyclerView.Adapter<ViewHolder> {

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(activity);
            View view = inflater.inflate(R.layout.album_directory_item, parent, false);
            ViewHolder holder = new ViewHolder(view);
            return holder;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
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
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getItemCount() {
            int size = 0;
            if (mAlbumFolderInfoList != null) {
                size = mAlbumFolderInfoList.size();
            }
            return size;
        }

        private OnItemClickListener itemClickListener;

        public void setItemClickListener(OnItemClickListener itemClickListener) {
            this.itemClickListener = itemClickListener;
        }
    }

    public interface OnItemClickListener{
        void itemClick(int position);
    }

    private static class ViewHolder extends RecyclerView.ViewHolder{

        ImageView ivAlbumCover;
        ImageView videoStateImg;
        TextView tvDirectoryName;
        TextView tvChildCount;

        public ViewHolder(View itemView) {
            super(itemView);
            ivAlbumCover = (ImageView) itemView.findViewById(R.id.iv_album_cover);
            videoStateImg = (ImageView) itemView.findViewById(R.id.img2);
            tvDirectoryName = (TextView) itemView.findViewById(R.id.tv_directory_name);
            tvChildCount = (TextView) itemView.findViewById(R.id.tv_child_count);
        }
    }

    public int getMaxSelect() {
        return maxSelect;
    }

    @Override
    public Activity getActivity() {
        return activity;
    }

    @Override
    public void setPresenter(AlbumContract.Presenter presenter) {
        this.presenter = presenter;
    }


}
