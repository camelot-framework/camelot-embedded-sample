package ru.yandex.qatools.camelot.example;

import org.springframework.stereotype.Component;
import ru.yandex.qatools.camelot.api.PluginInterop;
import ru.yandex.qatools.camelot.api.annotations.Plugin;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.HashSet;
import java.util.Set;

@Path("/test-user/test")
@Component
public class TestResource {
    @Plugin(TestAggregator.class)
    PluginInterop plugin;

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public Set<Object> getStates() {
        Set<Object> browsers = new HashSet<>();
        for (Object key : plugin.repo().keys()) {
            browsers.add(plugin.repo().get((String) key));
        }
        return browsers;
    }
}
