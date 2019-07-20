package com.qingcheng.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.qingcheng.dao.BrandMapper;
import com.qingcheng.dao.SpecMapper;
import com.qingcheng.pojo.goods.Sku;
import com.qingcheng.pojo.goods.Template;
import com.qingcheng.service.goods.SkuSearchService;
import com.qingcheng.service.goods.SpecService;
import com.qingcheng.util.CacheKey;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import javax.swing.text.Highlighter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: cooper
 * @Description:
 * @Date: Create in 18:57 2019/7/16
 */
@Service
public class SkuSearchServiceImpl implements SkuSearchService {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Autowired
    private BrandMapper brandMapper;

    @Autowired
    private SpecMapper specMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 查询
     * @param searchMap
     * @return
     */
    public Map serch(Map<String, String> searchMap) {

        //1 封装查询数据
        SearchRequest searchRequest = new SearchRequest("sku");//相当于从哪个索引库查询
        searchRequest.types("doc");//查询的类型 (相当于数据库中表)
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();//相当于得一个{}

        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();//布尔查询 构造器

        //1.1关键字搜索

        MatchQueryBuilder matchQueryBuilder = QueryBuilders.matchQuery("name", searchMap.get("keywords"));//match条件
        queryBuilder.must(matchQueryBuilder);//将match查询添加到bool查询


        //商品分类(聚合查询)
        TermsAggregationBuilder termsAggregationBuilder = AggregationBuilders.terms("sku_category").field("categoryName");

        //1.2商品分类过滤
        if (searchMap.get("category") != null) {//判断map中的category是否有值 没有值执行以下操作
            TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("categoryName", searchMap.get("category"));
            queryBuilder.filter(termQueryBuilder);
        }

        //1.3品牌过滤
        if (searchMap.get("brand") != null) {//判断查询条件中 是否有品牌 如果有 就进行查询
            TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("brandName", searchMap.get("brand"));
            queryBuilder.filter(termQueryBuilder);
        }
        //1.4规格过滤
        for (String key : searchMap.keySet()) {
            if (key.startsWith("spec.")) {//如果是规格参数
                TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery(key + ".keyword", searchMap.get(key));
                queryBuilder.filter(termQueryBuilder);
            }
        }

        //1.5价格过滤
        if (searchMap.get("price") != null) {
            String[] prices = searchMap.get("price").split("-");
            if (!prices[0].equals(0)) {//如果第一个价格不等于0 说明有最小值 gte方法代表大于等于
                RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("price").gte(prices[0] + "00");
                queryBuilder.filter(rangeQueryBuilder);
            }
            if (!prices[1].equals("*")) {
                RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("price").lte(prices[1] + "00");
                queryBuilder.filter(rangeQueryBuilder);
            }
        }
        searchSourceBuilder.query(queryBuilder);//将bool查询添加到大查询中 即第一个{}
        searchSourceBuilder.aggregation(termsAggregationBuilder);//将商品分类过滤这个查询添加到查询源中

        //1.6分页

        Integer pageNo =Integer.parseInt(searchMap.get("pageNo")) ;//获取当前页
        Integer pageSize = 30;//每页记录数
        Integer fromIndex = (pageNo - 1) * pageSize;//计算开始开始索引
        searchSourceBuilder.from(fromIndex);//设置开始索引
        searchSourceBuilder.size(pageSize);//设置每页记录数
        //1.7搜索排序
        String sort = searchMap.get("sort");//获得排序字段
        String sortOrder = searchMap.get("sortOrder"); //获得排序规则 降序DESC 升序 ASC
        if (!"".equals(sort)) {//如果sort等于空字符串 就不用查询 提高性能 默认是前台的综合
            searchSourceBuilder.sort(sort, SortOrder.valueOf(sortOrder));
        }

        //1.8高亮显示
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("name").preTags("<font style = 'color:red'>").postTags("</font>");
        //添加到searchSourceBuilder中
        searchSourceBuilder.highlighter(highlightBuilder);
        searchRequest.source(searchSourceBuilder);



        //2 封装查询结果
        Map resultMap = new HashMap();//新建一个map作为返回结果

        SearchResponse searchResponse = null;
        try {
            searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            SearchHits searchHits = searchResponse.getHits();
            long totalHits = searchHits.getTotalHits();
            System.out.println("总记录数:"+totalHits);
            SearchHit[] hits = searchHits.getHits();

            //2.1商品列表
            List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
            for (SearchHit hit : hits) {
                Map<String, Object> skuMap = hit.getSourceAsMap();//将每一个对象封装为一个map 原本装有name的属性 被下面高亮部分覆盖了
                Map<String, HighlightField> highlightFields = hit.getHighlightFields();
                Text[] names = highlightFields.get("name").fragments();
                String name = names[0].toString();
                skuMap.put("name", name);//将新的高亮的替换原来的name
                resultList.add(skuMap);//添加到集合中
            }

            resultMap.put("rows", resultList);

            //2.2商品分类列表
            Aggregations aggregations = searchResponse.getAggregations();
            Map<String, Aggregation> aggregationMapMap = aggregations.getAsMap();//转化为一个map
            Terms terms = (Terms) aggregationMapMap.get("sku_category");//获取sku_category的value值
            List<? extends Terms.Bucket> buckets = terms.getBuckets();//获取一个集合对象
            List<String> categoryList = new ArrayList<String>();//创建一个集合来接收中的value 因为只需要休闲鞋  "key" : "休闲鞋",
            for (Terms.Bucket bucket : buckets) {
                categoryList.add(bucket.getKeyAsString());
            }
            resultMap.put("categoryList", categoryList);//添加到返回的map中

            String categoryName ="";//商品分类名称
            if (searchMap.get("category") == null) {//判断查询条件里是否有分类名称 如果没有代表没有查询 品牌列表显示为第一个 如果没有分类条件
                if (categoryList.size() > 0) {
                    categoryName = categoryList.get(0);//获取第一个分类名称
                }
            }else {
                categoryName = searchMap.get("category");//选取了 就选取当前分类名称
            }
            //2.3品牌列表
            if (searchMap.get("brand") == null) {//如果没有brand的值
                //List<Map> brandList = brandMapper.findListByCategoryName(categoryName);//返回按照分类名称查询出来的品牌列表
                List<Map> brandList = (List<Map>)redisTemplate.boundHashOps(CacheKey.BRAND_LIST).get(categoryName);
                resultMap.put("brandList", brandList);
            }
            //2.4规格列表
            //List<Map> specList = specMapper.findListByCategoryName(categoryName);//获得规格参数列表
            List<Map> specList = (List<Map>)redisTemplate.boundHashOps(CacheKey.SPEC_LIST).get(categoryName);
            if (specList.size() > 0) {
                for (Map spec : specList) {
                    String[] options = ((String) spec.get("options")).split(",");//将字符串转换为数组
                    spec.put("options", options);//重新添加到map中
                }
                resultMap.put("specList", specList);
            }

            //2.5 页码
            long totalCount = searchHits.getTotalHits();//总记录数
            long pageCount = (totalCount % pageSize == 0) ? totalCount / pageSize : (totalCount / pageSize + 1);//总页数
            resultMap.put("totalPages", pageCount);//将总页数添加到返回中


        } catch (IOException e) {
            e.printStackTrace();
        }
        return resultMap;
    }
}
