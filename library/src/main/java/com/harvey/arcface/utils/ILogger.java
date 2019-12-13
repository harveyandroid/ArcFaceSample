package com.harvey.arcface.utils;

public interface ILogger {

    void showLog(boolean isShowLog);

    void showStackTrace(boolean isShowStackTrace);

    void d(String message);

    void i(String message);

    void w(String message);

    void e(String message);

    void monitor(String message);

    boolean isMonitorMode();

    String getDefaultTag();
}
