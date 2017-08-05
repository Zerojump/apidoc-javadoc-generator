package com.cmy.apidoc.generator;

import com.cmy.apidoc.generator.annotations.ApiDesc;
import com.cmy.apidoc.generator.annotations.ApiErrorDefine;
import com.cmy.apidoc.generator.annotations.ApiErrorFactoryMethod;
import com.cmy.apidoc.generator.annotations.ApiParam;
import com.cmy.apidoc.generator.enums.ApiDocEnum;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


/**
 * 工具类
 * <p>
 * <p>@author chenmingyi
 * <p>@version 1.0
 * <p>date: 2017/7/11
 */
public final class ApiDocBuilder {
    private ApiDocBuilder() {
    }

    private static Logger log = Logger.getLogger("ApiDocBuilder");

    private static final String JAVA_DOC_START = "/**";
    public static final String SPACE_ONE = " ";
    public static final String NEW_LINE = "\n";
    public static final String BRACE_OPEN = "{";
    public static final String BRACE_CLOSE = "}";
    private static final String BRACKET_OPEN = "[";
    private static final String BRACKET_CLOSE = "]";
    private static final String PAREN_OPEN = "(";
    private static final String PAREN_CLOSE = ")";
    private static final String DOC_LINE_START = " * @";
    private static final String DOC_START = " *";
    private static final String SLASH = "/";
    private static final String EMPTY = "";
    private static final String EQUAL = "=";
    private static final String JSON_BODY = "json";
    public static String VERSION = "0.0.1";
    private static final String COLLECTION = BRACKET_OPEN + BRACKET_CLOSE;

    private static final Gson GSON;

    static {
        GsonBuilder gsonBuilder = new GsonBuilder();
        GSON = gsonBuilder.serializeNulls()
                .setPrettyPrinting()
                .create();
    }

    private static final Pattern GENERIC_CODE_PATTERN = Pattern.compile("<[A-Z,<>]+>$");

    public static boolean isJSONObjectType(Class<?> clazz) {
        if (clazz.isPrimitive()) {
            return false;
        }

        Class<?> superclass = clazz.getSuperclass();
        if (superclass != null) {
            if (Number.class.equals(superclass)) {
                return false;
            }

            if (CharSequence.class.equals(superclass)) {
                return false;
            }
        }

        if (Number.class.equals(clazz)) {
            return false;
        }

        if (CharSequence.class.equals(clazz)) {
            return false;
        }

        if (Character.class.equals(clazz)) {
            return false;
        }

        if (clazz.isAnnotation()) {
            throw new IllegalArgumentException();
        }

        return true;
    }


    public static void writeFile(File file, String content, String fileEncoding) throws IOException {
        FileOutputStream fos = new FileOutputStream(file, false);
        OutputStreamWriter osw;
        if (fileEncoding == null) {
            osw = new OutputStreamWriter(fos);
        } else {
            osw = new OutputStreamWriter(fos, fileEncoding);
        }

        BufferedWriter bw = new BufferedWriter(osw);
        bw.write(content);
        bw.close();
    }

    public static StringBuilder createApiDocContentFromClass(Class<?> springMvcClass) throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        //Controller的类@RequestMapping注解
        RequestMapping classRMAntt = springMvcClass.getAnnotation(RequestMapping.class);

        //Controller的类@RequestMapping注解的value 值作为apiGroup
        String apiGroup = classRMAntt.value().length == 0 ? "" : classRMAntt.value()[0];

        String classSimpleName = springMvcClass.getSimpleName();

        StringBuilder apiDocSB = new StringBuilder();

        ApiErrorDefine classApiErrorDefineAntt = springMvcClass.getAnnotation(ApiErrorDefine.class);
        List<String> classApiUseCodeList = getApiUseCodeList(apiDocSB, classApiErrorDefineAntt, classSimpleName);

