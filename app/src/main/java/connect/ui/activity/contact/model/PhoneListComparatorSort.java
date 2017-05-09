package connect.ui.activity.contact.model;

import connect.ui.activity.contact.bean.PhoneContactBean;

public class PhoneListComparatorSort extends BaseListComparatorSort<PhoneContactBean>{
    @Override
    public String getName(PhoneContactBean phoneContactBean) {

        return phoneContactBean.getName();
    }
}
