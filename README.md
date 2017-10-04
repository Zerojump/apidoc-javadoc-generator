# apidoc-javadoc-generator

## 一、项目介绍

　　[apidoc](http://apidocjs.com/)是用node.js开发的可以根据api的注释文档生成相应的RESTful风格的api文档的工具，而且支持多种开发api的语言。
使用apidoc时生成文档时需要 3 步（默认使用者已经安装了node.js、apidoc，如果没有可以根据这个来完成：[node.js](https://nodejs.org/en/)，v6版本的node.js安装后会把npm也装好， [apidoc install](http://apidocjs.com/#install) ）
1.  完成api的注释文档；
2.  完成[apidoc.json](http://apidocjs.com/#configuration)（这一步可省略）；
3.  使用命令```apidoc -i doc_dir -o output_dir ``` [run](http://apidocjs.com/#run)，在哪个目录下执行这个命令，[apidoc.json](http://apidocjs.com/#configuration)放在哪个目录就行，就可生成漂亮美观的api文档了。

    
　　这个项目就是实现的就是可以根据java的注解和maven插件配置来生成apidoc注释的maven插件，即完成上面的第一步，也是最烦人的一步，
而且只支持使用spring mvc框架来开发的api。

## 二、使用介绍
　　1、把项目下载下来，然后使用maven 命令 install 到本地仓库（也可deploy到maven的私服，供他人使用）
```
D:\apidoc-javadoc-generator>mvn install -Dmaven.test.skip=true
```
　　2、在自己的项目中引入依赖和插件
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

　　3、在项目中使用特定注解
#### 插件内置注解
注解  | 描述
------|-----
ApiParam | 生成apidoc 的[@ApiParam](http://apidocjs.com/#param-api-param)或[@ApiSuccess](http://apidocjs.com/#param-api-success)，用在方法入参和VO类的属性上
ApiErrorFactoryMethod | 参考apidoc 的[@ApiDefine](http://apidocjs.com/#param-api-define)的使用，插件apiErrors配置对应的类的方法上使用
ApiIgnore | 用在VO类的属性上，使用该注解的属性不会生成[@ApiParam](http://apidocjs.com/#param-api-param)
ApiUse | 生成apidoc的[@ApiUse](http://apidocjs.com/#param-api-use)，用在方法入参和VO类的属性上。 一般搭配ApiErrorFactoryMethod注解使用（ApiUse注解的value属性的元素和ApiErrorFactoryMethod注解的value属性相同）

#### spring内置注解
注解  | 描述
------|-----
RequestMapping | 用在类上，其value属性用来生成[@ApiGroup](http://apidocjs.com/#param-api-group)；用在方法上，name属性用来生成[@ApiName](http://apidocjs.com/#param-api-name)，value和method属性用来生成[@Api](http://apidocjs.com/#param-api)的path和method，
RequestBody | 用来生成[@ApiParamExample](http://apidocjs.com/#param-api-param-example)

另外，此插件会根据使用了RequestMapping注解的方法的方法签名的返回类型生成[@ApiSuccessExample](http://apidocjs.com/#param-api-success-example)，返回类型可以是复杂的，带泛型引用类型，目前最新版本对使用多个泛型参数的类属性（比如类里带有Map）的支持还不是很好，但是对大多数的api设计已经够用了。

　　4、demo
```java
package com.cmy.controller.user;
//省略import

@RestController
@RequestMapping("user")
public class UserController {

    @RequestMapping(name = "修改", value = "role", method = RequestMethod.PUT)
    @ApiUse({"unauthorized","param_error"})
    public List<Integer> get(@ApiParam(name = "roleId", size = "0-", allowedValues = "1,2", defaultValue = "1", desc = "角色id") Integer roleId,
                             @RequestBody Node node) {
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

   public RespWrap(Long code,String desc,T data) {
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
   
    @ApiErrorFactoryMethod("param_error")
    public static <T> RespWrap<T> paramError(T t) {
        return new RespWrap(400,"参数错误",t);
    }
  
    @ApiErrorFactoryMethod("unauthorized")
    public static <T> RespWrap<T> paramError() {
        return new RespWrap(403,"权限不足",null);
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
        gsonBuilder.registerTypeAdapter(Date.class, new CustomDateTypeAdapter());
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
mvn com.cmy:apidoc-javadoc-generator-maven-plugin:0.0.1:generate
```