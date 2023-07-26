package com.heretere.hdl.impl;

import com.heretere.hdl.common.constants.DefaultRepository;
import com.heretere.hdl.common.json.HDLConfig;
import com.heretere.hdl.common.json.Repository;
import com.heretere.hdl.common.json.ResolvedDependency;
import com.heretere.hdl.impl.utils.DependencyUtils;
import lombok.NonNull;
import lombok.SneakyThrows;

import java.net.URLClassLoader;
import java.util.AbstractMap;
import java.util.HashSet;
import java.util.Set;

public class DependencyLoader {

    public static void load(String basePath) {
        new DependencyLoader(basePath).load();
    }



    private static final Set<AbstractMap.SimpleImmutableEntry<String, String>> privateDependencies = new HashSet<>();

    static {
        privateDependencies.add(
                new AbstractMap.SimpleImmutableEntry<>(
                        "com/fasterxml/jackson/core/jackson-databind/2.13.0/jackson-databind-2.13.0.jar",
                        "jackson-databind-2.13.0.jar"
                )
        );
        privateDependencies.add(
                new AbstractMap.SimpleImmutableEntry<>(
                        "com/fasterxml/jackson/core/jackson-core/2.13.0/jackson-core-2.13.0.jar",
                        "jackson-core-2.13.0.jar"
                )
        );
        privateDependencies.add(
                new AbstractMap.SimpleImmutableEntry<>(
                        "com/fasterxml/jackson/core/jackson-annotations/2.13.0/jackson-annotations-2.13.0.jar",
                        "jackson-annotations-2.13.0.jar"
                )
        );
    }

    private final String basePath;
    private final ClassLoader classLoader;

    public DependencyLoader(@NonNull String basePath) {
        this(basePath, DependencyLoader.class.getClassLoader());
    }

    public DependencyLoader(@NonNull String basePath, @NonNull ClassLoader classLoader) {
        if (!(classLoader instanceof URLClassLoader))
            throw new AssertionError("Classloader must be instanceof URLClassLoader.");

        this.basePath = basePath;
        this.classLoader = classLoader;
    }

    @SneakyThrows
    public boolean load() {
        this.loadPrivateDependencies();

        HDLConfig config = new com.fasterxml.jackson.databind.ObjectMapper().readValue(
                this.classLoader.getResourceAsStream("hdl_dependencies.json"),
                HDLConfig.class
        );

        for (ResolvedDependency dependency : config.getDependencies()) {
            this.downloadDependency(
                    dependency,
                    config.getRepositories().get(dependency.getRepositoryId())
            );
        }

        config.getDependencies().forEach(this::loadDependency);

        return true;
    }



    private void downloadDependency(@NonNull ResolvedDependency dependency,
                                    @NonNull Repository repository) {
        DependencyUtils.downloadJar(
                this.basePath,
                repository,
                dependency
        );
    }

    private void loadDependency(@NonNull ResolvedDependency dependency) {
        DependencyUtils.loadJar(
                this.basePath,
                dependency,
                this.classLoader
        );
    }

    private void loadPrivateDependencies() {
        for (AbstractMap.SimpleImmutableEntry<String, String> depend : privateDependencies) {
            DependencyUtils.downloadJar(
                    this.basePath,
                    DefaultRepository.MAVEN_CENTRAL.getRepository(),
                    new ResolvedDependency(
                            depend.getKey(),
                            DefaultRepository.MAVEN_CENTRAL.getId(),
                            depend.getValue()
                    )
            );
        }
        for (AbstractMap.SimpleImmutableEntry<String, String> dependency : privateDependencies) {
            this.loadDependency(new ResolvedDependency(
                    dependency.getKey(),
                    DefaultRepository.MAVEN_CENTRAL.getId(),
                    dependency.getValue()
            ));
        }
    }
}
