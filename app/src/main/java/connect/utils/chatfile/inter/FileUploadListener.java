package connect.utils.chatfile.inter;

/**
 * Created by Administrator on 2017/10/31.
 */

public interface FileUploadListener {

    void upSuccess(String msgid);

    void uploadFail(int code, String message);
}
