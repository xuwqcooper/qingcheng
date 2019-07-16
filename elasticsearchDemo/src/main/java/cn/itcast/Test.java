package cn.itcast;


import org.apache.http.HttpHost;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author: cooper
 * @Description:
 * @Date: Create in 20:29 2019/7/15
 */

/**
 *  3.2.1 新增和修改数据
 插入单条数据：
 HttpHost  :  url地址封装
 RestClientBuilder： rest客户端构建器
 RestHighLevelClient： rest高级客户端
 IndexRequest： 新增或修改请求
 IndexResponse：新增或修改的响应结果
 */
public class Test {

    public static void main(String[] args) throws IOException {

        //连接rest接口
        HttpHost http = new HttpHost("127.0.0.1",9200,"http");
        RestClientBuilder restClientBuilder = RestClient.builder(http);//rest构建器
        RestHighLevelClient restHighLevelClient = new RestHighLevelClient(restClientBuilder);//高级客户端对象
        //封装请求对象
        BulkRequest bulkRequest = new BulkRequest();//批量插入数据
        //可以循环
        IndexRequest indexRequest = new IndexRequest("sku", "doc", "7");//自带id新增
        Map skuMap = new HashMap();
        skuMap.put("name","华为p30pro 新增");
        skuMap.put("brandName","华为");
        skuMap.put("categoryName","手机");
        skuMap.put("price",1010221);
        skuMap.put("createTime","2019-05-01");
        skuMap.put("saleNum",101021);
        skuMap.put("commentNum",10102321);
        Map spec=new HashMap();
        spec.put("网络制式","移动4G");
        spec.put("屏幕尺寸","5");
        skuMap.put("spec",spec);
        indexRequest.source(skuMap);

        bulkRequest.add(indexRequest);//批量添加数据
        //获取执行结果
//        IndexResponse indexResponse = restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
        BulkResponse bulk = restHighLevelClient.bulk(bulkRequest);
        int status = bulk.status().getStatus();
        System.out.println(status);
        restHighLevelClient.close();//
    }
}
