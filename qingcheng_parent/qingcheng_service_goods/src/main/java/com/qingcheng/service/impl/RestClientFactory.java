package com.qingcheng.service.impl;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;

/**
 * @Author: cooper
 * @Description:
 * @Date: Create in 18:31 2019/7/16
 */
public class RestClientFactory {

    public static RestHighLevelClient getRestHighLevelClient(String hostname,int port) {
        HttpHost host = new HttpHost(hostname, port, "http");
        RestClientBuilder restClientBuilder = RestClient.builder(host);//rest构建器
        return new RestHighLevelClient(restClientBuilder);//高级客户端对象(连接)
    }
}
