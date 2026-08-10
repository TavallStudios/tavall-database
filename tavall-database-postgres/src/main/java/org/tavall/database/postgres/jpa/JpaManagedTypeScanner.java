package org.tavall.database.postgres.jpa;

import jakarta.persistence.Converter;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import jakarta.persistence.MappedSuperclass;
import org.tavall.database.postgres.exception.PostgresDatabaseException;

import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

final class JpaManagedTypeScanner {

    List<Class<?>> scan(ClassLoader classLoader, List<String> packageNames) {
        ClassLoader effectiveClassLoader = classLoader == null
                ? JpaManagedTypeScanner.class.getClassLoader()
                : classLoader;
        Set<String> classNames = new LinkedHashSet<>();
        for (String packageName : packageNames == null ? List.<String>of() : packageNames) {
            String normalizedPackage = normalizePackage(packageName);
            if (normalizedPackage.isEmpty()) {
                continue;
            }
            collectFromResources(effectiveClassLoader, normalizedPackage, classNames);
            collectFromClassLoaderUrls(effectiveClassLoader, normalizedPackage, classNames);
        }
        List<Class<?>> managedTypes = new ArrayList<>();
        classNames.stream().sorted().forEach(className -> {
            try {
                Class<?> candidate = Class.forName(className, false, effectiveClassLoader);
                if (isManagedType(candidate)) {
                    managedTypes.add(candidate);
                }
            } catch (ClassNotFoundException | LinkageError exception) {
                throw new PostgresDatabaseException(
                        "Unable to load configured JPA type " + className,
                        exception
                );
            }
        });
        return List.copyOf(managedTypes);
    }

    private void collectFromResources(
            ClassLoader classLoader,
            String packageName,
            Set<String> classNames
    ) {
        String packagePath = packageName.replace('.', '/');
        try {
            Enumeration<URL> resources = classLoader.getResources(packagePath);
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                switch (resource.getProtocol()) {
                    case "file" -> collectDirectory(
                            Path.of(resource.toURI()),
                            packageName,
                            classNames
                    );
                    case "jar" -> collectJar(
                            ((JarURLConnection) resource.openConnection()).getJarFile(),
                            packagePath,
                            classNames
                    );
                    default -> {
                        // Application and plugin class loaders generally expose file or jar URLs.
                    }
                }
            }
        } catch (IOException | URISyntaxException exception) {
            throw new PostgresDatabaseException(
                    "Unable to scan JPA package " + packageName,
                    exception
            );
        }
    }

    private void collectFromClassLoaderUrls(
            ClassLoader classLoader,
            String packageName,
            Set<String> classNames
    ) {
        if (!(classLoader instanceof URLClassLoader urlClassLoader)) {
            return;
        }
        String packagePath = packageName.replace('.', '/');
        for (URL url : urlClassLoader.getURLs()) {
            if (!"file".equals(url.getProtocol())) {
                continue;
            }
            try {
                Path path = Path.of(url.toURI());
                if (Files.isDirectory(path)) {
                    Path packageDirectory = path.resolve(packagePath);
                    if (Files.isDirectory(packageDirectory)) {
                        collectDirectory(packageDirectory, packageName, classNames);
                    }
                } else if (path.getFileName().toString().endsWith(".jar")) {
                    try (JarFile jarFile = new JarFile(path.toFile())) {
                        collectJar(jarFile, packagePath, classNames);
                    }
                }
            } catch (IOException | URISyntaxException exception) {
                throw new PostgresDatabaseException(
                        "Unable to scan JPA class path URL " + url,
                        exception
                );
            }
        }
    }

    private void collectDirectory(
            Path packageDirectory,
            String packageName,
            Set<String> classNames
    ) {
        try (var files = Files.walk(packageDirectory)) {
            files.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".class"))
                    .map(packageDirectory::relativize)
                    .map(Path::toString)
                    .map(name -> toClassName(packageName, name))
                    .filter(this::isLoadableClassName)
                    .forEach(classNames::add);
        } catch (IOException exception) {
            throw new PostgresDatabaseException(
                    "Unable to scan JPA package directory " + packageDirectory,
                    exception
            );
        }
    }

    private void collectJar(
            JarFile jarFile,
            String packagePath,
            Set<String> classNames
    ) {
        String prefix = packagePath.endsWith("/") ? packagePath : packagePath + "/";
        Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            String name = entry.getName();
            if (!entry.isDirectory()
                    && name.startsWith(prefix)
                    && name.endsWith(".class")) {
                String className = name.substring(0, name.length() - ".class".length())
                        .replace('/', '.');
                if (isLoadableClassName(className)) {
                    classNames.add(className);
                }
            }
        }
    }

    private String toClassName(String packageName, String relativeClassFile) {
        String relativeName = relativeClassFile
                .replace('\\', '.')
                .replace('/', '.');
        return packageName + "." + relativeName.substring(
                0,
                relativeName.length() - ".class".length()
        );
    }

    private boolean isLoadableClassName(String className) {
        return !className.endsWith("module-info")
                && !className.endsWith("package-info");
    }

    private boolean isManagedType(Class<?> candidate) {
        return candidate.isAnnotationPresent(Entity.class)
                || candidate.isAnnotationPresent(Embeddable.class)
                || candidate.isAnnotationPresent(MappedSuperclass.class)
                || candidate.isAnnotationPresent(Converter.class);
    }

    private String normalizePackage(String packageName) {
        if (packageName == null) {
            return "";
        }
        return packageName.trim();
    }
}
