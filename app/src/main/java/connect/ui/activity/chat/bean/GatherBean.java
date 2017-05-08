package connect.ui.activity.chat.bean;

import java.io.Serializable;

/** gather Ext1
 * Created by gtq on 2016/12/21.
 */
public class GatherBean implements Serializable{
    private String hashid;
    private long amount;
    private int totalMember;
    private boolean isCrowdfundRceipt;
    private String note;

    public GatherBean(String hashid, long amount, int totalMember, boolean isCrowdfundRceipt, String note) {
        this.hashid = hashid;
        this.amount = amount;
        this.totalMember = totalMember;
        this.isCrowdfundRceipt = isCrowdfundRceipt;
        this.note = note;
    }

    public String getHashid() {
        return hashid;
    }

    public void setHashid(String hashid) {
        this.hashid = hashid;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public int getTotalMember() {
        return totalMember;
    }

    public void setTotalMember(int totalMember) {
        this.totalMember = totalMember;
    }

    public boolean getIsCrowdfundRceipt() {
        return isCrowdfundRceipt;
    }

    public void setIsCrowdfundRceipt(boolean isCrowdfundRceipt) {
        this.isCrowdfundRceipt = isCrowdfundRceipt;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
