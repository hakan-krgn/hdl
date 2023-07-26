package com.heretere.hdl.impl.utils;

import com.heretere.hdl.common.json.Repository;
import com.heretere.hdl.common.json.ResolvedDependency;
import sun.misc.Unsafe;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Collection;

public class DependencyUtils {

    public static void downloadJar(String base,
                                   Repository repository,
                                   ResolvedDependency dependency) {
        File baseFile = new File(base);
        if (!baseFile.exists()) baseFile.mkdirs();

        File saveFile = new File(base, dependency.getFileName());
        if (saveFile.exists()) return;

        for (String url : repository.getUrls()) {
            try {
                URL website = new URL(url + dependency.getRelativeUrl());
                InputStream in = website.openStream();

                Files.copy(in, saveFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                return;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void loadJar(String base,
                               ResolvedDependency dependency,
                               ClassLoader classLoader) {
        try {
            URL url = new File(base, dependency.getFileName()).toURI().toURL();
            Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
            method.setAccessible(true);
            method.invoke(classLoader, url);
            method.setAccessible(false);
        } catch (Exception e) {
            try {
                Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
                unsafeField.setAccessible(true);
                Unsafe unsafe = (sun.misc.Unsafe) unsafeField.get(null);

                Object ucp = fetchField(unsafe, URLClassLoader.class, classLoader, "ucp");
                Collection<URL> unopenedURLs = (Collection<URL>) fetchField(unsafe, ucp.getClass(), ucp, "unopenedUrls");
                Collection<URL> pathURLs = (Collection<URL>) fetchField(unsafe, ucp.getClass(), ucp, "path");

                unopenedURLs.add(new File(base, dependency.getFileName()).toURI().toURL());
                pathURLs.add(new File(base, dependency.getFileName()).toURI().toURL());
            } catch (Exception ignored) {

            }
        }
    }

    private static Object fetchField(Unsafe unsafe, final Class<?> clazz, final Object object, final String name)
            throws NoSuchFieldException {
        Field field = clazz.getDeclaredField(name);
        long offset = unsafe.objectFieldOffset(field);
        return unsafe.getObject(object, offset);
    }
}
