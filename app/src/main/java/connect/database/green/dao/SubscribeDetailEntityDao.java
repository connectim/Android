package connect.database.green.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;

import connect.database.green.bean.SubscribeDetailEntity;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "SUBSCRIBE_DETAIL_ENTITY".
*/
public class SubscribeDetailEntityDao extends AbstractDao<SubscribeDetailEntity, Long> {

    public static final String TABLENAME = "SUBSCRIBE_DETAIL_ENTITY";

    /**
     * Properties of entity SubscribeDetailEntity.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property _id = new Property(0, Long.class, "_id", true, "_id");
        public final static Property MessageId = new Property(1, long.class, "messageId", false, "MESSAGE_ID");
        public final static Property RssId = new Property(2, long.class, "rssId", false, "RSS_ID");
        public final static Property Category = new Property(3, Integer.class, "category", false, "CATEGORY");
        public final static Property Content = new Property(4, String.class, "content", false, "CONTENT");
    }


    public SubscribeDetailEntityDao(DaoConfig config) {
        super(config);
    }
    
    public SubscribeDetailEntityDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"SUBSCRIBE_DETAIL_ENTITY\" (" + //
                "\"_id\" INTEGER PRIMARY KEY AUTOINCREMENT ," + // 0: _id
                "\"MESSAGE_ID\" INTEGER NOT NULL UNIQUE ," + // 1: messageId
                "\"RSS_ID\" INTEGER NOT NULL ," + // 2: rssId
                "\"CATEGORY\" INTEGER," + // 3: category
                "\"CONTENT\" TEXT);"); // 4: content
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"SUBSCRIBE_DETAIL_ENTITY\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, SubscribeDetailEntity entity) {
        stmt.clearBindings();
 
        Long _id = entity.get_id();
        if (_id != null) {
            stmt.bindLong(1, _id);
        }
        stmt.bindLong(2, entity.getMessageId());
        stmt.bindLong(3, entity.getRssId());
 
        Integer category = entity.getCategory();
        if (category != null) {
            stmt.bindLong(4, category);
        }
 
        String content = entity.getContent();
        if (content != null) {
            stmt.bindString(5, content);
        }
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, SubscribeDetailEntity entity) {
        stmt.clearBindings();
 
        Long _id = entity.get_id();
        if (_id != null) {
            stmt.bindLong(1, _id);
        }
        stmt.bindLong(2, entity.getMessageId());
        stmt.bindLong(3, entity.getRssId());
 
        Integer category = entity.getCategory();
        if (category != null) {
            stmt.bindLong(4, category);
        }
 
        String content = entity.getContent();
        if (content != null) {
            stmt.bindString(5, content);
        }
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    @Override
    public SubscribeDetailEntity readEntity(Cursor cursor, int offset) {
        SubscribeDetailEntity entity = new SubscribeDetailEntity( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // _id
            cursor.getLong(offset + 1), // messageId
            cursor.getLong(offset + 2), // rssId
            cursor.isNull(offset + 3) ? null : cursor.getInt(offset + 3), // category
            cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4) // content
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, SubscribeDetailEntity entity, int offset) {
        entity.set_id(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setMessageId(cursor.getLong(offset + 1));
        entity.setRssId(cursor.getLong(offset + 2));
        entity.setCategory(cursor.isNull(offset + 3) ? null : cursor.getInt(offset + 3));
        entity.setContent(cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4));
     }
    
    @Override
    protected final Long updateKeyAfterInsert(SubscribeDetailEntity entity, long rowId) {
        entity.set_id(rowId);
        return rowId;
    }
    
    @Override
    public Long getKey(SubscribeDetailEntity entity) {
        if(entity != null) {
            return entity.get_id();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(SubscribeDetailEntity entity) {
        return entity.get_id() != null;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}
