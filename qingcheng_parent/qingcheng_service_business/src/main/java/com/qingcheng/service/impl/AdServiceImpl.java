package com.qingcheng.service.impl;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.qingcheng.dao.AdMapper;
import com.qingcheng.entity.PageResult;
import com.qingcheng.pojo.business.Ad;
import com.qingcheng.pojo.goods.Para;
import com.qingcheng.service.business.AdService;
import com.qingcheng.util.CacheKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class AdServiceImpl implements AdService {

    @Autowired
    private AdMapper adMapper;

    /**
     * 返回全部记录
     * @return
     */
    public List<Ad> findAll() {
        return adMapper.selectAll();
    }

    /**
     * 分页查询
     * @param page 页码
     * @param size 每页记录数
     * @return 分页结果
     */
    public PageResult<Ad> findPage(int page, int size) {
        PageHelper.startPage(page,size);
        Page<Ad> ads = (Page<Ad>) adMapper.selectAll();
        return new PageResult<Ad>(ads.getTotal(),ads.getResult());
    }

    /**
     * 条件查询
     * @param searchMap 查询条件
     * @return
     */
    public List<Ad> findList(Map<String, Object> searchMap) {
        Example example = createExample(searchMap);
        return adMapper.selectByExample(example);
    }

    /**
     * 分页+条件查询
     * @param searchMap
     * @param page
     * @param size
     * @return
     */
    public PageResult<Ad> findPage(Map<String, Object> searchMap, int page, int size) {
        PageHelper.startPage(page,size);
        Example example = createExample(searchMap);
        Page<Ad> ads = (Page<Ad>) adMapper.selectByExample(example);
        return new PageResult<Ad>(ads.getTotal(),ads.getResult());
    }

    /**
     * 根据Id查询
     * @param id
     * @return
     */
    public Ad findById(Integer id) {
        return adMapper.selectByPrimaryKey(id);
    }

    /**
     * 新增
     * @param ad
     */
    public void add(Ad ad) {
        adMapper.insert(ad);
        sendAdToRedisByPosition(ad.getPosition());//从新加载缓存
    }

    /**
     * 修改
     * @param ad
     */
    public void update(Ad ad) {
        //获取当前的位置position
        String position = ad.getPosition();
        adMapper.updateByPrimaryKeySelective(ad);
        sendAdToRedisByPosition(position);//加入缓存
        //判断更改后的位置position与修改前的位置是否相同 如果不同执行下面操作
        if (!position.equals(ad.getPosition())) {
            sendAdToRedisByPosition(ad.getPosition());
        }
    }

    /**
     *  删除
     * @param id
     */
    public void delete(Integer id) {
        //根据id查询当前的ad
        Ad ad = adMapper.selectByPrimaryKey(id);
        //获取当前ad的position 位置信息
        String position = ad.getPosition();
        //删除当前ad
        adMapper.deleteByPrimaryKey(id);
        //重新加载缓存
        sendAdToRedisByPosition(position);

    }

    /**
     * 根据position查询轮播图
     * @param position
     * @return
     */
    public List<Ad> findByPosition(String position) {
        //从缓存中取出
        System.out.println("从缓存中取出");
        return (List<Ad>) redisTemplate.boundHashOps(CacheKey.AD).get(position);
    }

    @Autowired
    private RedisTemplate redisTemplate;
    /**
     *根据position查询ad信息加入缓存
     * @param position
     */
    public void sendAdToRedisByPosition(String position) {
        //从数据库查询商品分类详情
        Example example = new Example(Ad.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("position", position);
        criteria.andLessThanOrEqualTo("startTime", new Date());
        criteria.andGreaterThanOrEqualTo("endTime", new Date());
        criteria.andEqualTo("status", "1");
        List<Ad> adList = adMapper.selectByExample(example);
        //添加到缓存
        redisTemplate.boundHashOps(CacheKey.AD).put(position, adList);
    }

    /**
     * 查询所有广告信息加入缓存redis
     */
    public void sendAllAdToRedis() {
        List<String> positionList = getPositionList();
        for (String position : positionList) {
            //根据position查询ad信息加入缓存
            sendAdToRedisByPosition(position);
        }
    }

    /**
     * 返回所有的广告位置信息
     * @return
     */
    private List<String> getPositionList() {
        List<String> positionList = new ArrayList<String>();
        positionList.add("index_lb");//首页广告轮播图
        return positionList;
    }

    /**
     * 构建查询条件
     * @param searchMap
     * @return
     */
    private Example createExample(Map<String, Object> searchMap){
        Example example=new Example(Ad.class);
        Example.Criteria criteria = example.createCriteria();
        if(searchMap!=null){
            // 广告名称
            if(searchMap.get("name")!=null && !"".equals(searchMap.get("name"))){
                criteria.andLike("name","%"+searchMap.get("name")+"%");
            }
            // 广告位置
            if(searchMap.get("position")!=null && !"".equals(searchMap.get("position"))){
                criteria.andLike("position","%"+searchMap.get("position")+"%");
            }
            // 状态
            if(searchMap.get("status")!=null && !"".equals(searchMap.get("status"))){
                criteria.andLike("status","%"+searchMap.get("status")+"%");
            }
            // 图片地址
            if(searchMap.get("image")!=null && !"".equals(searchMap.get("image"))){
                criteria.andLike("image","%"+searchMap.get("image")+"%");
            }
            // URL
            if(searchMap.get("url")!=null && !"".equals(searchMap.get("url"))){
                criteria.andLike("url","%"+searchMap.get("url")+"%");
            }
            // 备注
            if(searchMap.get("remarks")!=null && !"".equals(searchMap.get("remarks"))){
                criteria.andLike("remarks","%"+searchMap.get("remarks")+"%");
            }

            // ID
            if(searchMap.get("id")!=null ){
                criteria.andEqualTo("id",searchMap.get("id"));
            }

        }
        return example;
    }

}
