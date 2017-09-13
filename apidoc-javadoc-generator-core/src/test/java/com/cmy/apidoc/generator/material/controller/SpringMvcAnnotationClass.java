package com.cmy.apidoc.generator.material.controller;

import com.cmy.apidoc.generator.annotations.ApiDesc;
import com.cmy.apidoc.generator.annotations.ApiErrorDefine;
import com.cmy.apidoc.generator.annotations.ApiParam;
import com.cmy.apidoc.generator.material.commons.ResponseWrap;
import com.cmy.apidoc.generator.material.generic.A;
import com.cmy.apidoc.generator.material.generic.B;
import com.cmy.apidoc.generator.material.generic.C;
import com.cmy.apidoc.generator.material.generic.D;
import com.cmy.apidoc.generator.material.generic.E;
import com.cmy.apidoc.generator.material.generic.F;
import com.cmy.apidoc.generator.material.vo.PermissionVo;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageInfo;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>@author chenmingyi
 * <p>@version 1.0
 * <p>Date: 2017/7/3
 * <p>
 * To change this template use File | Settings | File and Code Templates | Includes .
 */
@RestController
@RequestMapping("brand")
public class SpringMvcAnnotationClass {

    @RequestMapping(name = "不知道做什么啊", value = "read/{id}", method = RequestMethod.GET)
    public List<Integer> get(@ApiParam(name = "id", group = "mine", size = "0-", allowedValues = "1,2", defaultValue = "1", desc = "just send id", required = false)
                             @PathVariable Integer id,
                             @RequestBody PermissionVo permissionVo) {
        return Arrays.asList(id);
    }

    @RequestMapping(value = "create", method = RequestMethod.POST)
    public ResponseWrap<List<PermissionVo>> post(@RequestBody @ApiParam(desc = "权限id数组") List<Integer> permissionIdList) {
        List<PermissionVo> collect = permissionIdList.stream().map(i -> {
            PermissionVo permissionVo = new PermissionVo();
            permissionVo.setId(i);
            return permissionVo;
        }).collect(Collectors.toList());

        ResponseWrap<List<PermissionVo>> responseWrap = new ResponseWrap<>();
        responseWrap.setData(collect);
        return responseWrap;
    }

    @RequestMapping(value = "", method = RequestMethod.PUT)
    public ResponseWrap<PageInfo<PermissionVo>> edit(@RequestBody @ApiParam(desc = "权限id数组") List<Integer> ids) {

        List<PermissionVo> list = new LinkedList<>();
        for (Integer id : ids) {
            PermissionVo permissionVo = new PermissionVo();
            permissionVo.setId(id);
            list.add(permissionVo);
        }
        Page<PermissionVo> permissionVoPage = new Page<>();
        permissionVoPage.addAll(list);
        PageInfo<PermissionVo> pageInfo = permissionVoPage.toPageInfo();
        ResponseWrap<PageInfo<PermissionVo>> responseWrap = new ResponseWrap<>();
        responseWrap.setData(pageInfo);
        return responseWrap;
    }

    @RequestMapping(value = "generic", method = RequestMethod.GET)
    @ApiDesc(value = "好烦", desc = "泛型处理好鬼烦")
    public A<F, C<E, D>, B> generic(@ApiParam(name = "sss") @RequestBody String sss) {
        return null;
    }
}
