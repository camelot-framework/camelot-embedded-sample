package ru.yandex.qatools.camelot.example;

import org.springframework.stereotype.Component;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * @author: Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
@Path("/")
@Component
public class IndexResource {
    @GET
    @Produces({MediaType.APPLICATION_JSON + ";charset=utf-8"})
    public String index() {
        return "Server started up";
    }
}
