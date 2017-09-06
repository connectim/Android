package connect.widget.album.presenter;

import android.app.Activity;
import android.app.Dialog;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;
import connect.activity.base.BaseFragmentActivity;
import connect.ui.activity.R;
import connect.utils.system.SystemDataUtil;
import connect.utils.system.SystemUtil;
import connect.widget.album.adapter.AlbumFolderAdapter;
import connect.widget.album.contract.AlbumContract;
import connect.widget.album.model.AlbumFolder;
import connect.widget.album.fragment.AlbumPreviewFragment;
import connect.widget.album.fragment.AlbumListFragment;
import connect.widget.album.model.AlbumScanner;

/**
 * Created by Administrator on 2017/8/21.
 */
public class AlbumPresenter implements AlbumContract.Presenter {

    private Activity activity;
    private AlbumContract.BView bView;

    private AlbumListFragment gridFragment;
    private AlbumPreviewFragment galleryFragment;
    private AlbumScanner scannerModel;

    public AlbumPresenter(AlbumContract.BView view) {
        this.bView = view;
        this.bView.setPresenter(this);
    }

    @Override
    public void start() {
        activity = bView.getActivity();
        scannerModel = new AlbumScanner();
    }

    @Override
    public void albumScan(OnScanListener listener) {
        scannerModel.startScanAlbum(activity, bView.getAlbumFolderType(), listener);
    }

    public interface OnScanListener {

        void onScanFinish(List<AlbumFolder> infoList);
    }

    @Override
    public void gridAlbumFragment() {
        FragmentManager fragmentManager = ((BaseFragmentActivity) activity).getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        if (galleryFragment != null) {
            fragmentTransaction.hide(galleryFragment);
        }
        if (gridFragment == null) {
            gridFragment = AlbumListFragment.newInstance();
            fragmentTransaction.add(R.id.framelayout, gridFragment);
        } else {
            gridFragment.loadData();
            fragmentTransaction.show(gridFragment);
        }
        //java.lang.IllegalStateException: Can not perform this action after onSaveInstanceState
        fragmentTransaction.commit();
    }

    @Override
    public void galleyFragment(boolean select, int postion) {
        FragmentManager fragmentManager = ((BaseFragmentActivity)activity).getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        if (gridFragment != null) {
            fragmentTransaction.hide(gridFragment);
        }
        if (galleryFragment == null) {
            galleryFragment = AlbumPreviewFragment.newInstance();
            fragmentTransaction.add(R.id.framelayout, galleryFragment);
        } else {
            fragmentTransaction.show(galleryFragment);
        }

        galleryFragment.setPreViewState(select, postion);
        fragmentTransaction.commit();
    }

    @Override
    public void albumFolderDialog() {
        final Dialog dialog = new Dialog(activity, R.style.Dialog);
        LayoutInflater inflater = LayoutInflater.from(activity);
        View view = inflater.inflate(R.layout.view_list, null);
        dialog.setContentView(view);
        TextView titleTextView = (TextView) view.findViewById(R.id.title_tv);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(activity);
        recyclerView.setLayoutManager(linearLayoutManager);
        LinearLayout list_lin = (LinearLayout) view.findViewById(R.id.list_lin);

        final List<AlbumFolder> albumFolders = bView.getAlbumFolders();
        AlbumFolderAdapter folderAdapter = new AlbumFolderAdapter(albumFolders);
        if (folderAdapter != null) {
            list_lin.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.VISIBLE);
            recyclerView.setAdapter(folderAdapter);
        }

        ViewGroup.LayoutParams layoutParams = null;
        if (folderAdapter.getItemCount() >= 5) {
            layoutParams = new LinearLayout.LayoutParams(SystemDataUtil.getScreenWidth(),
                    SystemUtil.dipToPx(450));
        } else {
            layoutParams = new LinearLayout.LayoutParams(SystemDataUtil.getScreenWidth(),
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        list_lin.setLayoutParams(layoutParams);

        titleTextView.setVisibility(View.GONE);
        folderAdapter.setItemClickListener(new AlbumFolderAdapter.OnItemClickListener() {

            @Override
            public void itemClick(int position) {
                AlbumFolder albumFolder = albumFolders.get(position);
                gridFragment.setTitle(albumFolder.getFolderName());
                bView.setAlbumFiles(albumFolder.getAlbumFiles());

                gridAlbumFragment();
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
}
