package connect.ui.activity.wallet.bean;

import java.io.Serializable;
import java.util.List;

import connect.db.green.bean.ContactEntity;

/**
 * Created by Administrator on 2016/12/22.
 */
public class FriendSeleBean implements Serializable{

    private List<ContactEntity> list;

    public FriendSeleBean(List<ContactEntity> list) {
        this.list = list;
    }

    public List<ContactEntity> getList() {
        return list;
    }

    public void setList(List<ContactEntity> list) {
        this.list = list;
    }
}
