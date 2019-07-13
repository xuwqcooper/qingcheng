package com.qingcheng.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.qingcheng.pojo.goods.Goods;
import com.qingcheng.pojo.goods.Sku;
import com.qingcheng.pojo.goods.Spu;
import com.qingcheng.service.goods.CategoryService;
import com.qingcheng.service.goods.SpuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: cooper
 * @Description:
 * @Date: Create in 23:40 2019/7/12
 */
@RestController
@RequestMapping("/item")
public class ItemController {

    @Reference
    private SpuService spuService;

    @Value("${pagePath}")
    private String pagePath;

    @Autowired
    private TemplateEngine templateEngine;

    @Reference
    private CategoryService categoryService;

    /**
     * 生成商品详情页
     * @param spuId
     */
    @RequestMapping("/createPage.do")
    public void createPage(String spuId) {
        //根据spuId查询组合实体类
        Goods goods = spuService.findGoodsById(spuId);
        //获取spu
        Spu spu = goods.getSpu();
        //获取各级分类名称
        List<String> categoryList = new ArrayList<>();
        categoryList.add(categoryService.findById(spu.getCategory1Id()).getName());//一级分类名称
        categoryList.add(categoryService.findById(spu.getCategory2Id()).getName());//二级分类名称
        categoryList.add(categoryService.findById(spu.getCategory3Id()).getName());//三级分类名称

        //获取skuList
        List<Sku> skuList = goods.getSkuList();
        for (Sku sku : skuList) {
            //创建上下文
            Context context = new Context();
            //创建数据模型
            Map<String, Object> dataModel = new HashMap<>();
            dataModel.put("spu", spu);
            dataModel.put("sku", sku);
            dataModel.put("categoryList", categoryList);
            dataModel.put("skuImages",sku.getImages().split(","));//sku图片列表
            dataModel.put("spuImages",sku.getImages().split(","));//spu图片列表
            Map  paraItems = JSON.parseObject(spu.getParaItems());
            dataModel.put("paraItems", paraItems);//参数列表
            Map<String,String> specItems = (Map) JSON.parseObject(sku.getSpec());//规格列表
            dataModel.put("specItems", specItems);
            //规格选择面板
            // {"颜色":["白色","红色","黑色","蓝色"],"选择套装":["官方标配","碎屏无忧套装","原装壳套装"],"版本":["6GB+128GB","4GB+64GB","6GB+64GB"]}
            Map<String,List> specMap = (Map) JSON.parseObject(spu.getSpecItems());//规格和规格选项
            for (String key : specMap.keySet()) {
            //{"颜色":[{"'option','白色';'checked','true'"},"红色","黑色","蓝色"]
                List<String> list = specMap.get(key);
                List<Map> mapList = new ArrayList<>();//创建新的集合
                //循环规格选项
                for (String value : list) {
                    Map map = new HashMap();
                    map.put("option", value);//规格选项
                    if (value.equals(specItems.get(key))) {//如果和当前的sku的规格相同就选中
                        map.put("checked", true);//是否选中
                    } else {
                        map.put("checked", false);//是否选中
                    }
                    mapList.add(map);
                }
                specMap.put(key, mapList);//用新的集合替换原来的集合
            }
            dataModel.put("specMap", specMap);
            context.setVariables(dataModel);
            //创建文件
            File dir = new File(pagePath);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File dest = new File(dir,sku.getId()+".html");
            //生成页面
            try {
                PrintWriter printWriter = new PrintWriter(dest,"UTF-8");
                templateEngine.process("item", context, printWriter);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}
