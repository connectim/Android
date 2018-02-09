package connect.database.green.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;

import connect.database.green.bean.ConversionEntity;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "CONVERSION_ENTITY".
*/
public class ConversionEntityDao extends AbstractDao<ConversionEntity, Long> {

    public static final String TABLENAME = "CONVERSION_ENTITY";

    /**
     * Properties of entity ConversionEntity.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property _id = new Property(0, Long.class, "_id", true, "_id");
        public final static Property Identifier = new Property(1, String.class, "identifier", false, "IDENTIFIER");
        public final static Property Type = new Property(2, Integer.class, "type", false, "TYPE");
        public final static Property Name = new Property(3, String.class, "name", false, "NAME");
        public final static Property Avatar = new Property(4, String.class, "avatar", false, "AVATAR");
        public final static Property Draft = new Property(5, String.class, "draft", false, "DRAFT");
        public final static Property Content = new Property(6, String.class, "content", false, "CONTENT");
        public final static Property Unread_count = new Property(7, Integer.class, "unread_count", false, "UNREAD_COUNT");
        public final static Property Unread_at = new Property(8, Integer.class, "unread_at", false, "UNREAD_AT");
        public final static Property Unread_attention = new Property(9, Integer.class, "unread_attention", false, "UNREAD_ATTENTION");
        public final static Property Top = new Property(10, Integer.class, "top", false, "TOP");
        public final static Property Last_time = new Property(11, Long.class, "last_time", false, "LAST_TIME");
    }


    public ConversionEntityDao(DaoConfig config) {
        super(config);
    }
    
    public ConversionEntityDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"CONVERSION_ENTITY\" (" + //
                "\"_id\" INTEGER PRIMARY KEY AUTOINCREMENT ," + // 0: _id
                "\"IDENTIFIER\" TEXT NOT NULL UNIQUE ," + // 1: identifier
                "\"TYPE\" INTEGER," + // 2: type
                "\"NAME\" TEXT," + // 3: name
                "\"AVATAR\" TEXT," + // 4: avatar
                "\"DRAFT\" TEXT," + // 5: draft
                "\"CONTENT\" TEXT," + // 6: content
                "\"UNREAD_COUNT\" INTEGER," + // 7: unread_count
                "\"UNREAD_AT\" INTEGER," + // 8: unread_at
                "\"UNREAD_ATTENTION\" INTEGER," + // 9: unread_attention
                "\"TOP\" INTEGER," + // 10: top
                "\"LAST_TIME\" INTEGER);"); // 11: last_time
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"CONVERSION_ENTITY\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, ConversionEntity entity) {
        stmt.clearBindings();
 
        Long _id = entity.get_id();
        if (_id != null) {
            stmt.bindLong(1, _id);
        }
        stmt.bindString(2, entity.getIdentifier());
 
        Integer type = entity.getType();
        if (type != null) {
            stmt.bindLong(3, type);
        }
 
        String name = entity.getName();
        if (name != null) {
            stmt.bindString(4, name);
        }
 
        String avatar = entity.getAvatar();
        if (avatar != null) {
            stmt.bindString(5, avatar);
        }
 
        String draft = entity.getDraft();
        if (draft != null) {
            stmt.bindString(6, draft);
        }
 
        String content = entity.getContent();
        if (content != null) {
            stmt.bindString(7, content);
        }
 
        Integer unread_count = entity.getUnread_count();
        if (unread_count != null) {
            stmt.bindLong(8, unread_count);
        }
 
        Integer unread_at = entity.getUnread_at();
        if (unread_at != null) {
            stmt.bindLong(9, unread_at);
        }
 
        Integer unread_attention = entity.getUnread_attention();
        if (unread_attention != null) {
            stmt.bindLong(10, unread_attention);
        }
 
        Integer top = entity.getTop();
        if (top != null) {
            stmt.bindLong(11, top);
        }
 
        Long last_time = entity.getLast_time();
        if (last_time != null) {
            stmt.bindLong(12, last_time);
        }
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, ConversionEntity entity) {
        stmt.clearBindings();
 
        Long _id = entity.get_id();
        if (_id != null) {
            stmt.bindLong(1, _id);
        }
        stmt.bindString(2, entity.getIdentifier());
 
        Integer type = entity.getType();
        if (type != null) {
            stmt.bindLong(3, type);
        }
 
        String name = entity.getName();
        if (name != null) {
            stmt.bindString(4, name);
        }
 
        String avatar = entity.getAvatar();
        if (avatar != null) {
            stmt.bindString(5, avatar);
        }
 
        String draft = entity.getDraft();
        if (draft != null) {
            stmt.bindString(6, draft);
        }
 
        String content = entity.getContent();
        if (content != null) {
            stmt.bindString(7, content);
        }
 
        Integer unread_count = entity.getUnread_count();
        if (unread_count != null) {
            stmt.bindLong(8, unread_count);
        }
 
        Integer unread_at = entity.getUnread_at();
        if (unread_at != null) {
            stmt.bindLong(9, unread_at);
        }
 
        Integer unread_attention = entity.getUnread_attention();
        if (unread_attention != null) {
            stmt.bindLong(10, unread_attention);
        }
 
        Integer top = entity.getTop();
        if (top != null) {
            stmt.bindLong(11, top);
        }
 
        Long last_time = entity.getLast_time();
        if (last_time != null) {
            stmt.bindLong(12, last_time);
        }
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    @Override
    public ConversionEntity readEntity(Cursor cursor, int offset) {
        ConversionEntity entity = new ConversionEntity( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // _id
            cursor.getString(offset + 1), // identifier
            cursor.isNull(offset + 2) ? null : cursor.getInt(offset + 2), // type
            cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3), // name
            cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4), // avatar
            cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5), // draft
            cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6), // content
            cursor.isNull(offset + 7) ? null : cursor.getInt(offset + 7), // unread_count
            cursor.isNull(offset + 8) ? null : cursor.getInt(offset + 8), // unread_at
            cursor.isNull(offset + 9) ? null : cursor.getInt(offset + 9), // unread_attention
            cursor.isNull(offset + 10) ? null : cursor.getInt(offset + 10), // top
            cursor.isNull(offset + 11) ? null : cursor.getLong(offset + 11) // last_time
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, ConversionEntity entity, int offset) {
        entity.set_id(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setIdentifier(cursor.getString(offset + 1));
        entity.setType(cursor.isNull(offset + 2) ? null : cursor.getInt(offset + 2));
        entity.setName(cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3));
        entity.setAvatar(cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4));
        entity.setDraft(cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5));
        entity.setContent(cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6));
        entity.setUnread_count(cursor.isNull(offset + 7) ? null : cursor.getInt(offset + 7));
        entity.setUnread_at(cursor.isNull(offset + 8) ? null : cursor.getInt(offset + 8));
        entity.setUnread_attention(cursor.isNull(offset + 9) ? null : cursor.getInt(offset + 9));
        entity.setTop(cursor.isNull(offset + 10) ? null : cursor.getInt(offset + 10));
        entity.setLast_time(cursor.isNull(offset + 11) ? null : cursor.getLong(offset + 11));
     }
    
    @Override
    protected final Long updateKeyAfterInsert(ConversionEntity entity, long rowId) {
        entity.set_id(rowId);
        return rowId;
    }
    
    @Override
    public Long getKey(ConversionEntity entity) {
        if(entity != null) {
            return entity.get_id();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(ConversionEntity entity) {
        return entity.get_id() != null;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}
