package cn.itcast;

import org.apache.http.HttpHost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.io.IOException;

/**
 * @Author: cooper
 * @Description:
 * @Date: Create in 21:29 2019/7/15
 */

/**
 *3.2.3 布尔与词条查询
 BoolQueryBuilder：布尔查询构建器
 TermQueryBuilder：词条查询构建器
 QueryBuilders：查询构建器工厂
 示例：查询名称包含手机的，并且品牌为小米的记录
 */
public class Test3 {

    public static void main(String[] args) throws IOException {
        //连接rest接口
        HttpHost host = new HttpHost("127.0.0.1", 9200, "http");
        RestClientBuilder restClientBuilder = RestClient.builder(host);
        RestHighLevelClient restHighLevelClient = new RestHighLevelClient(restClientBuilder);//创建高级客户端对象

        //封装查询对象
        SearchRequest searchRequest = new SearchRequest("sku");
        searchRequest.types("doc");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();//布尔查询构造器
        MatchQueryBuilder matchQueryBuilder = QueryBuilders.matchQuery("name", "手机");//查询条件
        queryBuilder.must(matchQueryBuilder);
        TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("brandName", "小米");
        queryBuilder.must(termQueryBuilder);
        searchSourceBuilder.query(queryBuilder);
        searchRequest.source(searchSourceBuilder);
        //获取查询结果
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        SearchHits hits = searchResponse.getHits();
        long totalHits = hits.getTotalHits();
        System.out.println(totalHits);
        SearchHit[] searchHits = hits.getHits();
        for (SearchHit searchHit : searchHits) {
            String source = searchHit.getSourceAsString();
            System.out.println(source);
        }
    }
}
