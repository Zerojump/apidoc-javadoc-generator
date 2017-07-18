package com.cmy.apidoc.generator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.Test;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;

import static com.cmy.apidoc.generator.ApiDocBuilder.BRACE_CLOSE;
import static com.cmy.apidoc.generator.ApiDocBuilder.BRACE_OPEN;
import static com.cmy.apidoc.generator.ApiDocBuilder.NEW_LINE;
import static com.cmy.apidoc.generator.ApiDocBuilder.SPACE_ONE;
import static com.cmy.apidoc.generator.ApiDocBuilder.createApiDocContentFromClass;
import static com.cmy.apidoc.generator.ApiDocBuilder.writeFile;

/**
 * <p>@author chenmingyi
 * <p>@version 1.0
 * <p>Date: 2017/7/2
 * <p>
 * To change this template use File | Settings | File and Code Templates | Includes .
 */
public class LoadClassTest {

    public static final ClassLoader loader;

    static {
        ClassLoader parent = Thread.currentThread().getContextClassLoader();
        URL[] urls = new URL[1];
        try {
            urls[0] = new URL("file:///D:\\code\\apidoc-javadoc-generator\\apidoc-javadoc-generator-core\\target\\test-classes");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        loader = new URLClassLoader(urls, parent);
    }

    private static final String springMvcAnnotation = "com.cmy.apidoc.generator.material.controller.SpringMvcAnnotationClass";
    private static final String employeeVo = "com.cmy.apidoc.generator.material.vo.EmployeeVo";
    private static final String permissionVo = "com.cmy.apidoc.generator.material.vo.PermissionVo";
    private static Class<?> employeeVoClass;
    private static Class<?> permissionVoClass;
    private static Class<?> springMvcAnnotationClass;

    static Gson GSON;

    static {
        try {
            employeeVoClass = loader.loadClass(employeeVo);
            permissionVoClass = loader.loadClass(permissionVo);
            springMvcAnnotationClass = loader.loadClass(springMvcAnnotation);

            GsonBuilder gsonBuilder = new GsonBuilder();
            GSON = gsonBuilder.serializeNulls()
                    .setPrettyPrinting()
                    .create();

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testLoadClass() throws Exception {
        //Class<?> clazz1 = loader.loadClass(employeeVo);
        Class<?> clazz1 = Class.forName(springMvcAnnotation, true, loader);
        System.out.println(clazz1);
        System.out.println(Arrays.toString(clazz1.getAnnotations()));
    }

    @Test
    public void testGsonAdapter() throws Exception {
        Class<?> employeeVoClass = loader.loadClass(employeeVo);
        Class<?> permissionVoClass = loader.loadClass(permissionVo);
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.serializeNulls()
                .setPrettyPrinting()
                .create();

        String s = "{\"id\":1,\"permissionVoSet\":[1,2,3]}";
        Object o = gson.fromJson(s, employeeVoClass);
        System.out.println(gson.toJson(o));
    }

    /**
     * @api {get} /user/:id Read data of a User
     * @apiVersion 0.2.0
     * @apiName GetUser
     * @apiGroup User
     * @apiPermission admin
     * @apiDescription Here you can describe the function.
     * Multilines are possible.
     * @apiParam {String} id The Users-ID.
     * @apiSuccess {String} id         The Users-ID.
     * @apiSuccess {Date}   name       Fullname of the User.
     * @apiError UserNotFound   The <code>id</code> of the User was not found.
     */

    @Test
    public void testPrintDoc() throws Exception {
        StringBuilder apiDoc = createApiDocContentFromClass(springMvcAnnotationClass);
        System.out.println(apiDoc.toString());
    }

    @Test
    public void testCreateFile() throws Exception {
        StringBuilder apiDoc = createApiDocContentFromClass(springMvcAnnotationClass);

        StringBuilder sb = new StringBuilder();
        String className = "ApiDocApiDefine";
        sb.append("package com.cmy.apidoc.generator.doc.doc03;").append(NEW_LINE).append(NEW_LINE);
        sb.append("public class ").append(className).append(SPACE_ONE).append(BRACE_OPEN).append(NEW_LINE);
        sb.append(apiDoc);
        sb.append(BRACE_CLOSE);

        String fileName = className + ".java";
        String dir = "D:\\code\\apidoc-javadoc-generator\\apidoc-javadoc-generator-core\\src\\test\\java\\com\\cmy\\apidoc\\generator\\doc\\doc03";
        File file = new File(dir, fileName);
        writeFile(file, sb.toString(), "UTF-8");
    }
}
