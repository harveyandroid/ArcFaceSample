package com.harvey.arcface.model;

/**
 * Created by hanhui on 2020/12/4 17:39
 */
public class ProcessState {
    /**
     * 无状态
     */
    public static final int NONE = 0;
    /**
     * 人脸检测
     */
    public static final int FD = 1;
    /**
     * 人脸特征值提取加入队列等待中
     */
    public static final int FR_WAITING = 2;
    /**
     * 人脸特征值提前中
     */
    public static final int FR_PROCESSING = 3;
    /**
     * 特征值提取成功
     */
    public static final int FR_SUCCESS = 4;
    /**
     * 特征值提取失败
     */
    public static final int FR_FAILED = 5;

}
