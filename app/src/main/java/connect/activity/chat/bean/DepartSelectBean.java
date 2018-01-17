package connect.activity.chat.bean;

import java.io.Serializable;

import protos.Connect;

/**
 * Created by PuJin on 2018/1/10.
 */

public class DepartSelectBean implements Serializable {

    private Connect.Department department;
    private Connect.Workmate workmate;

    public DepartSelectBean() {
    }

    public Connect.Department getDepartment() {
        return department;
    }

    public void setDepartment(Connect.Department department) {
        this.department = department;
    }

    public Connect.Workmate getWorkmate() {
        return workmate;
    }

    public void setWorkmate(Connect.Workmate workmate) {
        this.workmate = workmate;
    }
}
