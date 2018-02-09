package instant.parser.inter;

/**
 * Created by Administrator on 2017/10/18.
 */
public interface ConnectListener {

    void disConnect();

    void requestLogin();

    void loginSuccess();

    void pullOfflineMessage();

    void connectSuccess();

    void welcome();

    void exceptionConnect();
}
