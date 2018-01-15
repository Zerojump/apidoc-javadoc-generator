# apidoc-javadoc-generator

## 一、项目介绍

　　[apidoc](http://apidocjs.com/)是用node.js开发的可以根据api的注释文档生成相应的RESTful风格的api文档的工具，而且支持多种开发api的语言。
使用apidoc时生成文档时需要 3 步（默认使用者已经安装了node.js、apidoc，如果没有可以根据这个来完成：[node.js](https://nodejs.org/en/)，v6版本的node.js安装后会把npm也装好， [apidoc install](http://apidocjs.com/#install) ）
1.  完成api的注释文档；
2.  完成[apidoc.json](http://apidocjs.com/#configuration)（这一步可省略）；
3.  使用命令 [apidoc -i doc_dir -o output_dir](http://apidocjs.com/#run)，在哪个目录下执行这个命令，[apidoc.json](http://apidocjs.com/#configuration)放在哪个目录就行，就可生成漂亮美观的api文档了。

    
　　这个项目就是实现的就是可以根据java的注解和maven插件配置来生成apidoc注释的maven插件，即完成上面的第一步，也是最烦人的一步，
而且只支持使用spring mvc框架来开发的api。

## 二、使用介绍
####1、把项目down下来，然后maven install到本地maven仓库（也可deploy到maven私服，供他人使用）
```
D:\apidoc-javadoc-generator>mvn install -Dmaven.test.skip=true
```
####2、在自己的项目中引入依赖和插件
```xml
<dependency>
    <groupId>com.cmy</groupId>
    <artifactId>apidoc-javadoc-generator-core</artifactId>
    <version>0.0.1</version>
</dependency>
```
```xml
<build>
<plugins>
    <plugin>
        <groupId>com.cmy</groupId>
        <artifactId>apidoc-javadoc-generator-maven-plugin</artifactId>
        <version>0.0.1</version>
        <dependencies>
            <!-- 插件依赖于spring mvc，如果自己项目里引入了别的依赖也可以在这里添加 -->
            <dependency>
                <groupId>org.springframework</groupId>
                <artifactId>spring-web</artifactId>
                <version>${spring.version}</version>
            </dependency>

            <dependency>
                <groupId>org.springframework</groupId>
                <artifactId>spring-core</artifactId>
                <version>${spring.version}</version>
            </dependency>
        </dependencies>
        <configuration>
            <apiDocFileName>ApiDocContent</apiDocFileName>
            <apiDocDir>src\test\java</apiDocDir>
            <apiSources>
                <apiSource>com.cmy.controller.XxController</apiSource>
            </apiSources>
        </configuration>
    </plugin>
</plugins>
</build>
```
### 插件配置:
配置项 | 必须 | 默认值 | 描述
------|-----|--------|-----
classPathDir | 否 | target/classes | 自己项目的源码编译后字节码文件路径
apiDocFileName | 否 | ApiDoc | 生成的注释文档的文件名，默认生成ApiDoc.java 文件
apiDocDir | 否 | ${project.build.sourceDirectory} | 自己项目的源码路径，源码路径相对于项目路径的相对路径
apiDocPackage | 否 | com | 生成的注释文档的文件的包路径
apiSources | 是 | | 使用了spring @RequestMapping 注解的类，一般是项目的controller，可以配置多个```。 <apiSources><apiSource>...</apiSource><apiSource>...</apiSource></apiSources>```
apiVersion | 否 | 0.0.1 | api的版本，对应于apidoc的[@ApiVersion](http://apidocjs.com/#param-api-version)
extClassPathDirs | 否 | | 自己其他项目的源码编译路径，当自己api所在的项目依赖于自己的其他项目（比如一个maven module依赖于其他的maven module），可以用这个添加```。 <extClassPathDirs><extClassPathDir>...</extClassPathDir><extClassPathDir>...</extClassPathDir></extClassPathDirs>```
apiErrors | 否 | | 指定api非正常返回时的返回内容，对应于apidoc的[@ApiDefine](http://apidocjs.com/#param-api-define)只不过是专门做[@apiError](http://apidocjs.com/#param-api-error)和[@apiErrorExample](http://apidocjs.com/#param-api-error-example)定义的。 下面有详细介绍，用法同apiDocDir
gsonFactory | 否 | | 生成插件使用的gson 实例的工厂类
gsonFactoryMethod | 否 | | 生成插件使用的gson 实例的工厂类的工厂方法，即gsonFactory对应的类的生成gson 实例的方法，该方法必须是无参的

####3、在项目中使用特定注解
#### 插件内置注解
注解  | 描述
------|-----
ApiParam | 生成apidoc 的[@ApiParam](http://apidocjs.com/#param-api-param)或[@ApiSuccess](http://apidocjs.com/#param-api-success)，用在方法入参和VO类的属性上
ApiErrorFactoryMethod | 参考apidoc 的[@ApiDefine](http://apidocjs.com/#param-api-define)的使用，插件apiErrors配置对应的类的方法上使用
ApiIgnore | 用在VO类的属性上，使用该注解的属性不会生成[@ApiParam](http://apidocjs.com/#param-api-param)
ApiUse | 生成apidoc的[@ApiUse](http://apidocjs.com/#param-api-use)，用在方法入参和VO类的属性上。 一般搭配ApiErrorFactoryMethod注解使用（ApiUse注解的value属性的元素和ApiErrorFactoryMethod注解的方法名相同）

#### spring内置注解
注解  | 描述
------|-----
RequestMapping | 用在类上，其value属性用来生成[@ApiGroup](http://apidocjs.com/#param-api-group)；用在方法上，name属性用来生成[@ApiName](http://apidocjs.com/#param-api-name)，value和method属性用来生成[@Api](http://apidocjs.com/#param-api)的path和method，
RequestBody | 用来生成[@ApiParamExample](http://apidocjs.com/#param-api-param-example)

####4、使用注意
此插件会根据使用了RequestMapping注解的方法的方法签名的返回类型生成[@ApiSuccessExample](http://apidocjs.com/#param-api-success-example)，返回类型可以是复杂的，带泛型引用类型，目前最新版本对使用多个泛型参数的类属性（比如类里带有Map）的支持还不是很好，但是对大多数的api设计已经够用了。
同一个类的使用RequestMapping注解的方法如果同名的话会被覆盖。如果生成的[@ApiParamExample](http://apidocjs.com/#param-api-param-example)或[@ApiSuccessExample](http://apidocjs.com/#param-api-success-example)在注释文档里出现null，可能是某个类没有默认构造器，插件执行的控制台输出会提示。

####5、demo
```java
package com.cmy.controller.user;
//省略import

@RestController
@RequestMapping("user")
public class UserController {

    @RequestMapping(name = "修改", value = "role", method = RequestMethod.PUT)
    @ApiUse({"unauthorized","paramError"})
    public List<Integer> get(@ApiParam(name = "roleId", size = "0-", allowedValues = "1,2", defaultValue = "1", desc = "角色id") Integer roleId,
                             @RequestBody Node node) {
        return null;
    }

    @RequestMapping(name = "文件上传", value = "", method = RequestMethod.POST)
    public RespWrap<List<String>> get(@ApiParam(name = "file", desc = "上传的文件") MultipartFile file) {
        return null;
    }
}
```
```java
public class Node {
    @ApiParam(desc = "节点id")
    private Long id;
    
    @ApiParam(desc = "节点名称")
    private String name;
}
```
```java
public class RespWrap<T> {
    @ApiParam(desc = "返回码")
    private int code;

    @ApiParam(desc = "返回描述")
    private String desc;

    @ApiParam(desc = "返回数据")
    private T data;

    public RespWrap() {
        this(200, "请求成功", null);
    }

    public RespWrap(int code, String desc, T data) {
        this.code = code;
        this.desc = desc;
        this.data = data;
    }
}
```
```java
package com.cmy.factory.resp;
//省略import

public class ErrorRespFactory {
   
    @ApiErrorFactoryMethod(desc = "参数错误")
    public static <T> RespWrap<T> paramError(T t) {
        return new RespWrap<>(400, "参数错误", t);
    }

    @ApiErrorFactoryMethod(desc = "权限不足")
    public static <T> RespWrap<T> unauthorized() {
        return new RespWrap<>(403, "权限不足", null);
    }
}
```
```java
package com.cmy.factory.gson;
//省略import

public class GsonFactory {
   public static Gson gson() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        //使用自定义的Date和json的序列化和反序列化方式
        gsonBuilder.setPrettyPrinting()
                   .registerTypeAdapter(Date.class, new CustomDateTypeAdapter());
        return gsonBuilder.create();
    }
    
    //自定义Date和json的序列化和反序列化方式为通过毫秒时间戳互转
    public static class CustomDateTypeAdapter extends TypeAdapter<Date> {
        @Override
        public void write(JsonWriter out, Date date) throws IOException {
            if (date == null) {
                out.nullValue();
            } else {
                out.value(date.getTime());
            }
        }
 
        @Override
        public Date read(JsonReader in) throws IOException {
            return new Date(in.nextLong());
        }
    }
 }
```
pom.xml文件
```xml
<dependency>
    <groupId>com.cmy</groupId>
    <artifactId>apidoc-javadoc-generator-core</artifactId>
    <version>0.0.1</version>
</dependency>
```
```xml
<build>
<plugins>
    <plugin>
        <groupId>com.cmy</groupId>
        <artifactId>apidoc-javadoc-generator-maven-plugin</artifactId>
        <version>0.0.1</version>
        <dependencies>
            <!-- 插件依赖于spring mvc，如果自己项目里引入了别的依赖也可以在这里添加 -->
            <dependency>
                <groupId>org.springframework</groupId>
                <artifactId>spring-web</artifactId>
                <version>${spring.version}</version>
            </dependency>

            <dependency>
                <groupId>org.springframework</groupId>
                <artifactId>spring-core</artifactId>
                <version>${spring.version}</version>
            </dependency>
        </dependencies>
        <configuration>
            <apiDocDir>src\test\java</apiDocDir>
            <gsonFactory>com.cmy.factory.gson.GsonFactory</gsonFactory>
            <gsonFactoryMethod>gson</gsonFactoryMethod>
            <apiErrors>
                <apiError>com.cmy.factory.resp.ErrorRespFactory</apiError>
            </apiErrors>
            
            <!-- 假如有个同级module 叫common -->
            <extClassPathDirs>
                <extClassPathDir>..\common\target\classes</extClassPathDir>
            </extClassPathDirs>
            
            <apiSources>
                <apiSource>com.cmy.controller.user.UserClass</apiSource>
            </apiSources>
        </configuration>
    </plugin>
</plugins>
</build>
```

执行插件目标生成注释文档
注意：生成前最好编译一下重新生成字节码文件，有时候要把target目录（字节码所在目录）删掉来确保字节码文件是最新编译出来的
```
mvn apidoc-javadoc-generator:generate
```
执行成功，会提示生成的注释文档的路径
```
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 2.347 s
[INFO] Finished at: 2017-10-04T23:00:47+08:00
十月 04, 2017 11:00:47 下午 com.cmy.apidoc.generator.ApidocGeneratorMojo execute
信息: create file:D:\ideaworkspace\study\apidoc\src\test\java\com\ApiDoc.java
```
生成的注释文档大概是这样的
```java
package com;

public class ApiDoc {

/**
 * @apiDefine paramError
 * @apiError (Error-Response) {RespWrap} paramError 参数错误
 * @apiErrorExample {json} paramError
 * HTTP 200
 *{
  "code": 400,
  "desc": "参数错误"
}
 */

/**
 * @apiDefine unauthorized
 * @apiError (Error-Response) {RespWrap} unauthorized 权限不足
 * @apiErrorExample {json} unauthorized
 * HTTP 200
 *{
  "code": 403,
  "desc": "权限不足"
}
 */

/**
 * @api {PUT} /user/role 修改
 * @apiVersion 0.0.1
 * @apiName com.cmy.controller.UserController.get
 * @apiGroup user
 * @apiDescription 修改
 *
 * @apiParam {Integer {0-}=1,2} roleId=1 角色id
 *
 * @apiParam ({json}Node) {Long} id 节点id
 * @apiParam ({json}Node) {String} name 节点名称
 *
 * @apiParamExample {json} Request-Example:
 * {
  "id": 0,
  "name": ""
}
 *
 * @apiSuccess (Integer) {Integer[]} Integer Integer
 *
 * @apiSuccessExample {json} Success-Response:
 * HTTP 200 OK
 * [
  0
]
 *
 * @apiUse unauthorized
 * @apiUse paramError
 */

/**
 * @api {POST} /user 文件上传
 * @apiVersion 0.0.1
 * @apiName com.cmy.controller.UserController.uploadFile
 * @apiGroup user
 * @apiDescription 文件上传
 *
 * @apiParam {MultipartFile} file 上传的文件
 *
 * @apiSuccess (RespWrap) {int} code 返回码
 * @apiSuccess (RespWrap) {String} desc 返回描述
 * @apiSuccess (RespWrap) {Object} data 返回数据
 *
 * @apiSuccess (String) {String[]} String String
 *
 * @apiSuccessExample {json} Success-Response:
 * HTTP 200 OK
 * {
  "code": 200,
  "desc": "请求成功",
  "data": [
    ""
  ]
}
 */
}
```