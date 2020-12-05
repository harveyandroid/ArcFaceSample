package com.harvey.arcfacedamo.db;

import android.content.Context;

import com.harvey.arcfacedamo.db.bean.FaceRegister;
import com.harvey.arcfacedamo.db.dao.DaoMaster;
import com.harvey.arcfacedamo.db.dao.DaoSession;
import com.harvey.arcfacedamo.db.dao.FaceRegisterDao;
import com.harvey.arcfacedamo.db.dao.OwnerOpenHelper;

import org.greenrobot.greendao.query.QueryBuilder;

import java.util.List;

/**
 * Created by harvey on 2017/1/22 0022 16:30
 */
public class DBHelper {
    private final static String TAG = "RobotDBHelper";
    public static String DATABASE_NAME = "arc_face.db";
    private DaoMaster daoMaster;
    private DaoSession daoSession;
    private FaceRegisterDao registeredFaceDao;


    public DBHelper(Context context) {
        OwnerOpenHelper helper = new OwnerOpenHelper(context.getApplicationContext(), DATABASE_NAME, null);
        daoMaster = new DaoMaster(helper.getWritableDatabase());
        daoSession = daoMaster.newSession();
        registeredFaceDao = daoSession.getFaceRegisterDao();
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
    public void save(FaceRegister face) {
        QueryBuilder<FaceRegister> qb = registeredFaceDao.queryBuilder();
        qb.where(FaceRegisterDao.Properties.Name.eq(face.getName())).limit(1);
        if (qb.count() > 0) {
            FaceRegister oldFace = qb.list().get(0);
            face.setId(oldFace.getId());
            registeredFaceDao.update(face);
        } else {
            registeredFaceDao.insert(face);
        }
    }

    /**
     * 获取所有的注册人脸
     *
     * @return
     */
    public List<FaceRegister> loadAll() {
        return registeredFaceDao.loadAll();
    }
}
