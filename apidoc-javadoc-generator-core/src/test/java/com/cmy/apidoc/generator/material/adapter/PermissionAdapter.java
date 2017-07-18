package com.cmy.apidoc.generator.material.adapter;

import com.cmy.apidoc.generator.material.vo.PermissionVo;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

/**
 * <p>@author chenmingyi
 * <p>@version 1.0
 * <p>Date: 2017/7/3
 * <p>
 * To change this template use File | Settings | File and Code Templates | Includes .
 */
public class PermissionAdapter extends TypeAdapter<PermissionVo> {

    @Override
    public void write(JsonWriter out, PermissionVo permissionVo) throws IOException {
        out.beginObject()
                .name("id").value(permissionVo.getId())
                .name("name").value(permissionVo.getName())
                .endObject();
    }

    @Override
    public PermissionVo read(JsonReader in) throws IOException {
        //in.beginObject();
        int id = in.nextInt();
        PermissionVo permissionVo = new PermissionVo();
        permissionVo.setId(id);
        //in.endObject();
        return permissionVo;
    }
}
