package com.harvey.db.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;

import com.harvey.db.bean.RegisteredFace;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "REGISTERED_FACE".
*/
public class RegisteredFaceDao extends AbstractDao<RegisteredFace, Long> {

    public static final String TABLENAME = "REGISTERED_FACE";

    /**
     * Properties of entity RegisteredFace.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property Person_id = new Property(1, long.class, "person_id", false, "PERSON_ID");
        public final static Property Name = new Property(2, String.class, "name", false, "NAME");
        public final static Property Age = new Property(3, int.class, "age", false, "AGE");
        public final static Property Gender = new Property(4, String.class, "gender", false, "GENDER");
        public final static Property FeatureData = new Property(5, byte[].class, "featureData", false, "FEATURE_DATA");
        public final static Property ImagePath = new Property(6, String.class, "imagePath", false, "IMAGE_PATH");
        public final static Property FaceTime = new Property(7, long.class, "faceTime", false, "FACE_TIME");
    }


    public RegisteredFaceDao(DaoConfig config) {
        super(config);
    }
    
    public RegisteredFaceDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"REGISTERED_FACE\" (" + //
                "\"_id\" INTEGER PRIMARY KEY ," + // 0: id
                "\"PERSON_ID\" INTEGER NOT NULL ," + // 1: person_id
                "\"NAME\" TEXT," + // 2: name
                "\"AGE\" INTEGER NOT NULL ," + // 3: age
                "\"GENDER\" TEXT," + // 4: gender
                "\"FEATURE_DATA\" BLOB," + // 5: featureData
                "\"IMAGE_PATH\" TEXT," + // 6: imagePath
                "\"FACE_TIME\" INTEGER NOT NULL );"); // 7: faceTime
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"REGISTERED_FACE\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, RegisteredFace entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
        stmt.bindLong(2, entity.getPerson_id());
 
        String name = entity.getName();
        if (name != null) {
            stmt.bindString(3, name);
        }
        stmt.bindLong(4, entity.getAge());
 
        String gender = entity.getGender();
        if (gender != null) {
            stmt.bindString(5, gender);
        }
 
        byte[] featureData = entity.getFeatureData();
        if (featureData != null) {
            stmt.bindBlob(6, featureData);
        }
 
        String imagePath = entity.getImagePath();
        if (imagePath != null) {
            stmt.bindString(7, imagePath);
        }
        stmt.bindLong(8, entity.getFaceTime());
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, RegisteredFace entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
        stmt.bindLong(2, entity.getPerson_id());
 
        String name = entity.getName();
        if (name != null) {
            stmt.bindString(3, name);
        }
        stmt.bindLong(4, entity.getAge());
 
        String gender = entity.getGender();
        if (gender != null) {
            stmt.bindString(5, gender);
        }
 
        byte[] featureData = entity.getFeatureData();
        if (featureData != null) {
            stmt.bindBlob(6, featureData);
        }
 
        String imagePath = entity.getImagePath();
        if (imagePath != null) {
            stmt.bindString(7, imagePath);
        }
        stmt.bindLong(8, entity.getFaceTime());
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    @Override
    public RegisteredFace readEntity(Cursor cursor, int offset) {
        RegisteredFace entity = new RegisteredFace( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.getLong(offset + 1), // person_id
            cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2), // name
            cursor.getInt(offset + 3), // age
            cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4), // gender
            cursor.isNull(offset + 5) ? null : cursor.getBlob(offset + 5), // featureData
            cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6), // imagePath
            cursor.getLong(offset + 7) // faceTime
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, RegisteredFace entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setPerson_id(cursor.getLong(offset + 1));
        entity.setName(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
        entity.setAge(cursor.getInt(offset + 3));
        entity.setGender(cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4));
        entity.setFeatureData(cursor.isNull(offset + 5) ? null : cursor.getBlob(offset + 5));
        entity.setImagePath(cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6));
        entity.setFaceTime(cursor.getLong(offset + 7));
     }
    
    @Override
    protected final Long updateKeyAfterInsert(RegisteredFace entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    @Override
    public Long getKey(RegisteredFace entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(RegisteredFace entity) {
        return entity.getId() != null;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}
