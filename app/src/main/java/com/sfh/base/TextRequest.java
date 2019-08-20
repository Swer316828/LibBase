package com.sfh.base;

import com.sfh.lib.http.IRxHttpClient;
import com.sfh.lib.http.service.CommHttpClientService;
import com.sfh.lib.http.transaction.OutreachRequest;

/**
 * 功能描述:
 *
 * @author SunFeihu 孙飞虎
 * @date 2019/8/9
 */
public class TextRequest extends OutreachRequest<String> {
    public TextRequest() {
        super("/oms-apps/order/source");
    }

    @Override
    public IRxHttpClient getHttpService() {
        return CommHttpClientService.newInstance();
    }

    @Override
    public String getUrl(String code) {
        return "http://sit.apigateway-core.zczy56.com:3655";
    }
}
