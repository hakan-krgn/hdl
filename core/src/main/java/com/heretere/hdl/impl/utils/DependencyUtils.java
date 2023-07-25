package com.heretere.hdl.impl.utils;

import com.heretere.hdl.common.json.Repository;
import com.heretere.hdl.common.json.ResolvedDependency;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public final class DependencyUtils {

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
            e.printStackTrace();
        }
    }
}