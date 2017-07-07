package connect.utils.glide;

/**
 * The callback to download images
 * Created by Administrator on 2017/4/6 0006.
 */

public abstract class OnDownloadTarget{

    public abstract void finish(String path);

    public void error(){}

}
