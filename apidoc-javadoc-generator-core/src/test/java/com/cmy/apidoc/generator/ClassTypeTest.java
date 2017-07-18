package com.cmy.apidoc.generator;

import com.cmy.apidoc.generator.material.commons.ResponseWrap;
import com.cmy.apidoc.generator.material.controller.SpringMvcAnnotationClass;
import com.cmy.apidoc.generator.material.generic.B;
import com.cmy.apidoc.generator.material.vo.PermissionVo;
import org.junit.Test;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>@author chenmingyi
 * <p>@version 1.0
 * <p>Date: 2017/7/9
 * <p>
 * To change this template use File | Settings | File and Code Templates | Includes .
 */
public class ClassTypeTest {

    Class<SpringMvcAnnotationClass> clazz = SpringMvcAnnotationClass.class;

    @Test
    public void test1() throws Exception {
        return;

    }

    @Test
    public void test2() throws Exception {
        Method method1 = clazz.getMethod("get", int.class, PermissionVo.class);

        System.out.println("method1.getGenericParameterTypes = " + Arrays.toString(method1.getGenericParameterTypes()));
        System.out.println("method1.getParameterTypes = " + Arrays.toString(method1.getParameterTypes()));
        System.out.println("method1.getGenericReturnType = " + method1.getGenericReturnType());
        System.out.println("method1.getReturnType = " + method1.getReturnType());


        Method method2 = clazz.getMethod("edit", List.class);
        System.out.println();

        System.out.println("method2.getGenericParameterTypes = " + Arrays.toString(method2.getGenericParameterTypes()));
        System.out.println("method2.getParameterTypes = " + Arrays.toString(method2.getParameterTypes()));
        System.out.println("method2.getGenericReturnType = " + method2.getGenericReturnType());
        System.out.println("method2.getReturnType = " + method2.getReturnType());

    }


    @Test
    public void test4() throws Exception {
        Method method = clazz.getMethod("post", List.class);

        Type parameterizedType = method.getParameters()[0].getParameterizedType();

        return;

    }


    @Test
    public void test8() throws Exception {
        ResponseWrap<PermissionVo> responseWrap = new ResponseWrap<>();
        Class<ResponseWrap> responseWrapClass = ResponseWrap.class;
        System.out.println(LoadClassTest.GSON.toJson(responseWrap));

        Method method = responseWrapClass.getMethod("setData", Object.class);
        method.setAccessible(true);
        TypeVariable<Method>[] typeParameters = method.getTypeParameters();
        typeParameters[0].getTypeName();
        System.out.println(typeParameters);
        method.invoke(responseWrap, new PermissionVo());
        System.out.println(LoadClassTest.GSON.toJson(responseWrap));
        System.out.println("method.toGenericString() = " + method.toGenericString());
    }

    @Test
    public void test12() throws Exception {
        //new Long(0);

        Class<B> bClass = B.class;
        Field field = bClass.getDeclaredField("name");

        System.out.println(field.getModifiers());
    }

    @Test
    public void test13() throws Exception {
        Method method = clazz.getMethod("generic");
        System.out.println(method.getReturnType().equals(Void.TYPE));
        System.out.println(method.getReturnType().equals(void.class));
    }

    @Test
    public void test14() throws Exception {
        Pattern compile = Pattern.compile("<[A-Z,<>]+>$");
        Matcher matcher = compile.matcher("java.util.List<T,E<C,D>,B>");
        //Matcher matcher = compile.matcher("java.util.List<T,E<C,D>,B>");
        matcher.find();
        String group = matcher.group();
        System.out.println("group = " + group);
        String replaceAll = group.replaceAll("[<>,]", "");
        System.out.println("replaceAll = " + replaceAll);
        System.out.println("Arrays.toString(group.split(,)) = " + Arrays.toString(replaceAll.split("")));
    }

    @Test
    public void test15() throws Exception {
        RequestMethod[] methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.DELETE};
        String x = Arrays.toString(methods);
        System.out.println(x.substring(1, x.length() - 1));
    }

    @Test
    public void test16() throws Exception {
        //java.version
        //Java 运行时环境版本
        //java.home
        //Java 安装目录
        //os.name
        //        操作系统的名称
        //os.version
        //        操作系统的版本
        //user.name
        //        用户的账户名称
        //user.home
        //        用户的主目录
        //user.dir
        //        用户的当前工作目录

        Properties properties = System.getProperties();
        for (Map.Entry<Object, Object> objectObjectEntry : properties.entrySet()) {
            System.out.println(objectObjectEntry.getKey() + "---" + objectObjectEntry.getValue());
        }

    }

    @Test
    public void test17() throws Exception {
        File dir = new File("D:\\ideaworkspace\\custom_plugin\\apidoc-generator-maven-plugin\\src\\test\\java");
        String child = "com.cmy.baby".replaceAll("\\.", "/");
        System.out.println("child = " + child);
        File file = new File(dir, child);
        System.out.println("file.getAbsolutePath() = " + file.getAbsolutePath());
        System.out.println(file.isDirectory());
        System.out.println("file.isFile() = " + file.isFile());
        System.out.println("file.mkdirs() = " + file.mkdirs());
    }
}
