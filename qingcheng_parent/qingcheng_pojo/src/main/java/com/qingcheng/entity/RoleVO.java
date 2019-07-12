package com.qingcheng.entity;

import com.qingcheng.pojo.system.Resource;

import javax.persistence.Table;
import java.io.Serializable;
import java.util.List;

/**
 * @Author: cooper
 * @Description:
 * @Date: Create in 16:10 2019/7/10
 */

public class RoleVO implements Serializable {
    private Integer id;
    private String name;//角色名称
    private List<Resource> resourceList;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Resource> getResourceList() {
        return resourceList;
    }

    public void setResourceList(List<Resource> resourceList) {
        this.resourceList = resourceList;
    }
}
