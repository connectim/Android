package connect.activity.contact.model;

import connect.activity.contact.bean.PhoneContactBean;

public class PhoneListComparatorSort extends BaseListComparatorSort<PhoneContactBean>{
    @Override
    public String getName(PhoneContactBean phoneContactBean) {

        return phoneContactBean.getName();
    }
}
