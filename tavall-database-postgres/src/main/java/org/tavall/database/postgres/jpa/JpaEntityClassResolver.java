package org.tavall.database.postgres.jpa;

import jakarta.persistence.Converter;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import jakarta.persistence.MappedSuperclass;
import org.tavall.database.postgres.IPostgresConfigData;
import org.tavall.database.postgres.exception.PostgresJpaException;

import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

final class JpaEntityClassResolver {

    private JpaEntityClassResolver() {
    }

    static List<Class<?>> resolve(IPostgresConfigData configData) {
        LinkedHashSet<Class<?>> managedClasses = new LinkedHashSet<>();
        for (Class<?> entityClass : configData.getEntityClasses()) {
            addExplicitClass(managedClasses, entityClass);
        }

        ClassLoader classLoader = configData.getEntityClassLoader();
        for (String entityPackage : configData.getEntityPackages()) {
            scanPackage(managedClasses, classLoader, entityPackage);
        }

        if (managedClasses.isEmpty()) {
            throw new PostgresJpaException(
                    "PostgreSQL JPA was configured without any discoverable @Entity, @Embeddable, @MappedSuperclass, or @Converter classes."
            );
        }
        return List.copyOf(managedClasses);
    }

    private static void addExplicitClass(Set<Class<?>> managedClasses, Class<?> entityClass) {
        if (!isManagedClass(entityClass)) {
            throw new PostgresJpaException(
                    "Configured JPA class is not a managed persistence type: " + entityClass.getName()
            );
        }
        managedClasses.add(entityClass);
    }

    private static void scanPackage(
            Set<Class<?>> managedClasses,
            ClassLoader classLoader,
            String entityPackage
    ) {
        String resourcePath = entityPackage.replace('.', '/');
        boolean resourceFound = false;
        try {
            Enumeration<URL> resources = classLoader.getResources(resourcePath);
            while (resources.hasMoreElements()) {
                resourceFound = true;
                scanResource(managedClasses, classLoader, entityPackage, resourcePath, resources.nextElement());
            }
        } catch (IOException exception) {
            throw new PostgresJpaException("Unable to scan JPA package " + entityPackage + '.', exception);
        }

        if (!resourceFound && classLoader instanceof URLClassLoader urlClassLoader) {
            scanClassLoaderUrls(managedClasses, classLoader, entityPackage, resourcePath, urlClassLoader);
        }
    }

    private static void scanResource(
            Set<Class<?>> managedClasses,
            ClassLoader classLoader,
            String entityPackage,
            String resourcePath,
            URL resource
    ) {
        switch (resource.getProtocol()) {
            case "file" -> scanDirectory(managedClasses, classLoader, entityPackage, resource);
            case "jar" -> scanJarConnection(managedClasses, classLoader, resourcePath, resource);
            default -> {
                // Unsupported protocols are ignored so explicit entityClass registration remains a reliable fallback.
            }
        }
    }

    private static void scanDirectory(
            Set<Class<?>> managedClasses,
            ClassLoader classLoader,
            String entityPackage,
            URL resource
    ) {
        try {
            Path root = Path.of(resource.toURI());
            try (Stream<Path> paths = Files.walk(root)) {
                paths.filter(Files::isRegularFile)
                        .filter(path -> path.getFileName().toString().endsWith(".class"))
                        .forEach(path -> {
                            String relativeName = root.relativize(path).toString().replace('\\', '/');
                            String className = entityPackage + '.'
                                    + relativeName.substring(0, relativeName.length() - ".class".length())
                                    .replace('/', '.');
                            addDiscoveredClass(managedClasses, classLoader, className);
                        });
            }
        } catch (IOException | URISyntaxException exception) {
            throw new PostgresJpaException("Unable to scan JPA directory " + resource + '.', exception);
        }
    }

    private static void scanJarConnection(
            Set<Class<?>> managedClasses,
            ClassLoader classLoader,
            String resourcePath,
            URL resource
    ) {
        try {
            JarURLConnection connection = (JarURLConnection) resource.openConnection();
            connection.setUseCaches(false);
            try (JarFile jarFile = connection.getJarFile()) {
                scanJar(managedClasses, classLoader, resourcePath, jarFile);
            }
        } catch (IOException exception) {
            throw new PostgresJpaException("Unable to scan JPA archive " + resource + '.', exception);
        }
    }

    private static void scanClassLoaderUrls(
            Set<Class<?>> managedClasses,
            ClassLoader classLoader,
            String entityPackage,
            String resourcePath,
            URLClassLoader urlClassLoader
    ) {
        for (URL url : urlClassLoader.getURLs()) {
            if (!"file".equals(url.getProtocol())) {
                continue;
            }

            try {
                Path path = Path.of(url.toURI());
                if (Files.isDirectory(path)) {
                    Path packagePath = path.resolve(resourcePath);
                    if (Files.isDirectory(packagePath)) {
                        scanDirectory(managedClasses, classLoader, entityPackage, packagePath.toUri().toURL());
                    }
                } else if (path.getFileName().toString().endsWith(".jar")) {
                    try (JarFile jarFile = new JarFile(path.toFile())) {
                        scanJar(managedClasses, classLoader, resourcePath, jarFile);
                    }
                }
            } catch (IOException | URISyntaxException exception) {
                throw new PostgresJpaException("Unable to scan JPA class-loader URL " + url + '.', exception);
            }
        }
    }

    private static void scanJar(
            Set<Class<?>> managedClasses,
            ClassLoader classLoader,
            String resourcePath,
            JarFile jarFile
    ) {
        String prefix = resourcePath.endsWith("/") ? resourcePath : resourcePath + '/';
        Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            String entryName = entry.getName();
            if (entry.isDirectory() || !entryName.startsWith(prefix) || !entryName.endsWith(".class")) {
                continue;
            }
            String className = entryName.substring(0, entryName.length() - ".class".length()).replace('/', '.');
            addDiscoveredClass(managedClasses, classLoader, className);
        }
    }

    private static void addDiscoveredClass(
            Set<Class<?>> managedClasses,
            ClassLoader classLoader,
            String className
    ) {
        if (className.endsWith("package-info") || className.endsWith("module-info")) {
            return;
        }

        try {
            Class<?> candidate = Class.forName(className, false, classLoader);
            if (isManagedClass(candidate)) {
                managedClasses.add(candidate);
            }
        } catch (ClassNotFoundException | LinkageError exception) {
            throw new PostgresJpaException("Unable to load discovered JPA class " + className + '.', exception);
        }
    }

    private static boolean isManagedClass(Class<?> candidate) {
        return candidate != null
                && (candidate.isAnnotationPresent(Entity.class)
                || candidate.isAnnotationPresent(Embeddable.class)
                || candidate.isAnnotationPresent(MappedSuperclass.class)
                || candidate.isAnnotationPresent(Converter.class));
    }
}
