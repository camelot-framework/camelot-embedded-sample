package ru.yandex.qatools.camelot.example;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.internal.scanning.PackageNamesScanner;
import org.glassfish.jersey.server.spring.scope.RequestContextFilter;

/**
 * @author smecsia
 */
public class Application extends ResourceConfig {

    public Application() {
        register(RequestContextFilter.class);
        register(JacksonFeature.class);
        registerFinder(packageScanner());
    }

    private PackageNamesScanner packageScanner() {
        return new PackageNamesScanner(new String[]{getClass().getPackage().getName()}, true);
    }
}