        //Controller的所有方法
        Method[] methods = springMvcClass.getMethods();
        for (Method method : methods) {
            RequestMapping methodRMAntt = method.getAnnotation(RequestMapping.class);
            //过滤掉不少接口的方法
            if (methodRMAntt == null) {
                continue;
            }

            String methodName = method.getName();

            String prefix = classSimpleName + methodName;
            ApiErrorDefine methodApiErrorDefineAntt = method.getAnnotation(ApiErrorDefine.class);
            List<String> methodApiUseCodeList = getApiUseCodeList(apiDocSB, methodApiErrorDefineAntt, prefix);

            methodApiUseCodeList.addAll(classApiUseCodeList);

            StringBuilder apiSB = buildApiDocBasicSingle(apiGroup, method, methodRMAntt, methodApiUseCodeList);
            apiDocSB.append(apiSB);


            //apiDocSB.append()
        }
        return apiDocSB;
    }

    private static List<String> getApiUseCodeList(StringBuilder apiDocSB, ApiErrorDefine methodApiErrorDefineAntt, String prefix) throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        List<String> methodApiUseCodeList = new ArrayList<>();
        if (methodApiErrorDefineAntt != null) {
            StringBuilder apiErrorDefineSB = buildApiErrorDefineBasic(methodApiErrorDefineAntt, prefix);
            apiDocSB.append(apiErrorDefineSB);

            Class<?> clazz = methodApiErrorDefineAntt.clazz();
            String simpleName = clazz.getSimpleName();
            String[] errorFactoryMethodArr = methodApiErrorDefineAntt.methods();
            methodApiUseCodeList = Arrays.stream(errorFactoryMethodArr).map(s -> simpleName + s + prefix).collect(Collectors.toList());
        }
        return methodApiUseCodeList;
    }

    private static StringBuilder buildApiErrorDefineBasic(ApiErrorDefine methodApiErrorDefineAntt, String prefix) throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        StringBuilder apiErrorDefineSB = new StringBuilder();

        Class<?> clazz = methodApiErrorDefineAntt.clazz();
        String clazzSimpleName = clazz.getSimpleName();
        Object instance = clazz.newInstance();

        String[] errorDefineFactoryMethodArr = methodApiErrorDefineAntt.methods();
        if (errorDefineFactoryMethodArr.length != 0) {
            for (String m : errorDefineFactoryMethodArr) {
                apiErrorDefineSB.append(NEW_LINE);

                Method factoryMethod = clazz.getMethod(m);

                ApiErrorFactoryMethod apiErrorFactoryMethodAntt = factoryMethod.getAnnotation(ApiErrorFactoryMethod.class);
                String apiErrorCode = m;
                String errorDesc = null;
                int statusCode = 200;
                if (apiErrorFactoryMethodAntt != null) {
                    if (!apiErrorFactoryMethodAntt.value().isEmpty()) {
                        apiErrorCode = apiErrorFactoryMethodAntt.value();
                    }

                    if (!apiErrorFactoryMethodAntt.desc().isEmpty()) {
                        errorDesc = apiErrorFactoryMethodAntt.desc();
                    }

                    statusCode = apiErrorFactoryMethodAntt.statusCode();
                }
                /*
                 * @apiDefine MyError
                 * @apiError UserNotFound The <code>id</code> of the User was not found.
                 */

                /*
                 * @api {get} /user/:id
                 * @apiErrorExample {json} Error-Response:
                 *     HTTP/1.1 404 Not Found
                 *     {
                 *       "error": "UserNotFound"
                 *     }
                 */
                apiErrorDefineSB.append(JAVA_DOC_START).append(NEW_LINE);
                //@apiDefine
                apiErrorDefineSB.append(DOC_LINE_START).append(ApiDocEnum.API_DEFINE.getCode())
                        .append(SPACE_ONE).append(clazzSimpleName).append(m).append(prefix).append(NEW_LINE);
                //@apiError
                apiErrorDefineSB.append(DOC_LINE_START).append(ApiDocEnum.API_ERROR.getCode())
                        .append(SPACE_ONE).append(apiErrorCode);
                if (errorDesc != null) {
                    apiErrorDefineSB.append(SPACE_ONE).append(errorDesc);
                }
                apiErrorDefineSB.append(NEW_LINE);

                //@apiErrorExample
                apiErrorDefineSB.append(DOC_LINE_START).append(ApiDocEnum.API_ERROR_EXAMPLE.getCode())
                        .append(SPACE_ONE).append(BRACE_OPEN).append(JSON_BODY).append(BRACE_CLOSE)
                        .append(SPACE_ONE).append(apiErrorCode).append(NEW_LINE);
                apiErrorDefineSB.append(DOC_START)
                        .append(SPACE_ONE).append("HTTP")
                        .append(SPACE_ONE).append(statusCode).append(NEW_LINE);

                Object errorExample = factoryMethod.invoke(instance);
                apiErrorDefineSB.append(DOC_START).append(GSON.toJson(errorExample)).append(NEW_LINE);
                apiErrorDefineSB.append(" */").append(NEW_LINE);
            }
        }
        return apiErrorDefineSB;
    }

    private static StringBuilder buildApiDocBasicSingle(String apiGroup, Method method, RequestMapping methodRMAntt, List<String> methodApiUseCodeList) throws InstantiationException, IllegalAccessException {
        //http请求方法，默认是get
        RequestMethod[] reqMethods = methodRMAntt.method();

        String reqMethod;
        if (reqMethods.length == 0) {
            reqMethod = "GET";
        } else {
            String reqMethodString = Arrays.toString(reqMethods);
            reqMethod = reqMethodString.substring(1, reqMethodString.length() - 1);
        }

        //http URL路由
        String[] reqPaths = methodRMAntt.value();
        String reqUrl = reqPaths.length == 0 || StringUtils.isEmpty(reqPaths[0]) ? EMPTY : SLASH + reqPaths[0];

        //接口方法名称
        ApiDesc apiDescAntt = method.getAnnotation(ApiDesc.class);
        String reqName = StringUtils.isEmpty(methodRMAntt.name()) ? method.getName() : methodRMAntt.name();
        String apiName = method.getDeclaringClass().getCanonicalName() + "." + method.getName();
        String apiDesc = reqName;
        if (apiDescAntt != null) {
            if (!apiDescAntt.value().isEmpty()) {
                apiName = apiDescAntt.value();
            }
            if (!apiDescAntt.desc().isEmpty()) {
                apiDesc = apiDescAntt.desc();
            }
        }

        StringBuilder apiSB = new StringBuilder();
        apiSB.append(NEW_LINE);

        apiSB.append(JAVA_DOC_START).append(NEW_LINE);
        //@api
        apiSB.append(DOC_LINE_START).append(ApiDocEnum.API.getCode())
                .append(SPACE_ONE).append(BRACE_OPEN).append(reqMethod).append(BRACE_CLOSE)
                .append(SPACE_ONE).append(SLASH).append(apiGroup).append(reqUrl)
                .append(SPACE_ONE).append(reqName)
                .append(NEW_LINE);
        //@apiVersion TODO version 处理
        apiSB.append(DOC_LINE_START).append(ApiDocEnum.API_VERSION.getCode()).append(SPACE_ONE).append(VERSION).append(NEW_LINE);
        //@apiName
        apiSB.append(DOC_LINE_START).append(ApiDocEnum.API_NAME.getCode()).append(SPACE_ONE).append(apiName).append(NEW_LINE);
        //@apiGroup
        apiSB.append(DOC_LINE_START).append(ApiDocEnum.API_GROUP.getCode()).append(SPACE_ONE).append(apiGroup).append(NEW_LINE);
        //@apiDesc
        apiSB.append(DOC_LINE_START).append(ApiDocEnum.API_DESCRIPTION.getCode()).append(SPACE_ONE).append(apiDesc).append(NEW_LINE);


            /*
             * @api {post} /user/
             * @apiParam {String} [firstname]  Optional Firstname of the User.
             * @apiParam {String} lastname     Mandatory Lastname.
             * @apiParam {String} country="DE" Mandatory with default value "DE".
             * @apiParam {Number} [age=18]     Optional Age with default 18.
             *
             * @apiParam (Login) {String} pass Only logged in users can post this.
             *                                 In generated documentation a separate
             *                                 "Login" Block will be generated.
             * {string {..5}="small","huge"}
             */

        Parameter bodyParameter = null;
        Parameter[] parameterArr = method.getParameters();
        //<editor-fold desc="@apiParam">
        for (Parameter p : parameterArr) {
            RequestBody body = p.getAnnotation(RequestBody.class);
            ApiParam apiParamAntt = p.getAnnotation(ApiParam.class);

            String paramName = p.getName();
            if (body != null) {
                bodyParameter = p;
                //TODO parameterType 为集合的情况处理

                Type parameterizedType = p.getParameterizedType();

                StringBuilder ObjParamSB = createApiParamFromNestedObj(parameterizedType, apiParamAntt, paramName);

                apiSB.append(ObjParamSB);

            } else {
                apiSB.append(DOC_START).append(NEW_LINE);

                Class<?> parameterType = p.getType();
                StringBuilder paramSB = createApiParam(null, apiParamAntt, parameterType.getSimpleName(), false, paramName);
                apiSB.append(paramSB);
            }
        }
        //</editor-fold>

        //@apiParamExample [{type}] [title]
        //example
            /*
             * @api {get} /user/:id
             * @apiParamExample {json} Request-Example:
             *     {
             *       "id": 4711
             *     }
             */

        //<editor-fold desc="@apiParamExample">
        if (bodyParameter != null) {
            apiSB.append(DOC_START).append(NEW_LINE);
            apiSB.append(DOC_LINE_START).append(ApiDocEnum.API_PARAM_EXAMPLE.getCode())
                    .append(SPACE_ONE).append(BRACE_OPEN).append(JSON_BODY).append(BRACE_CLOSE)
                    .append(SPACE_ONE).append("Request-Example:").append(NEW_LINE);

            Object successExample = createApiSuccessExample(bodyParameter.getType(), bodyParameter.getParameterizedType());
            apiSB.append(DOC_START).append(SPACE_ONE).append(GSON.toJson(successExample)).append(NEW_LINE);
        }
        //</editor-fold>

        Type genericReturnType = method.getGenericReturnType();
        if (genericReturnType != Void.TYPE) {
            //@apiSuccess (200) {String} lastname  Lastname of the User.
            //<editor-fold desc="@apiSuccess">
            //接口方法返回类型的泛型嵌套的最后一层的 名称标识
            ApiParam methodApiParamAntt = method.getAnnotation(ApiParam.class);
            //TODO VOID 处理
            StringBuilder objSuccessParam = createApiSuccessFromNestedObj(genericReturnType, methodApiParamAntt);
            apiSB.append(objSuccessParam);
            //</editor-fold>

            //<editor-fold desc="@apiSuccessExample">
            Class<?> returnType = method.getReturnType();
            Object successExample = createApiSuccessExample(returnType, genericReturnType);

            apiSB.append(DOC_START).append(NEW_LINE);
            apiSB.append(DOC_LINE_START).append(ApiDocEnum.API_SUCCESS_EXAMPLE.getCode())
                    .append(SPACE_ONE).append(BRACE_OPEN).append(JSON_BODY).append(BRACE_CLOSE)
                    .append(SPACE_ONE).append("Success-Response:").append(NEW_LINE);
            apiSB.append(DOC_START).append(" HTTP 200 OK").append(NEW_LINE);
            apiSB.append(DOC_START).append(SPACE_ONE).append(GSON.toJson(successExample)).append(NEW_LINE);
            //</editor-fold>
        }

        //@apiUse
        if (!methodApiUseCodeList.isEmpty()) {
            apiSB.append(DOC_START).append(NEW_LINE);
            for (String apiUseCode : methodApiUseCodeList) {
                apiSB.append(DOC_LINE_START).append(ApiDocEnum.API_USE.getCode()).append(SPACE_ONE).append(apiUseCode).append(NEW_LINE);
            }
        }

        apiSB.append(" */").append(NEW_LINE);
        return apiSB;
    }

    //TODO 暂时不支持 Map
    private static StringBuilder createApiSuccessFromNestedObj(Type genericReturnType, ApiParam apiParamAntt) {
        StringBuilder objSuccessParam = new StringBuilder();
        createApiSuccessFromNestedObj(genericReturnType, apiParamAntt, objSuccessParam);
        return objSuccessParam;
    }

    private static void createApiSuccessFromNestedObj(Type genericReturnType, ApiParam apiParamAntt, StringBuilder objSuccessParam) {
        List<Class<?>> genericReturnTypeList = getTypeListFromNestedGenericType(genericReturnType);

        int nestedNum = genericReturnTypeList.size();
        for (int i = 0; i < nestedNum; i++) {
            Class<?> clazz = genericReturnTypeList.get(i);

            String clazzName = clazz.getSimpleName();

            //泛型类型是基本类型的包装类 TODO 测试 String 基本类型数组
            if (ClassUtils.isPrimitiveWrapper(clazz) || clazz.isAssignableFrom(String.class) || isPrimitiveOrWrapperOrStringArray(clazz)) {
                objSuccessParam.append(DOC_START).append(NEW_LINE);

                String group = clazzName;
                String name = clazzName;
                String desc = clazzName;
                if (apiParamAntt != null) {
                    if (!apiParamAntt.group().isEmpty()) {
                        group = apiParamAntt.group();
                    }
                    if (!apiParamAntt.name().isEmpty()) {
                        name = apiParamAntt.name();
                    }
                    if (!apiParamAntt.desc().isEmpty()) {
                        desc = apiParamAntt.desc();
                    }
                }

                boolean isArray = i != 0 && i == nestedNum - 1 && Collection.class.isAssignableFrom(genericReturnTypeList.get(i - 1));

                StringBuilder successSB = buildApiSuccess(group, clazzName, name, isArray, desc);
                objSuccessParam.append(successSB);
                break;
            }

            List<Field> fieldList = FieldUtils.getAllFieldsList(clazz);

            if (fieldList.isEmpty()) {
                continue;
            }

            objSuccessParam.append(DOC_START).append(NEW_LINE);
            for (Field field : fieldList) {

                //TODO field 是基本类型及其包装类的情况
                //TODO 递归 field 也是对象的情况
                //TODO 递归 field 数组或集合，Map的情况

                if (field.getModifiers() > Modifier.PROTECTED) {
                    continue;
                }

                Class<?> fieldType = field.getType();
                ApiParam fieldParamAntt = field.getAnnotation(ApiParam.class);

                //fieldType.isAssignableFrom(String.class) Object类型会判断为true
                if (ClassUtils.isPrimitiveOrWrapper(fieldType) || fieldType.isAssignableFrom(String.class) || Date.class.isAssignableFrom(fieldType) || isPrimitiveOrWrapperOrStringArray(fieldType)) {
                    String group = clazzName;
                    String name = field.getName();
                    String desc = field.getName();
                    if (apiParamAntt != null) {
                        if (!apiParamAntt.group().isEmpty()) {
                            group = apiParamAntt.group();
                        } else {
                            ApiParam clazzApiParamAntt = clazz.getAnnotation(ApiParam.class);
                            if (clazzApiParamAntt != null && !clazzApiParamAntt.name().isEmpty()) {
                                group = clazzApiParamAntt.name();
                            }
                        }
                    }
                    if (fieldParamAntt != null) {
                        if (!fieldParamAntt.name().isEmpty()) {
                            name = fieldParamAntt.name();
                        }
                        if (!fieldParamAntt.desc().isEmpty()) {
                            desc = fieldParamAntt.desc();
                        }
                    }

                    String typeName = fieldType.getSimpleName();

                    StringBuilder successSB = buildApiSuccess(group, typeName, name, false, desc);

                    objSuccessParam.append(successSB);
                } else {
                    //当Field序列化后还是还是一个对象时，递归处理
                    Type genericType = field.getGenericType();
                    createApiSuccessFromNestedObj(genericType, fieldParamAntt, objSuccessParam);
                }
            }
        }
    }

    private static StringBuilder buildApiSuccess(String group, String type, String name, boolean isArray, String desc) {
        StringBuilder successSB = new StringBuilder();
        successSB.append(DOC_LINE_START).append(ApiDocEnum.API_SUCCESS.getCode())
                .append(SPACE_ONE).append(PAREN_OPEN).append(group).append(PAREN_CLOSE)
                .append(SPACE_ONE).append(BRACE_OPEN).append(type);
        if (isArray) {
            successSB.append(COLLECTION);
        }
        successSB.append(BRACE_CLOSE)
                .append(SPACE_ONE).append(name)
                .append(SPACE_ONE).append(desc);
        successSB.append(NEW_LINE);
        return successSB;
    }

    private static StringBuilder createApiParamFromNestedObj(Type parameterizedType, ApiParam apiParamAntt, String paramName) {
        StringBuilder ObjParamSB = new StringBuilder();

        createApiParamFromNestedObj(parameterizedType, apiParamAntt, paramName, ObjParamSB);
        return ObjParamSB;
    }

    private static void createApiParamFromNestedObj(Type parameterizedType, ApiParam apiParamAntt, String attributeName, StringBuilder objParamSB) {
        List<Class<?>> classList = getTypeListFromNestedGenericType(parameterizedType);

        int nestedNum = classList.size();
        for (int i = 0; i < nestedNum; i++) {
            Class<?> clazz = classList.get(i);

            String group = clazz.getSimpleName();

            //可以不用嵌套去查的类 数组、Date
            if (isPrimitiveOrWrapperOrStringArray(clazz) || Date.class.isAssignableFrom(clazz)) {
                objParamSB.append(DOC_START).append(NEW_LINE);
                StringBuilder paramSB = createApiParam(BRACE_OPEN + JSON_BODY + BRACE_CLOSE + group, apiParamAntt, group, false, attributeName);
                objParamSB.append(paramSB);
                break;
            }

            List<Field> fieldList = FieldUtils.getAllFieldsList(clazz);
            if (fieldList.isEmpty()) {
                continue;
            }

            //泛型类型是基本类型的包装类 TODO 测试 String 基本类型数组
            if (ClassUtils.isPrimitiveWrapper(clazz) || clazz.isAssignableFrom(String.class)) {
                objParamSB.append(DOC_START).append(NEW_LINE);
                boolean isArray = i != 0 && i == nestedNum - 1 && Collection.class.isAssignableFrom(classList.get(i - 1));

                StringBuilder paramSB = createApiParam(BRACE_OPEN + JSON_BODY + BRACE_CLOSE + group, apiParamAntt, group, isArray, attributeName);
                objParamSB.append(paramSB);
                break;
            }

            objParamSB.append(DOC_START).append(NEW_LINE);
            for (Field field : fieldList) {

                if (field.getModifiers() > Modifier.PROTECTED) {
                    continue;
                }

                //TODO field 是基本类型及其包装类的情况
                //TODO 递归 field 也是对象的情况
                //TODO 递归 field 数组或集合，Map的情况

                Class<?> fieldType = field.getType();
                String fieldName = field.getName();
                ApiParam fieldParamAntt = field.getAnnotation(ApiParam.class);

                //if (fieldType.isAssignableFrom(Collection.class)) {
                //    fieldType.getTypeParameters()
                //}

                if (ClassUtils.isPrimitiveOrWrapper(fieldType) || fieldType.isAssignableFrom(String.class) || isPrimitiveOrWrapperOrStringArray(fieldType)) {
                    String typeName = fieldType.getSimpleName();

                    StringBuilder paramSB = createApiParam(BRACE_OPEN + JSON_BODY + BRACE_CLOSE + group, fieldParamAntt, typeName, false, fieldName);
                    objParamSB.append(paramSB);
                } else {
                    //当Field序列化后还是还是一个对象时，递归处理
                    Type genericType = field.getGenericType();
                    createApiParamFromNestedObj(genericType, fieldParamAntt, fieldName, objParamSB);
                }
            }
        }
    }

    private static StringBuilder createApiParam(String group, ApiParam fieldParamAntt, String typeName, boolean isArray, String fieldName) {
        StringBuilder paramSB = new StringBuilder();
        paramSB.append(DOC_LINE_START).append(ApiDocEnum.API_PARAM.getCode());

        if (fieldParamAntt != null) {
            String actualGroup = StringUtils.isEmpty(fieldParamAntt.group()) ? group : fieldParamAntt.group();
            String actualParamName = StringUtils.isEmpty(fieldParamAntt.name()) ? fieldName : fieldParamAntt.name();
            paramSB.append(buildApiParamByAntt(fieldParamAntt, actualGroup, typeName, isArray, actualParamName));

        } else {
            paramSB.append(buildApiParamSimply(group, typeName, isArray, fieldName));
        }
        paramSB.append(NEW_LINE);
        return paramSB;
    }

    private static StringBuilder buildApiParamSimply(String groupName, String typeName, boolean isArray, String fieldName) {
        StringBuilder paramSB = new StringBuilder();

        if (!StringUtils.isEmpty(groupName)) {
            paramSB.append(SPACE_ONE).append(PAREN_OPEN).append(groupName).append(PAREN_CLOSE);
        }
        paramSB.append(SPACE_ONE).append(BRACE_OPEN).append(typeName);
        if (isArray) {
            paramSB.append(COLLECTION);
        }
        paramSB.append(BRACE_CLOSE)
                .append(SPACE_ONE).append(fieldName);

        return paramSB;
    }

    private static StringBuilder buildApiParamByAntt(ApiParam fieldParamAntt, String group, String typeName, boolean isArray, String paraName) {
        StringBuilder paramSB = new StringBuilder();

        if (!StringUtils.isEmpty(group)) {
            paramSB.append(SPACE_ONE).append(PAREN_OPEN).append(group).append(PAREN_CLOSE);
        }

        paramSB.append(SPACE_ONE).append(BRACE_OPEN).append(typeName);
        if (isArray) {
            paramSB.append(COLLECTION);
        }

        if (!StringUtils.isEmpty(fieldParamAntt.size())) {
            paramSB.append(SPACE_ONE).append(BRACE_OPEN).append(fieldParamAntt.size()).append(BRACE_CLOSE);
        }
        if (!StringUtils.isEmpty(fieldParamAntt.allowedValues())) {
            paramSB.append(EQUAL).append(fieldParamAntt.allowedValues());
        }
        paramSB.append(BRACE_CLOSE);
        paramSB.append(SPACE_ONE);

        StringBuilder param = new StringBuilder();
        param.append(paraName);
        if (!StringUtils.isEmpty(fieldParamAntt.defaultValue())) {
            param.append(EQUAL).append(fieldParamAntt.defaultValue());
        }
        if (!fieldParamAntt.required()) {
            param.insert(0, BRACKET_OPEN).append(BRACKET_CLOSE);
        }
        paramSB.append(param);

        if (!StringUtils.isEmpty(fieldParamAntt.desc())) {
            paramSB.append(SPACE_ONE).append(fieldParamAntt.desc());
        }

        return paramSB;
    }

    private static List<Class<?>> getTypeListFromNestedGenericType(Type type) {
        List<Class<?>> classList = new ArrayList<>();

        getTypeListFromNestedGenericType(type, classList);

        return classList;
    }

    private static void getTypeListFromNestedGenericType(Type type, List<Class<?>> classList) {

        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;

            classList.add((Class) parameterizedType.getRawType());

            Type[] parameterArgTypes = parameterizedType.getActualTypeArguments();
            for (Type parameterArgType : parameterArgTypes) {
                getTypeListFromNestedGenericType(parameterArgType, classList);
            }
        } else if (type instanceof Class) {
            classList.add((Class) type);
        }
    }

    private static boolean isPrimitiveOrWrapperOrStringArray(Class<?> clazz) {
        for (Class<?> c = clazz; c.isArray(); ) {
            Class<?> componentType = c.getComponentType();
            if (ClassUtils.isPrimitiveOrWrapper(componentType) || componentType.isAssignableFrom(String.class)) {
                return true;
            } else {
                c = componentType;
            }
        }
        return false;
    }

    private static Map<Integer, Map<String, Class<?>>> getNestedGenericTypeMap(List<Class<?>> classList) {
        int classListSize = classList.size();
        int[] genericTypeNumArr = new int[classListSize];

        Map<Class<?>, List<String>> classGenericCodeListMap = new LinkedHashMap<>();

        for (int i = 0; i < classListSize; i++) {
            Class<?> clazz = classList.get(i);

            TypeVariable<? extends Class<?>>[] typeParameterArr = clazz.getTypeParameters();
            int genericTypeNum = typeParameterArr.length;
            genericTypeNumArr[i] = genericTypeNum;

            if (genericTypeNum != 0) {
                List<String> genericCodeList = Arrays.stream(typeParameterArr).map(TypeVariable::getName).collect(Collectors.toList());
                classGenericCodeListMap.put(clazz, genericCodeList);
            }
        }

        if (!classGenericCodeListMap.isEmpty()) {
            Map<Integer, Map<String, Class<?>>> nestedGenericTypeMap = new HashMap<>();

            int level = 0;
            for (int i = 0; i < classListSize; i++) {

                int genericNum = genericTypeNumArr[i];

                if (genericNum != 0) {
                    Class<?> clazz = classList.get(i);
                    List<String> genericCodeList = classGenericCodeListMap.get(clazz);

                    Map<String, Class<?>> map = new HashMap<>(genericNum);

                    int offset = 0;
                    int genericIndex = 0;//genericCodeList 的索引角标
                    //如果循环不加 j < classListSize 条件，在泛型不加实参时会出现角标越界异常
                    for (int j = i + 1; j < classListSize && genericIndex < genericNum; j++) {
                        if (offset == 0) {
                            map.put(genericCodeList.get(genericIndex++), classList.get(j));
                        } else {
                            offset--;
                        }
                        offset += genericTypeNumArr[j];
                    }
                    nestedGenericTypeMap.put(level++, map);
                }
            }
            return nestedGenericTypeMap;
        }
        return Collections.EMPTY_MAP;
    }

    private static Object createApiSuccessExample(Class<?> clazz, Type genericType) throws InstantiationException, IllegalAccessException {
        List<Class<?>> nestedGenericClassList = getTypeListFromNestedGenericType(genericType);
        Map<Integer, Map<String, Class<?>>> nestedGenericTypeMap = getNestedGenericTypeMap(nestedGenericClassList);
        return createApiSuccessExample(clazz, nestedGenericTypeMap, 0);
    }

    private static Object createApiSuccessExample(Class<?> clazz, Map<Integer, Map<String, Class<?>>> map, Integer level) throws IllegalAccessException, InstantiationException {
        //获取方法返回值泛型层数 和 对应类型

        //基本类型、其包装类、String（用默认值处理）
        //这些的无成员属性集合集合 和 多维数组 用空数组处理
        //递归终止条件

        //自有泛型实参的属性，递归

        //需要附属类的泛型实参的属性，获取到对应泛型参数 递归
        //传入泛型参数level，泛型形参

        if (Set.class.isAssignableFrom(clazz)) {
            Map<String, Class<?>> stringClassMap = map.get(level);
            Class<?> componentClass = stringClassMap.get("E");
            Object component = createApiSuccessExample(componentClass, map, level + 1);
            Set<Object> set = new HashSet<>(1);
            set.add(component);
            return set;
        } else if (List.class.isAssignableFrom(clazz) || Collection.class.isAssignableFrom(clazz)) {
            Map<String, Class<?>> stringClassMap = map.get(level);
            Class<?> componentClass = stringClassMap.get("E");
            Object component = createApiSuccessExample(componentClass, map, level + 1);
            List<Object> list = new LinkedList<>();
            list.add(component);
            return list;
        } else if (Map.class.isAssignableFrom(clazz)) {
            return Collections.EMPTY_MAP;
        } else if (clazz.isArray()) {
            return Collections.EMPTY_LIST;
        }

        Object instance = getPrimitiveOrWrapperOrStringDefaultInstance(clazz);
        if (instance != null) {
            return instance;
        }

        Object classInstance;
        try {
            classInstance = clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            log.info("Can't create an instance of " + clazz.getCanonicalName() + " because it doesn't have the default constructor");
            return null;
        }

        //遍历
        List<Field> fieldList = FieldUtils.getAllFieldsList(clazz);
        if (fieldList.isEmpty()) {
            return classInstance;
        }

        Map<String, Class<?>> stringClassMap = map.get(level);

        boolean useClazzGeneric = false;
        if (stringClassMap != null && !stringClassMap.isEmpty()) {
            useClazzGeneric = true;
        }
        for (Field field : fieldList) {
            if (field.getModifiers() > Modifier.PROTECTED) {
                continue;
            }

            field.setAccessible(true);

            Class<?> fieldType = field.getType();
            if (!fieldType.isArray()) {
                Object fieldInstance;
                fieldInstance = getPrimitiveOrWrapperOrStringDefaultInstance(fieldType);
                if (fieldInstance == null) {
                    Type genericType = field.getGenericType();
                    String genericTypeName = genericType.getTypeName();

                    if (useClazzGeneric && stringClassMap.get(genericTypeName) != null) {
                        Class<?> genericClass = stringClassMap.get(genericTypeName);
                        fieldInstance = createApiSuccessExample(genericClass, map, level + 1);
                    } else {
                        if (useClazzGeneric) {
                            Matcher matcher = GENERIC_CODE_PATTERN.matcher(genericTypeName);
                            if (matcher.find()) {
                                //TODO 没有做泛型层次的兼容，有损健壮性
                                String group = matcher.group();
                                if (group.indexOf("<") != group.lastIndexOf("<")) {
                                    throw new IllegalArgumentException("暂时不支持域泛型嵌套 " + clazz.getName() + "." + genericTypeName);
                                }
                                String replaced = group.replaceAll("[<>,]", "");
                                String[] genericCodeArr = replaced.split("");
                                int length = genericCodeArr.length;
                                Class<?>[] actualTypes = Arrays.stream(genericCodeArr).map(stringClassMap::get).toArray((i) -> new Class<?>[length]);
                                genericType = ParameterizedTypeImpl.make(fieldType, actualTypes, clazz);
                            }
                        }

                        //没有使用泛型或使用泛型实参的域
                        List<Class<?>> nestedGenericClassList = getTypeListFromNestedGenericType(genericType);
                        //...另一种递归策略
                        Map<Integer, Map<String, Class<?>>> nestedGenericTypeMap = getNestedGenericTypeMap(nestedGenericClassList);
                        fieldInstance = createApiSuccessExample(fieldType, nestedGenericTypeMap, 0);
                    }
                }

                field.set(classInstance, fieldInstance);
            }
        }

        return classInstance;
    }

    private static Object getPrimitiveOrWrapperOrStringDefaultInstance(Class<?> fieldType) {
        if (Integer.class.isAssignableFrom(fieldType) || int.class.isAssignableFrom(fieldType)) {
            return 0;
        } else if (Boolean.class.isAssignableFrom(fieldType) || boolean.class.isAssignableFrom(fieldType)) {
            return false;
        } else if (Long.class.isAssignableFrom(fieldType) || long.class.isAssignableFrom(fieldType)) {
            return 0L;
        } else if (Double.class.isAssignableFrom(fieldType) || double.class.isAssignableFrom(fieldType)) {
            return 0D;
        } else if (Byte.class.isAssignableFrom(fieldType) || byte.class.isAssignableFrom(fieldType)) {
            return (byte) 0;
        } else if (Float.class.isAssignableFrom(fieldType) || float.class.isAssignableFrom(fieldType)) {
            return 0F;
        } else if (Short.class.isAssignableFrom(fieldType) || short.class.isAssignableFrom(fieldType)) {
            return (short) 0;
        } else if (Character.class.isAssignableFrom(fieldType) || char.class.isAssignableFrom(fieldType)) {
            return 'a';
        } else if (CharSequence.class.isAssignableFrom(fieldType)) {
            return EMPTY;
        }
        return null;
    }

}
