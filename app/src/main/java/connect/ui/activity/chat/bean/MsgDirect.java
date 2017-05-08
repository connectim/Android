package connect.ui.activity.chat.bean;

/**
 * Created by gtq on 2016/11/24.
 */
public enum MsgDirect {
    From(-1),
    To(1);

    public int dirct;

    MsgDirect(int dirct) {
        this.dirct = dirct;
    }

    public static MsgDirect toDirect(int dirct) {
        return dirct == From.dirct ? From : To;
    }
}