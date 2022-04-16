package javax0.jamal.api;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
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
        ServiceLoader<T> services = ServiceLoader.load(klass);
        List<T> list = new ArrayList<>();
        services.iterator().forEachRemaining(list::add);
        if (list.size() == 0) {
            try {
                final var classes = new HashSet<Class<T>>(); // different classloaders in the hierarchy may load the same file more than once
                for (final var is : loadResources("META-INF/services/" + klass.getName(), ServiceLoaded.class.getClassLoader())) {
                    for (final var className : new String(is.readAllBytes(), StandardCharsets.UTF_8).split("[\n\r]+")) {
                        try {
                            final var providerKlass = (Class<T>)Class.forName(className);
                            if( !classes.contains(providerKlass)) {
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
            } catch (IOException e) {
                //ignored
            }
        }
        return list;
    }


    private static <T> Method getProvider(Class<T> klass){
        try {
            return klass.getDeclaredMethod("provider");
        }catch(NoClassDefFoundError | NoSuchMethodException e){
            return null;
        }
    }

    static List<InputStream> loadResources(String name, ClassLoader classLoader) throws IOException {
        final List<InputStream> list = new ArrayList<>();
        final Enumeration<URL> systemResources =
                (classLoader == null ? ClassLoader.getSystemClassLoader() : classLoader)
                        .getResources(name);
        while (systemResources.hasMoreElements()) {
            list.add(systemResources.nextElement().openStream());
        }
        return list;
    }

}
