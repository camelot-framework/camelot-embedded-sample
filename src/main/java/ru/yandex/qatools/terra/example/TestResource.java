package ru.yandex.qatools.terra.example;

import ru.yandex.qatools.terra.api.AggregatorRepository;
import ru.yandex.qatools.terra.api.annotations.Repository;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.HashSet;
import java.util.Set;

@Path("/test-user/test")
public class TestResource {
    @Repository
    AggregatorRepository repo;

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public Set<Object> getStates() {
        Set<Object> browsers = new HashSet<Object>();
        for (String key : repo.keys()) {
            browsers.add(repo.get(key));
        }
        return browsers;
    }
}
