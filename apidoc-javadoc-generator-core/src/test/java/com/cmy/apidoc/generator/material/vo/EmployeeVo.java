package com.cmy.apidoc.generator.material.vo;

import java.util.Set;

/**
 * <p>@author chenmingyi
 * <p>@version 1.0
 * <p>Date: 2017/7/3
 * <p>
 * To change this template use File | Settings | File and Code Templates | Includes .
 */
public class EmployeeVo {

    private Integer id;
    //private Date birthday;
    private String name;
    private Set<PermissionVo> permissionVoSet;

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

    public Set<PermissionVo> getPermissionVoSet() {
        return permissionVoSet;
    }

    public void setPermissionVoSet(Set<PermissionVo> permissionVoSet) {
        this.permissionVoSet = permissionVoSet;
    }

    @Override
    public String toString() {
        return "EmployeeVo{" +
                "id=" + id +
                //", birthday=" + birthday +
                ", name='" + name + '\'' +
                ", permissionVoSet=" + permissionVoSet +
                '}';
    }
}
