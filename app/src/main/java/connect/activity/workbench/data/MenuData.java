package connect.activity.workbench.data;

import connect.ui.activity.R;

/**
 * Created by Administrator on 2018/1/16 0016.
 */

public class MenuData {

    private static MenuData menuData = null;

    public static MenuData getInstance() {
        if (null == menuData) {
            menuData = new MenuData();
        }
        return menuData;
    }

    public MenuBean getData(String code){
        MenuBean menuBean = new MenuBean();
        menuBean.setCode(code);
        /*|  1 | 出差   | business_trip  |     | NULL | #      |        1 |        1 |      1 |       NULL |       NULL |
        |  2 | 请假   | leave          |     | NULL | #      |        2 |        1 |      1 |       NULL |       NULL |
        |  3 | 报销   | reimbursement  |     | NULL | #      |        3 |        1 |      1 |       NULL |       NULL |
        |  4 | 审批   | examine        |     | NULL | #      |        4 |        1 |      1 |       NULL |       NULL |
        |  5 | 餐补   | meal_allowance |     | NULL | #      |        5 |        1 |      1 |       NULL |       NULL |
        |  6 | 招聘   | recruit        |     | NULL | #      |        6 |        1 |      1 |       NULL |       NULL |
        |  7 | 合同   | contract       |     | NULL | #      |        7 |        1 |      1 |       NULL |       NULL |
        |  8 | 工资   | salary         |     | NULL | #      |        8 |        1 |      1 |       NULL |       NULL |*/

        switch (code){
            case "business_trip":
                menuBean.setTextId(R.string.Work_On_a_business_trip);
                menuBean.setIconId(R.mipmap.menu_business_trip);
                break;
            case "leave":
                menuBean.setTextId(R.string.Work_Ask_for_leave);
                menuBean.setIconId(R.mipmap.menu_ask_leave);
                break;
            case "reimbursement":
                menuBean.setTextId(R.string.Work_To_submit_an_expense_account);
                menuBean.setIconId(R.mipmap.menu_expense_account);
                break;
            case "examine":
                menuBean.setTextId(R.string.Work_The_examination_and_approval);
                menuBean.setIconId(R.mipmap.menu_approval);
                break;
            case "meal_allowance":
                menuBean.setTextId(R.string.Work_Subsidized_meals);
                menuBean.setIconId(R.mipmap.menu_meals);
                break;
            case "recruit":
                menuBean.setTextId(R.string.Work_Recruitment);
                menuBean.setIconId(R.mipmap.menu_recruitment);
                break;
            case "contract":
                menuBean.setTextId(R.string.Work_The_contract);
                menuBean.setIconId(R.mipmap.menu_contract);
                break;
            case "salary":
                menuBean.setTextId(R.string.Work_Wage);
                menuBean.setIconId(R.mipmap.menu_wage);
                break;
            case "visitors":
                menuBean.setTextId(R.string.Work_Visitors);
                menuBean.setIconId(R.mipmap.menu_visitors);
                break;
            case "add":
                menuBean.setTextId(R.string.Work_Add);
                menuBean.setIconId(R.mipmap.menu_add);
                break;
            default:
                break;
        }
        return menuBean;
    }

}