package com.sfh.lib.mvvm.data;


import com.sfh.lib.mvvm.service.BaseLiveData;

/**
 * 功能描述: 数据拥有刷新UI功能
 *
 * @author Administrator
 * @date 2018/7/18
 */
public interface IValue {
    void notifyChange();

    void onDestroy();

    void removeLiveData();

    void setLiveData(BaseLiveData baseLiveData);
}
