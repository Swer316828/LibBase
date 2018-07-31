package com.sfh.lib.mvvm.data;

import com.sfh.lib.mvvm.service.BaseLiveData;

/**
 * 功能描述: 数据拥有刷新UI功能能力
 *
 * @author Administrator
 * @company 中储南京智慧物流科技有限公司
 * @copyright （版权）中储南京智慧物流科技有限公司所有
 * @date 2018/7/18
 */
public class BaseValue implements IValue {

    private BaseLiveData<BaseValue> mLiveData;

    @Override
    public synchronized void setLiveData(BaseLiveData baseLiveData) {
        if (this.mLiveData != baseLiveData) {
            this.mLiveData = baseLiveData;
        }
    }

    @Override
    public synchronized void removeLiveData() {
        this.mLiveData = null;
    }

    @Override
    public void onDestroy() {
        removeLiveData();
    }

    @Override
    public synchronized void notifyChange() {
        if (this.mLiveData != null) {
            this.mLiveData.postValue(this);
        }
    }
}
