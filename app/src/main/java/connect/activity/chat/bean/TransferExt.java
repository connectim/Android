package connect.activity.chat.bean;

/**
 * Created by pujin on 2017/1/10.
 */
public class TransferExt {
    private long amount;
    private String note;
    private int type;//0:inner 1:outer

    public TransferExt() {
    }

    public TransferExt(long amount, String note, int type) {
        this.amount = amount;
        this.note = note;
        this.type = type;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
