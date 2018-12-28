package com.sfh.lib.http.transaction;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sfh.lib.http.service.gson.NullStringToEmptyAdapterFactory;

import java.io.Reader;
import java.lang.reflect.Type;

/**
 * 功能描述:数据解析
 *
 * @author SunFeihu 孙飞虎
 * @date 2018/12/28
 */
public abstract class ParseResult {


    public <T> T parseResult(Reader reader, Type cls) {

        Gson gson = this.getGson ();
        return gson.fromJson (reader, cls);

    }

    public String toJson(Object object) {

        Gson gson = this.getGson ();
        return gson.toJson (object);
    }

    private Gson getGson() {

        return new GsonBuilder ()
                .setLenient ()// json宽松
//                .enableComplexMapKeySerialization()//支持Map的key为复杂对象的形式
//                .serializeNulls() //智能null
//                .setPrettyPrinting()// 调整格式 ，使对齐
                .registerTypeAdapterFactory (new NullStringToEmptyAdapterFactory ()).create ();
    }
}
