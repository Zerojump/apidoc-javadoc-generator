package com.cmy.apidoc.generator;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.cmy.apidoc.generator.ApiDocBuilder.BRACE_CLOSE;
import static com.cmy.apidoc.generator.ApiDocBuilder.BRACE_OPEN;
import static com.cmy.apidoc.generator.ApiDocBuilder.NEW_LINE;
import static com.cmy.apidoc.generator.ApiDocBuilder.SPACE_ONE;
import static com.cmy.apidoc.generator.ApiDocBuilder.createApiDocContentFromClass;
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

    Logger log = Logger.getLogger("ApidocGeneratorMojo");

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

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        
        ClassLoader parent = Thread.currentThread().getContextClassLoader();
        URL[] urls = new URL[1];
        try {
            //urls[0] = new URL("file:///"+baseDir+"");
            File classpathDir = new File(baseDir, classPathDir);

            urls[0] = classpathDir.toURI().toURL();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        ClassLoader loader = new URLClassLoader(urls, parent);

        ApiDocBuilder.VERSION = apiVersion;

        StringBuilder apiDocSB = new StringBuilder();
        apiDocSB.append(PACKAGE).append(SPACE_ONE).append(apiDocPackage).append(SEMICOLON).append(NEW_LINE);
        apiDocSB.append("public class ").append(apiDocFileName).append(SPACE_ONE).append(BRACE_OPEN).append(NEW_LINE);
        for (String apiSource : apiSources) {
            Class<?> apiSourceClass;
            try {
                apiSourceClass = loader.loadClass(apiSource);
            } catch (ClassNotFoundException e) {
                log.log(Level.WARNING, "can't load class " + apiSource + " in " + classPathDir, e);
                try {
                    apiSourceClass = parent.loadClass(apiSource);
                } catch (ClassNotFoundException e1) {
                    e1.printStackTrace();
                    log.log(Level.SEVERE, "can't load class " + apiSource + " in " + classPathDir, e1);
                    throw new MojoExecutionException("load class fail:" + apiSource, e);
                }
            }

            StringBuilder apiDocContent;
            try {
                apiDocContent = createApiDocContentFromClass(apiSourceClass);
            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
                log.log(Level.SEVERE, "can't create apidoc from " + apiSource, e);
                throw new MojoExecutionException("can't create apidoc from " + apiSource, e);
            }
            apiDocSB.append(apiDocContent);
        }

        apiDocSB.append(BRACE_CLOSE);
        File dir = new File(apiDocDir, apiDocPackage.replaceAll("\\.", "/"));
        log.info("dir.getAbsolutePath() = " + dir.getAbsolutePath());

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
            writeFile(file,apiDocSB.toString(),"UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
            log.log(Level.SEVERE, "write file fail:" + apiDocFileName + ".java", e);
            throw new MojoExecutionException("write file fail:" + apiDocFileName + ".java", e);
        }
    }
}
