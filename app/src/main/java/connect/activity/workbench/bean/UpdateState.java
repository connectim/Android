package connect.activity.workbench.bean;

/**
 * Created by Administrator on 2018/2/7 0007.
 */

public class UpdateState {

    public enum StatusEnum{
        UPDATE_VISITOR,
        UPDATE_WAREHOUSE,
    }

    private UpdateState.StatusEnum statusEnum;

    public UpdateState() {}

    public UpdateState(StatusEnum statusEnum) {
        this.statusEnum = statusEnum;
    }

    public StatusEnum getStatusEnum() {
        return statusEnum;
    }

}
