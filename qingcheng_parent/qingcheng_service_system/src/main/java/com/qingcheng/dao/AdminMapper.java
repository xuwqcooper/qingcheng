package com.qingcheng.dao;

import com.alibaba.dubbo.config.annotation.Service;
import com.qingcheng.pojo.system.Admin;
import com.qingcheng.pojo.system.Menu;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface AdminMapper extends Mapper<Admin> {



}
