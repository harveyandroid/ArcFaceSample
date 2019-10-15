package com.harvey.arcface;

import android.util.Log;

import com.harvey.arcface.template.ILogger;


/**
 * Created by hanhui on 2019/7/26 0026 16:55
 */
public class DefaultLogger implements ILogger {

    private static boolean isShowLog = false;
    private static boolean isShowStackTrace = false;
    private static boolean isMonitorMode = false;

    private String defaultTag = "AIFace";

    public DefaultLogger() {
    }

    public DefaultLogger(String defaultTag) {
        this.defaultTag = defaultTag;
    }

    public static String getExtInfo(StackTraceElement stackTraceElement) {

        String separator = " & ";
        StringBuilder sb = new StringBuilder("[");

        if (isShowStackTrace) {
            String threadName = Thread.currentThread().getName();
            String fileName = stackTraceElement.getFileName();
            String className = stackTraceElement.getClassName();
            String methodName = stackTraceElement.getMethodName();
            long threadID = Thread.currentThread().getId();
            int lineNumber = stackTraceElement.getLineNumber();

            sb.append("ThreadId=").append(threadID).append(separator);
            sb.append("ThreadName=").append(threadName).append(separator);
            sb.append("FileName=").append(fileName).append(separator);
            sb.append("ClassName=").append(className).append(separator);
            sb.append("MethodName=").append(methodName).append(separator);
            sb.append("LineNumber=").append(lineNumber);
        }

        sb.append(" ] ");
        return sb.toString();
    }

    @Override
    public void showLog(boolean showLog) {
        isShowLog = showLog;
    }

    @Override
    public void showStackTrace(boolean showStackTrace) {
        isShowStackTrace = showStackTrace;
    }

    public void showMonitor(boolean showMonitor) {
        isMonitorMode = showMonitor;
    }

    @Override
    public void d(String message) {
        if (isShowLog) {
            StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[3];
            Log.d(getDefaultTag(), message + getExtInfo(stackTraceElement));
        }
    }

    @Override
    public void i(String message) {
        if (isShowLog) {
            StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[3];
            Log.i(getDefaultTag(), message + getExtInfo(stackTraceElement));
        }
    }

    @Override
    public void w(String message) {
        if (isShowLog) {
            StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[3];
            Log.w(getDefaultTag(), message + getExtInfo(stackTraceElement));
        }
    }

    @Override
    public void e(String message) {
        if (isShowLog) {
            StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[3];
            Log.e(getDefaultTag(), message + getExtInfo(stackTraceElement));
        }
    }

    @Override
    public void monitor(String message) {
        if (isShowLog && isMonitorMode()) {
            StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[3];
            Log.d(defaultTag + "::monitor", message + getExtInfo(stackTraceElement));
        }
    }

    @Override
    public boolean isMonitorMode() {
        return isMonitorMode;
    }

    @Override
    public String getDefaultTag() {
        return defaultTag;
    }
}