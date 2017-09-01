package uk.gov.pay.apps.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/")
public class HelloResource {

    @GET
    @Path("hello")
    @Produces(APPLICATION_JSON)
    public Response sayHello() {
        return Response.ok().entity(hello()).build();
    }

    private Map<String, String> hello() {
        return new HashMap<String, String>() {{
            put("message", "hello");
            put("name", "payapps");
        }};
    }
}
