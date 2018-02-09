package connect.database.green.DaoHelper.mergin;

import android.database.Cursor;

/**
 * Created by Administrator on 2017/8/17.
 */

public abstract class MigrateVerisonHelper {

    abstract void migrate();

    public String cursorGetString(Cursor cursor, String indexname) {
        String index = "";
        int indexPosi = cursor.getColumnIndex(indexname);
        if (0 > indexPosi) {
            index = "";
        } else {
            index = cursor.getString(indexPosi);
        }
        return index;
    }

    public int cursorGetInt(Cursor cursor, String indexname) {
        int index = 0;
        int indexPosi = cursor.getColumnIndex(indexname);
        if (0 > indexPosi) {
            index = 0;
        } else {
            index = cursor.getInt(indexPosi);
        }
        return index;
    }

    public long cursorGetLong(Cursor cursor, String indexname) {
        long index = 0L;
        int indexPosi = cursor.getColumnIndex(indexname);
        if (0 > indexPosi) {
            index = 0L;
        } else {
            index = cursor.getLong(indexPosi);
        }
        return index;
    }
}
