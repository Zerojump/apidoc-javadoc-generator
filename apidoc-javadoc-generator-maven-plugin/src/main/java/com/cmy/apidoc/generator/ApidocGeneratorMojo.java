package com.cmy.apidoc.generator;

import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.cmy.apidoc.generator.ApiDocBuilder.BRACE_CLOSE;
import static com.cmy.apidoc.generator.ApiDocBuilder.BRACE_OPEN;
import static com.cmy.apidoc.generator.ApiDocBuilder.NEW_LINE;
import static com.cmy.apidoc.generator.ApiDocBuilder.SPACE_ONE;
import static com.cmy.apidoc.generator.ApiDocBuilder.writeFile;


/**
 * <p>@author chenmingyi
 * <p>@version 1.0
 * <p>Date: 2017/7/1
 * <p>
 * To change this template use File | Settings | File and Code Templates | Includes .
 */
@Mojo(name = "generate")
public class ApidocGeneratorMojo extends AbstractMojo {

    private static final Logger log = Logger.getLogger("ApidocGeneratorMojo");

    public static final String PACKAGE = "package";
    public static final String SEMICOLON = ";";

    @Parameter(defaultValue = "${project.basedir}", required = true, readonly = true)
    private File baseDir;

    @Parameter(defaultValue = "${project.build.sourceDirectory}", required = true, readonly = true)
    private File sourceDirectory;

    @Parameter(defaultValue = "target/classes")
    private String classPathDir;

    @Parameter(defaultValue = "ApiDoc")
    private String apiDocFileName;

    @Parameter(defaultValue = "${project.build.sourceDirectory}")
    private String apiDocDir;

    @Parameter(defaultValue = "com")
    private String apiDocPackage;

    @Parameter(required=true)
    private String[] apiSources;

    @Parameter(defaultValue = "0.0.1")
    private String apiVersion;

    @Parameter()
    private String[] extClassPathDirs;

    @Parameter
    private String[] apiErrors;

    @Parameter
    private String gsonFactory;

    @Parameter
    private String gsonFactoryMethod;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        if (StringUtils.isEmpty(apiVersion)) {
            log.info("apiVersion can't be empty");
            throw new MojoExecutionException("apiVersion can't be empty");
        }
        if (StringUtils.isEmpty(apiDocFileName)) {
            log.info("apiDocFileName can't be empty");
            throw new MojoExecutionException("apiDocFileName can't be empty");
        }
        if (StringUtils.isEmpty(apiDocDir)) {
            log.info("apiDocDir can't be empty");
            throw new MojoExecutionException("apiDocDir can't be empty");
        }

        List<URL> urlList = null;
        if (extClassPathDirs != null) {
            urlList = new ArrayList<>(extClassPathDirs.length + 1);
            for (String pathDir : extClassPathDirs) {
                try {
                    //URL url = new URL("file:///" + pathDir);
                    URL url = new File(pathDir).toURI().toURL();
                    urlList.add(url);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    log.log(Level.SEVERE, "Can't load class path:" + pathDir);
                    throw new MojoExecutionException("Load class path fail:" + pathDir, e);
                }
            }
        }

        ClassLoader parent = Thread.currentThread().getContextClassLoader();
        URL[] urls;
        try {
            File classpathDir = new File(baseDir, classPathDir);
            URL url = classpathDir.toURI().toURL();

            if (urlList != null) {
                urlList.add(url);
                urls = urlList.toArray(new URL[urlList.size()]);
            } else {
                urls = new URL[]{url};
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
            log.log(Level.SEVERE, "Can't load class path:" + classPathDir);
            throw new MojoExecutionException("Load class path fail:" + classPathDir, e);
        }

        log.info("use classpath:" + Arrays.toString(urls));
        ClassLoader loader = new URLClassLoader(urls, parent);

        ApiDocBuilder.VERSION = apiVersion;

        Gson gson = null;
        Class<?> gsonFactoryClass = null;
        if (!StringUtils.isEmpty(gsonFactory) && !StringUtils.isEmpty(gsonFactoryMethod)) {
            gsonFactoryClass = loadClass(loader, gsonFactory);
        }
        if (gsonFactoryClass != null) {
            Method gsonFactoryClassMethod;
            try {
                gsonFactoryClassMethod = gsonFactoryClass.getMethod(gsonFactoryMethod);
                gson = (Gson) gsonFactoryClassMethod.invoke(gsonFactoryClass.newInstance());
            } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
                e.printStackTrace();
                log.log(Level.SEVERE, "Can't new gson by invoking " + gsonFactory + "." + gsonFactoryMethod);
                log.log(Level.SEVERE, "use build-in gson");
            }
        }

        ApiDocBuilder apiDocBuilder = new ApiDocBuilder();
        if (gson == null) {
            apiDocBuilder.init4Gson();
        } else {
            apiDocBuilder.setGson(gson);
        }

