package connect.database.green.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;

import connect.database.green.bean.TransactionEntity;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "TRANSACTION_ENTITY".
*/
public class TransactionEntityDao extends AbstractDao<TransactionEntity, Long> {

    public static final String TABLENAME = "TRANSACTION_ENTITY";

    /**
     * Properties of entity TransactionEntity.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property _id = new Property(0, Long.class, "_id", true, "_id");
        public final static Property Message_id = new Property(1, String.class, "message_id", false, "MESSAGE_ID");
        public final static Property Hashid = new Property(2, String.class, "hashid", false, "HASHID");
        public final static Property Status = new Property(3, Integer.class, "status", false, "STATUS");
        public final static Property Pay_count = new Property(4, Integer.class, "pay_count", false, "PAY_COUNT");
        public final static Property Crowd_count = new Property(5, Integer.class, "crowd_count", false, "CROWD_COUNT");
    }


    public TransactionEntityDao(DaoConfig config) {
        super(config);
    }
    
    public TransactionEntityDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"TRANSACTION_ENTITY\" (" + //
                "\"_id\" INTEGER PRIMARY KEY AUTOINCREMENT ," + // 0: _id
                "\"MESSAGE_ID\" TEXT NOT NULL ," + // 1: message_id
                "\"HASHID\" TEXT NOT NULL UNIQUE ," + // 2: hashid
                "\"STATUS\" INTEGER," + // 3: status
                "\"PAY_COUNT\" INTEGER," + // 4: pay_count
                "\"CROWD_COUNT\" INTEGER);"); // 5: crowd_count
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"TRANSACTION_ENTITY\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, TransactionEntity entity) {
        stmt.clearBindings();
 
        Long _id = entity.get_id();
        if (_id != null) {
            stmt.bindLong(1, _id);
        }
        stmt.bindString(2, entity.getMessage_id());
        stmt.bindString(3, entity.getHashid());
 
        Integer status = entity.getStatus();
        if (status != null) {
            stmt.bindLong(4, status);
        }
 
        Integer pay_count = entity.getPay_count();
        if (pay_count != null) {
            stmt.bindLong(5, pay_count);
        }
 
        Integer crowd_count = entity.getCrowd_count();
        if (crowd_count != null) {
            stmt.bindLong(6, crowd_count);
        }
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, TransactionEntity entity) {
        stmt.clearBindings();
 
        Long _id = entity.get_id();
        if (_id != null) {
            stmt.bindLong(1, _id);
        }
        stmt.bindString(2, entity.getMessage_id());
        stmt.bindString(3, entity.getHashid());
 
        Integer status = entity.getStatus();
        if (status != null) {
            stmt.bindLong(4, status);
        }
 
        Integer pay_count = entity.getPay_count();
        if (pay_count != null) {
            stmt.bindLong(5, pay_count);
        }
 
        Integer crowd_count = entity.getCrowd_count();
        if (crowd_count != null) {
            stmt.bindLong(6, crowd_count);
        }
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    @Override
    public TransactionEntity readEntity(Cursor cursor, int offset) {
        TransactionEntity entity = new TransactionEntity( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // _id
            cursor.getString(offset + 1), // message_id
            cursor.getString(offset + 2), // hashid
            cursor.isNull(offset + 3) ? null : cursor.getInt(offset + 3), // status
            cursor.isNull(offset + 4) ? null : cursor.getInt(offset + 4), // pay_count
            cursor.isNull(offset + 5) ? null : cursor.getInt(offset + 5) // crowd_count
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, TransactionEntity entity, int offset) {
        entity.set_id(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setMessage_id(cursor.getString(offset + 1));
        entity.setHashid(cursor.getString(offset + 2));
        entity.setStatus(cursor.isNull(offset + 3) ? null : cursor.getInt(offset + 3));
        entity.setPay_count(cursor.isNull(offset + 4) ? null : cursor.getInt(offset + 4));
        entity.setCrowd_count(cursor.isNull(offset + 5) ? null : cursor.getInt(offset + 5));
     }
    
    @Override
    protected final Long updateKeyAfterInsert(TransactionEntity entity, long rowId) {
        entity.set_id(rowId);
        return rowId;
    }
    
    @Override
    public Long getKey(TransactionEntity entity) {
        if(entity != null) {
            return entity.get_id();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(TransactionEntity entity) {
        return entity.get_id() != null;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}
