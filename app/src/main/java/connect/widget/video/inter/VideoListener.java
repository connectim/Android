package connect.widget.video.inter;

import java.io.Serializable;

/**
 * Created by Administrator on 2017/8/21.
 */
public interface VideoListener extends Serializable{

    void onVideoPrepared();

    void onVidePlayFinish();

}
