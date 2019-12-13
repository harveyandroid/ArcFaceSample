package com.harvey.arcfacedamo.db.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.harvey.arcfacedamo.db.MigrationHelper;

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
     *
     * @param db
     * @param oldVersion
     * @param newVersion
     */
    @Override
    public void onUpgrade(Database db, int oldVersion, int newVersion) {
        Log.i("greenDAO", "Upgrading schema from version " + oldVersion + " to " + newVersion);
        // 操作数据库的更新 有几个表升级都可以传入到下面
        MigrationHelper.migrate(db, FaceRegisterDao.class);
    }
}
