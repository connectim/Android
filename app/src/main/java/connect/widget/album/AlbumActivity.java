package connect.widget.album;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import connect.activity.base.BaseFragmentActivity;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.permission.PermissionUtil;
import connect.widget.album.contract.AlbumContract;
import connect.widget.album.model.AlbumFile;
import connect.widget.album.model.AlbumFolder;
import connect.widget.album.model.AlbumFolderType;
import connect.widget.album.presenter.AlbumPresenter;

/**
 * Created by Administrator on 2017/8/21.
 */

public class AlbumActivity extends BaseFragmentActivity implements AlbumContract.BView{

    @Bind(R.id.framelayout)
    FrameLayout framelayout;
    @Bind(R.id.txt1)
    TextView txt1;

    private AlbumActivity activity;
    /** External incoming request code */
    private int requestCode;
    /** Maximum number of choices */
    private int maxSelect;


    /** request code */
    public final static int OPEN_ALBUM_CODE = 502;

    /** The selected image list */
    private Map<String,AlbumFile> imageInfoMap = new HashMap<>();
    private List<AlbumFolder> folderInfos;
    private ArrayList<AlbumFile> albumFiles = new ArrayList<>();

    private AlbumContract.Presenter presenter;
    private AlbumFolderType albumFolderType;

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
        albumFolderType = (maxSelect == 1 ? AlbumFolderType.Photo : AlbumFolderType.All);

        new AlbumPresenter(activity).start();
        PermissionUtil.getInstance().requestPermission(activity, new String[]{PermissionUtil.PERMISSION_STORAGE}, permissomCallBack);
    }

    private PermissionUtil.ResultCallBack permissomCallBack = new PermissionUtil.ResultCallBack() {

        @Override
        public void granted(String[] permissions) {
            presenter.albumScan(new AlbumPresenter.OnScanListener() {
                @Override
                public void onScanFinish(List<AlbumFolder> infoList) {
                    if (infoList != null) {
                        folderInfos = infoList;
                        albumFiles = infoList.get(0).getAlbumFiles();

                        presenter.gridAlbumFragment();
                    }
                }
            });
        }

        @Override
        public void deny(String[] permissions) {
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionUtil.getInstance().onRequestPermissionsResult(activity,requestCode,permissions,grantResults,permissomCallBack);
    }

    public void sendImgInfos() {
        ArrayList<AlbumFile> albumFiles = new ArrayList<>();
        for (AlbumFile albumFile : imageInfoMap.values()) {
            if (albumFile.getSize() > 1024 * 1024 * 10) {
                continue;
            }
            albumFiles.add(albumFile);
        }
        Bundle bundle = new Bundle();
        bundle.putSerializable("list", albumFiles);
        ActivityUtil.goBackWithResult(activity, requestCode, bundle);
    }

    public void albumFolderDialog() {
        presenter.albumFolderDialog();
    }

    /**
     * A number of selected images
     * @return true:You can also choose false:Can't choose
     */
    public boolean canSelectImg() {
        return maxSelect > getSelectSize();
    }

    public void updateSelectInfos(boolean add, AlbumFile albumFile) {
        albumFile.setChecked(add);
        if (add) {
            imageInfoMap.put(albumFile.getPath(),albumFile);
        } else {
            imageInfoMap.remove(albumFile.getPath());
        }
    }

    public int getSelectSize() {
        return imageInfoMap.size();
    }

    public boolean isSelectImg(AlbumFile file){
        return imageInfoMap.containsKey(file.getPath());
    }

    public List<AlbumFile> getSelectInfoList() {
        List<AlbumFile> selectInfos = new ArrayList<>();
        for (AlbumFile albumFile : imageInfoMap.values()) {
            selectInfos.add(albumFile);
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

    @Override
    public AlbumContract.Presenter getPresenter() {
        return presenter;
    }

    public AlbumFolderType getAlbumFolderType() {
        return albumFolderType;
    }

    @Override
    public List<AlbumFolder> getAlbumFolders() {
        return folderInfos;
    }

    @Override
    public ArrayList<AlbumFile> getAlbumFiles() {
        return albumFiles;
    }

    @Override
    public void setAlbumFiles(ArrayList<AlbumFile> albumFiles) {
        this.albumFiles = albumFiles;
    }
}
