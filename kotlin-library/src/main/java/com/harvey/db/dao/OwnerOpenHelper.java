package com.harvey.db.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;


import org.greenrobot.greendao.database.Database;

/**
 * Created by harvey on 2017/10/11 0011 14:36
 */

public class OwnerOpenHelper extends DaoMaster.OpenHelper {

    public OwnerOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory) {
        super(context, name, factory);
    }

    /**
     * 数据库升级
     * <p>
     * 1-->AnyChatRecordVideo，AnyChatSnapShot，AnyChatTransFile Syllabus表
     * 2-->Syllabus改字段 3-->Syllabus改字段类型，增加BaseSyllabus表 4-->增加RegisteredFace表
     * <p>
     * </p>
     *
     * @param db
     * @param oldVersion
     * @param newVersion
     */
    @Override
    public void onUpgrade(Database db, int oldVersion, int newVersion) {
        Log.i("greenDAO", "Upgrading schema from version " + oldVersion + " to " + newVersion);
        // 操作数据库的更新 有几个表升级都可以传入到下面
        MigrationHelper.migrate(db, RegisteredFaceDao.class);
    }
}
