package connect.widget.album.presenter;

import connect.widget.album.contract.AlbumContract;

/**
 * Created by Administrator on 2017/8/21.
 */
public class AlbumPresenter implements AlbumContract.Presenter {

    private AlbumContract.BView view;

    public AlbumPresenter(AlbumContract.BView view) {
        this.view = view;
        this.view.setPresenter(this);
    }

    @Override
    public void start() {

    }
}
