package connect.database.green;

import android.database.Cursor;

import connect.database.green.bean.DaoSession;

/**
 * Created by gtq on 2016/11/22.
 */
public class BaseDao {
    public DaoManager manager;
    public DaoSession daoSession;

    public BaseDao() {
        manager = DaoManager.getInstance();
        daoSession = manager.getDaoSession();
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
}
