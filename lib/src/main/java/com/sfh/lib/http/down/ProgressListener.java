package com.sfh.lib.http.down;

/**
 * 功能描述:下载进度回调
 *
 * @author SunFeihu 孙飞虎
 * @date 2017/6/23
 */
public interface ProgressListener {

    /***
     * @param total     总大小
     * @param percent 下载百分比
     * @param progress 下载值
     *
     */
    void onProgress(long total, long percent, long progress);
}
