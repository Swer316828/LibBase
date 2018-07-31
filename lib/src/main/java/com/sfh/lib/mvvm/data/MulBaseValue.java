package com.sfh.lib.mvvm.data;

import com.sfh.lib.mvvm.service.BaseLiveData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 功能描述: 数据拥有同时通知多个相同数据类型功能
 *
 * @author Administrator
 * @company 中储南京智慧物流科技有限公司
 * @copyright （版权）中储南京智慧物流科技有限公司所有
 * @date 2018/7/18
 */
public class MulBaseValue implements IValue {
    private List<BaseLiveData<MulBaseValue>> mLiveDatas;

    @Override
    public void setLiveData(BaseLiveData baseLiveData) {
        if (baseLiveData != null) {
            checkDatas();
            if (!this.mLiveDatas.contains(baseLiveData)) {
                this.mLiveDatas.add(baseLiveData);
            }
        }
    }

    private synchronized void checkDatas() {
        if (this.mLiveDatas == null) {
            this.mLiveDatas = Collections.synchronizedList(new ArrayList(2));
        }
    }

    @Override
    public void removeLiveData() {
        if (this.mLiveDatas != null) {
            this.mLiveDatas.clear();
            this.mLiveDatas = null;
        }
    }

    @Override
    public void onDestroy() {
        removeLiveData();
    }

    @Override
    public void notifyChange() {
        if (this.mLiveDatas != null && !this.mLiveDatas.isEmpty()) {
            for (BaseLiveData baseLiveData : this.mLiveDatas) {
                if (baseLiveData != null) {
                    baseLiveData.postValue(this);
                }
            }
        }
    }
}
