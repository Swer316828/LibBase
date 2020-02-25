package com.sfh.lib.http;

/**
 * 功能描述:参数类型
 *
 * @author SunFeihu 孙飞虎
 * @date 2019/1/9
 */
public enum HttpMediaType {
    /**
     * "application/x-www-form-urlencoded"，是默认的MIME内容编码类型，一般可以用于所有的情况，但是在传输比较大的二进制或者文本数据时效率低。
     * 这时候应该使用"multipart/form-data"。如上传文件或者二进制数据和非ASCII数据。
     */
    MEDIA_TYPE_NORAML_FORM("application/x-www-form-urlencoded;charset=utf-8"),


    /**
     * 既可以提交普通键值对，也可以提交(多个)文件键值对
     */
    MEDIA_TYPE_MULTIPART_FORM("multipart/form-data;charset=utf-8"),

    /**
     * 文本类型
     */
    MEDIA_TYPE_TEXT("text/plain;charset=utf-8"),

    /**
     * Json类型
     */
    MEDIA_TYPE_JSON("application/json;charset=utf-8");

    String value;

     HttpMediaType(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
