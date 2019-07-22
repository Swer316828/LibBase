package com.sfh.lib.http.service;

/**
 * 功能描述:通用网络HTTP
 *
 * @author SunFeihu 孙飞虎
 * @date 2019/6/10
 */
public final class CommHttpClientService extends AbstractHttpClientService {

    public static boolean LOG = true;

    private static class Builder {

        private static final CommHttpClientService CLIENT_SERVICE = new CommHttpClientService();
    }

    public static CommHttpClientService newInstance() {

        return Builder.CLIENT_SERVICE;
    }

    @Override
    public boolean log() {
        return LOG;
    }


}
