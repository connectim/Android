package connect.utils.chatfile.inter;

/**
 * Created by Administrator on 2017/10/31.
 */

public interface InterFileDown {

    void successDown(byte[] bytes);

    void failDown();

    void onProgress(long bytesWritten, long totalSize);
}
