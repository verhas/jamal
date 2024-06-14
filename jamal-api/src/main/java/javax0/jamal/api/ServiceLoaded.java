package javax0.jamal.api;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

/**
 * This helper interface can load classes that implement a specific interface using the service loader. If the service
 * loader does not see the classes (the program sees that zero service instances were loaded) then as a fallback it tries
 * to load the {@code META-INF/services/className} files and the classes listed in those.
 * <p>
 * The Javadoc Taglet execution environment for some reason does not load the service loader loaded instances.
 * This is a workaround.
 * <p>
 * The {@link Macro} interface extends this interface, because macros are service loaded.
 * <p>
 * This is a very general interface, could be used in any other project, nothing specific to Jamal.
 */
public interface ServiceLoaded {

    /**
     * Load the classes that implement the interface {@code klass} and are provided by the modules or are available.
     *
     * @param klass the interface for which the implementing class instances are needed
     * @param <T>   the interface
     * @return the list of instances
     */
    static <T> List<T> getInstances(Class<T> klass) {
        final var services =  getInstances(klass, Thread.currentThread().getContextClassLoader());
        if(!services.isEmpty()){
            return services;
        }
        return getInstances(klass, ServiceLoaded.class.getClassLoader());
    }

    static <T> List<T> getInstances(Class<T> klass, final ClassLoader cl) {
        List<T> list = new ArrayList<>();
        try {
            final ServiceLoader<T> services = ServiceLoader.load(klass, cl);
            services.iterator().forEachRemaining(list::add);
            if (list.isEmpty()) {
                loadViaMetaInf(klass, list, cl);
            }
            return list;
        } catch (ServiceConfigurationError ignored) {
            loadViaMetaInf(klass, list, cl);
            return list;
        }
    }

    /**
     * Load the classes using the names listed in the {@code META-INF/services/}<i>class name</i> resources found by the
     * classloader.
     * <p>
     * The implementation mimics the behaviour of the class loader using the {@code provider()} public static method if
     * it exists in the implementation.
     * <p>
     * Class types and assignability is not checked by the method. If there is any discrepancy a class cast exception
     * will occur.
     *
     * @param klass the interface for which the classes are to be loaded
     * @param list  the list to fill the instances to
     * @param <T>   the klass type
     */
    static <T> void loadViaMetaInf(final Class<T> klass, final List<T> list, final ClassLoader cl) {
        try {
            final var classes = new HashSet<Class<T>>(); // different classloaders in the hierarchy may load the same file more than once
            for (final var url : loadResources("META-INF/services/" + klass.getName(), cl)) {
                try (var is = url.openStream()) {
                    for (final var className : new String(is.readAllBytes(), StandardCharsets.UTF_8).split("[\n\r]+")) {
                        try {
                            final var providerKlass = (Class<T>) Class.forName(className);
                            if (!classes.contains(providerKlass)) {
                                classes.add(providerKlass);
                                final Method providerMethod = getProvider(providerKlass);
                                final T instance;
                                if (providerMethod == null) {
                                    instance = providerKlass.getConstructor().newInstance();
                                } else {
                                    instance = (T) providerMethod.invoke(null);
                                }
                                list.add(instance);
                            }
                        } catch (ClassCastException |
                                 ClassNotFoundException |
                                 NoSuchMethodException |
                                 InvocationTargetException |
                                 InstantiationException |
                                 IllegalAccessException e) {
                            // ignored, here we try our best
                        }
                    }
                }
            }
        } catch (IOException e) {
            //ignored
        }
    }


    /**
     * Get the provider method from the class. The {@code provider()} is a public static method in the class. If it
     * exists it has to return an instance of the serviced class. In this case the class {@code klass} does not even
     * need to implement the service interface.
     *
     * @param klass the provider class
     * @param <T>   the klass type
     * @return the provider method or {@code null} if there is no provider method in the class
     */
    private static <T> Method getProvider(Class<T> klass) {
        try {
            return klass.getDeclaredMethod("provider");
        } catch (NoClassDefFoundError | NoSuchMethodException e) {
            return null;
        }
    }

    /**
     * Get the url list of the resources for the given name.
     *
     * @param name        the name of the resource
     * @param classLoader the class loader to use or {@code null} to use the system class loader
     * @return the list of the urls
     * @throws IOException if the resources cannot be loaded
     */
    static List<URL> loadResources(String name, ClassLoader classLoader) throws IOException {
        final List<URL> list = new ArrayList<>();
        final Enumeration<URL> systemResources =
                (classLoader == null ? ClassLoader.getSystemClassLoader() : classLoader)
                        .getResources(name);
        while (systemResources.hasMoreElements()) {
            list.add(systemResources.nextElement());
        }
        return list;
    }

}
