package com.sfh.lib.event;

import io.reactivex.functions.Consumer;

/**
 * 功能描述: 消息监听接口
 *
 * @author SunFeihu 孙飞虎
 * @date 2018/8/8
 */
public interface IEventResult<T extends EventData> extends Consumer<T> {

}
