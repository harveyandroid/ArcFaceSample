package com.harvey.db;

import android.content.Context;

import com.harvey.db.bean.RegisteredFace;
import com.harvey.db.dao.DaoMaster;
import com.harvey.db.dao.DaoSession;
import com.harvey.db.dao.OwnerOpenHelper;
import com.harvey.db.dao.RegisteredFaceDao;

import org.greenrobot.greendao.query.QueryBuilder;

import java.util.List;

/**
 * Created by harvey on 2017/1/22 0022 16:30
 */
public class OwnerDBHelper {
    private final static String TAG = "RobotDBHelper";
    public static String DATABASE_NAME = "arc_face.db";
    private volatile static OwnerDBHelper instance;
    private DaoMaster daoMaster;
    private DaoSession daoSession;
    private RegisteredFaceDao registeredFaceDao;

    private OwnerDBHelper() {
    }

    public static OwnerDBHelper getInstance() {
        if (instance == null) {
            synchronized (OwnerDBHelper.class) {
                if (instance == null) {
                    instance = new OwnerDBHelper();
                }
            }
        }
        return instance;
    }

    /**
     * 取得DaoMaster db
     *
     * @param context
     * @return
     */
    public DaoMaster getDaoMaster(Context context) {
        if (daoMaster == null) {
            OwnerOpenHelper helper = new OwnerOpenHelper(context.getApplicationContext(), DATABASE_NAME, null);
            daoMaster = new DaoMaster(helper.getWritableDatabase());
        }
        return daoMaster;
    }

    /**
     * 取得DaoSession
     *
     * @param context
     * @return
     */
    public DaoSession getDaoSession(Context context) {
        if (daoSession == null) {
            if (daoMaster == null) {
                daoMaster = getDaoMaster(context);
            }
            daoSession = daoMaster.newSession();
        }
        return daoSession;
    }

    /**
     * 需要全局初始化
     *
     * @param context
     * @return
     */
    public OwnerDBHelper init(Context context) {
        if (registeredFaceDao == null)
            registeredFaceDao = getDaoSession(context).getRegisteredFaceDao();
        return this;
    }

    /**
     * 初始化DB log
     *
     * @param isDebug
     */
    public void initLog(boolean isDebug) {
        QueryBuilder.LOG_VALUES = isDebug;
        QueryBuilder.LOG_SQL = isDebug;
    }

    /**
     * 保存注册人脸
     *
     * @param face
     */
    public void saveRegisteredFace(RegisteredFace face) {
        QueryBuilder<RegisteredFace> qb = registeredFaceDao.queryBuilder();
        qb.where(RegisteredFaceDao.Properties.Name.eq(face.getName()));
        if (qb.list().size() > 0) {
            RegisteredFace oldFace = qb.list().get(0);
            face.setId(oldFace.getId());
            registeredFaceDao.save(face);
        } else {
            registeredFaceDao.insert(face);
        }
    }

    /**
     * 获取所有的注册人脸
     *
     * @return
     */
    public List<RegisteredFace> getRegisteredFaces() {
        QueryBuilder<RegisteredFace> qb = registeredFaceDao.queryBuilder();
        return qb.list();
    }
}
