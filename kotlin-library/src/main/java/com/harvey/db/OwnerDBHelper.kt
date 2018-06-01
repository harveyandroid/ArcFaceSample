package com.harvey.db

import android.content.Context
import com.harvey.db.bean.RegisteredFace
import com.harvey.db.dao.DaoMaster
import com.harvey.db.dao.DaoSession
import com.harvey.db.dao.OwnerOpenHelper
import com.harvey.db.dao.RegisteredFaceDao
import org.greenrobot.greendao.query.QueryBuilder
import java.util.*

/**
 * Created by hanhui on 2018/5/31 0031 17:41
 */
object OwnerDBHelper {
    private const val TAG = "RobotDBHelper"
    private const val DATABASE_NAME = "arc_face.db"
    const val UPDATE_FACE_TAG = 1
    private lateinit var daoMaster: DaoMaster
    private lateinit var daoSession: DaoSession
    private lateinit var registeredFaceDao: RegisteredFaceDao
    private val observable = Observable()
    /**
     * 需要全局初始化
     *
     * @param context
     * @return
     */
    fun init(context: Context) {
        registeredFaceDao = getDaoSession(context).registeredFaceDao
    }

    /**
     * 添加观察者
     */
    fun addObserver(o: Observer) {
        observable.addObserver(o)
    }

    /**
     * 删除观察者
     */
    fun deleteObserver(o: Observer) {
        observable.deleteObserver(o)
    }


    /**
     * 取得DaoMaster db
     *
     * @param context
     * @return
     */
    private fun getDaoMaster(context: Context): DaoMaster {
        val helper = OwnerOpenHelper(context.applicationContext, DATABASE_NAME, null)
        daoMaster = DaoMaster(helper.writableDatabase)
        return daoMaster
    }

    /**
     * 取得DaoSession
     *
     * @param context
     * @return
     */
    private fun getDaoSession(context: Context): DaoSession {
        daoMaster = getDaoMaster(context)
        daoSession = daoMaster.newSession()
        return daoSession
    }


    /**
     * 初始化DB log
     *
     * @param isDebug
     */
    fun initLog(isDebug: Boolean) {
        QueryBuilder.LOG_VALUES = isDebug
        QueryBuilder.LOG_SQL = isDebug
    }

    /**
     * 获取所有的注册人脸
     *
     * @return
     */
    val registeredFaces: List<RegisteredFace>
        get() {
            val qb = registeredFaceDao.queryBuilder()
            return qb.list()
        }


    /**
     * 保存注册人脸
     *
     * @param face
     */
    fun saveRegisteredFace(face: RegisteredFace) {
        val qb = registeredFaceDao.queryBuilder()
        qb.where(RegisteredFaceDao.Properties.Name.eq(face.name))
        if (qb.list().size > 0) {
            val oldFace = qb.list()[0]
            face.id = oldFace.id
            registeredFaceDao.save(face)
        } else {
            registeredFaceDao.insert(face)
        }
        observable.notifyObservers(UPDATE_FACE_TAG)
    }
}
