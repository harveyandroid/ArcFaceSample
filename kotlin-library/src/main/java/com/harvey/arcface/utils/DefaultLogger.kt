package com.harvey.arcface.utils

import android.util.Log


/**
 * Created by hanhui on 2019/7/26 0026 16:55
 */
class DefaultLogger(override val isMonitorMode: Boolean = false, override val defaultTag: String = "AIFace") : ILogger {

    override fun showLog(showLog: Boolean) {
        isShowLog = showLog
    }

    override fun showStackTrace(showStackTrace: Boolean) {
        isShowStackTrace = showStackTrace
    }

    override fun d(message: String) {
        if (isShowLog) {
            val stackTraceElement = Thread.currentThread().getStackTrace()[3]
            Log.d(defaultTag, message + getExtInfo(stackTraceElement))
        }
    }

    override fun i(message: String) {
        if (isShowLog) {
            val stackTraceElement = Thread.currentThread().getStackTrace()[3]
            Log.i(defaultTag, message + getExtInfo(stackTraceElement))
        }
    }

    override fun w(message: String) {
        if (isShowLog) {
            val stackTraceElement = Thread.currentThread().getStackTrace()[3]
            Log.w(defaultTag, message + getExtInfo(stackTraceElement))
        }
    }

    override fun e(message: String) {
        if (isShowLog) {
            val stackTraceElement = Thread.currentThread().getStackTrace()[3]
            Log.e(defaultTag, message + getExtInfo(stackTraceElement))
        }
    }

    override fun monitor(message: String) {
        if (isShowLog && isMonitorMode) {
            val stackTraceElement = Thread.currentThread().getStackTrace()[3]
            Log.d("$defaultTag::monitor", message + getExtInfo(stackTraceElement))
        }
    }

    companion object {

        private var isShowLog = false
        private var isShowStackTrace = false

        fun getExtInfo(stackTraceElement: StackTraceElement): String {
            val separator = " & "
            val sb = StringBuilder("[")
            if (isShowStackTrace) {
                val threadName = Thread.currentThread().getName()
                val fileName = stackTraceElement.getFileName()
                val className = stackTraceElement.getClassName()
                val methodName = stackTraceElement.getMethodName()
                val threadID = Thread.currentThread().getId()
                val lineNumber = stackTraceElement.getLineNumber()

                sb.append("ThreadId=").append(threadID).append(separator)
                sb.append("ThreadName=").append(threadName).append(separator)
                sb.append("FileName=").append(fileName).append(separator)
                sb.append("ClassName=").append(className).append(separator)
                sb.append("MethodName=").append(methodName).append(separator)
                sb.append("LineNumber=").append(lineNumber)
            }
            sb.append(" ] ")
            return sb.toString()
        }
    }
}