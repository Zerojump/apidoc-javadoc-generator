package com.cmy.apidoc.generator.material.vo;

import com.cmy.apidoc.generator.annotations.ApiParam;

import java.util.Date;

/**
 * <p>@author chenmingyi
 * <p>@version 1.0
 * <p>Date: 2017/7/3
 * <p>
 * To change this template use File | Settings | File and Code Templates | Includes .
 */
public class PermissionVo {

    private Integer id;

    @ApiParam(size = "..5", required = false)
    private String code;

    private Date createTime;

    private String name;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PermissionVo that = (PermissionVo) o;

        if (!id.equals(that.id)) return false;
        if (code != null ? !code.equals(that.code) : that.code != null) return false;
        return name != null ? name.equals(that.name) : that.name == null;

    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + (code != null ? code.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "PermissionVo{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