        StringBuilder apiErrorContent = new StringBuilder();
        if (apiErrors != null && apiErrors.length != 0) {
            for (String apiError : apiErrors) {
                Class<?> apiErrorClass = loadClass(loader, apiError);
                try {
                    StringBuilder errorDefineBasic = apiDocBuilder.buildApiErrorDefineBasic(apiErrorClass);
                    apiErrorContent.append(errorDefineBasic);
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                    log.log(Level.SEVERE, "can't create @apiDefine from " + apiError, e);
                    throw new MojoExecutionException("can't create @apiDefine from " + apiError, e);
                }
            }
        }


        StringBuilder apiDocContents = new StringBuilder();
        for (String apiSource : apiSources) {
            Class<?> apiSourceClass;
            apiSourceClass = loadClass(loader, apiSource);

            StringBuilder apiDocContent;

            try {
                apiDocContent = apiDocBuilder.createApiDocContentFromClass(apiSourceClass);
            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
                log.log(Level.SEVERE, "can't create apidoc from " + apiSource, e);
                throw new MojoExecutionException("can't create apidoc from " + apiSource, e);
            }
            apiDocContents.append(apiDocContent);
        }

        StringBuilder apiDocSB = new StringBuilder();
        apiDocSB.append(PACKAGE).append(SPACE_ONE).append(apiDocPackage).append(SEMICOLON).append(NEW_LINE).append(NEW_LINE);
        apiDocSB.append("public class ").append(apiDocFileName).append(SPACE_ONE).append(BRACE_OPEN).append(NEW_LINE);

        apiDocSB.append(apiErrorContent);
        apiDocSB.append(apiDocContents);

        apiDocSB.append(BRACE_CLOSE);
        File dir = new File(apiDocDir, apiDocPackage.replaceAll("\\.", "/"));

        if (dir.isFile()) {
            log.log(Level.SEVERE, dir.getAbsolutePath() + " is not a directory");
            throw new MojoExecutionException(dir.getAbsolutePath() + " is not a directory");
        }

        if (!dir.exists() && !dir.mkdirs()) {
            log.log(Level.SEVERE, "mkdir " + dir.getAbsolutePath() + " fail");
            throw new MojoExecutionException("mkdir " + dir.getAbsolutePath() + " fail");
        }

        File file = new File(dir, apiDocFileName + ".java");

        try {
            writeFile(file, apiDocSB.toString(), "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
            log.log(Level.SEVERE, "write file fail:" + apiDocFileName + ".java", e);
            throw new MojoExecutionException("write file fail:" + apiDocFileName + ".java", e);
        }

        log.info("create file:" + file.getAbsolutePath());
    }

    private static Class<?> loadClass(ClassLoader loader, String classStr) throws MojoExecutionException {
        Class<?> apiSourceClass;
        try {
            apiSourceClass = loader.loadClass(classStr);

        } catch (ClassNotFoundException e) {
            try {
                apiSourceClass = loader.getParent().loadClass(classStr);
            } catch (ClassNotFoundException e1) {
                e1.printStackTrace();
                log.log(Level.SEVERE, "can't load class " + classStr, e1);
                throw new MojoExecutionException("load class fail:" + classStr, e1);
            }
        }
        return apiSourceClass;
    }

    public File getBaseDir() {
        return baseDir;
    }

    public void setBaseDir(File baseDir) {
        this.baseDir = baseDir;
    }

    public File getSourceDirectory() {
        return sourceDirectory;
    }

    public void setSourceDirectory(File sourceDirectory) {
        this.sourceDirectory = sourceDirectory;
    }

    public String getClassPathDir() {
        return classPathDir;
    }

    public void setClassPathDir(String classPathDir) {
        this.classPathDir = classPathDir;
    }

    public String getApiDocFileName() {
        return apiDocFileName;
    }

    public void setApiDocFileName(String apiDocFileName) {
        this.apiDocFileName = apiDocFileName;
    }

    public String getApiDocDir() {
        return apiDocDir;
    }

    public void setApiDocDir(String apiDocDir) {
        this.apiDocDir = apiDocDir;
    }

    public String getApiDocPackage() {
        return apiDocPackage;
    }

    public void setApiDocPackage(String apiDocPackage) {
        this.apiDocPackage = apiDocPackage;
    }

    public String[] getApiSources() {
        return apiSources;
    }

    public void setApiSources(String[] apiSources) {
        this.apiSources = apiSources;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public String[] getExtClassPathDirs() {
        return extClassPathDirs;
    }

    public void setExtClassPathDirs(String[] extClassPathDirs) {
        this.extClassPathDirs = extClassPathDirs;
    }

    public String[] getApiErrors() {
        return apiErrors;
    }

    public void setApiErrors(String[] apiErrors) {
        this.apiErrors = apiErrors;
    }

    public String getGsonFactory() {
        return gsonFactory;
    }

    public void setGsonFactory(String gsonFactory) {
        this.gsonFactory = gsonFactory;
    }

    public String getGsonFactoryMethod() {
        return gsonFactoryMethod;
    }

    public void setGsonFactoryMethod(String gsonFactoryMethod) {
        this.gsonFactoryMethod = gsonFactoryMethod;
    }
}
