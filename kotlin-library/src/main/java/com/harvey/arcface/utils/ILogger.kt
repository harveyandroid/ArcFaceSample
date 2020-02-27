package com.harvey.arcface.utils

interface ILogger {

    val isMonitorMode: Boolean

    val defaultTag: String

    fun showLog(isShowLog: Boolean)

    fun showStackTrace(isShowStackTrace: Boolean)

    fun d(message: String)

    fun i(message: String)

    fun w(message: String)

    fun e(message: String)

    fun monitor(message: String)
}
